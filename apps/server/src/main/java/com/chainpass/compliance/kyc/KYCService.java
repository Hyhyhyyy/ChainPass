package com.chainpass.compliance.kyc;

import com.chainpass.did.entity.DIDDocument;
import com.chainpass.did.service.DIDService;
import com.chainpass.exception.BusinessException;
import com.chainpass.vc.dto.VCDto;
import com.chainpass.vc.service.VCService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.List;
import java.text.Normalizer;
import java.util.Locale;

/**
 * KYC服务 - 身份认证核心服务
 */
@Service
@RequiredArgsConstructor
public class KYCService {

    private static final Logger log = LoggerFactory.getLogger(KYCService.class);

    private final KYCMapper kycMapper;
    private final DIDService didService;
    private final VCService vcService;

    /**
     * 提交KYC申请
     */
    @Transactional
    public KYCRecord submitKYC(Long userId, KYCDto.KYCSubmitRequest request) {
        log.info("Submitting KYC for user: {}", userId);

        // 检查是否已有KYC记录
        KYCRecord existing = kycMapper.findByUserId(userId);
        if (existing != null && existing.getVerificationStatus() == 2) {
            throw new BusinessException("用户已完成KYC认证");
        }

        // 获取用户DID
        DIDDocument didDoc = didService.getDIDByUserId(userId);
        if (didDoc == null) {
            throw new BusinessException("请先创建DID");
        }

        // 创建KYC记录
        KYCRecord record = KYCRecord.builder()
            .userId(userId)
            .did(didDoc.getId())
            .kycLevel(1) // 基础等级
            .fullName(request.getFullName())
            .nationality(request.getNationality())
            .idType(request.getIdType())
            .idNumber(request.getIdNumber())
            .idDocumentFront(request.getIdDocumentFront())
            .idDocumentBack(request.getIdDocumentBack())
            .facePhoto(request.getFacePhoto())
            .verificationStatus(1) // 审核中
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .submittedAt(Instant.now())
            .build();

        if (existing != null) {
            record.setId(existing.getId());
            kycMapper.updateById(record);
        } else {
            kycMapper.insert(record);
        }

        log.info("KYC submitted for manual/provider review: {}", userId);
        return record;
    }

    /**
     * 审核通过KYC
     */
    @Transactional
    public void approveKYC(Long kycId, Long reviewerId) {
        log.info("Approving KYC: {}", kycId);

        KYCRecord record = kycMapper.selectById(kycId);
        if (record == null || record.getVerificationStatus() != 1) {
            throw new BusinessException("KYC记录不存在或不在审核中");
        }

        // 更新状态
        record.setVerificationStatus(2); // 通过
        record.setVerifiedBy(reviewerId);
        record.setVerifiedAt(Instant.now());
        record.setExpiresAt(Instant.now().plus(730, ChronoUnit.DAYS)); // 2年有效期
        record.setUpdatedAt(Instant.now());

        // 签发KYC凭证
        VCDto.IssueVCRequest vcRequest = new VCDto.IssueVCRequest();
        vcRequest.setHolderDid(record.getDid());
        vcRequest.setVcType("KYCCredential");
        // 数据最小化：凭证只证明审核结论，不复制姓名、证件号或国籍。
        vcRequest.setClaims(Map.of(
            "assuranceLevel", record.getKycLevel(),
            "verifiedAt", record.getVerifiedAt().toString(),
            "validUntil", record.getExpiresAt().toString(),
            "verificationPolicy", "chainpass-manual-review-v1"
        ));

        var vc = vcService.issueCredential(vcRequest);
        record.setVcId(vc.getId());

        kycMapper.updateById(record);

        log.info("KYC approved and VC issued: {}", kycId);
    }

    /** 拒绝KYC。审核操作只由具有compliance:kyc:audit权限的控制器调用。 */
    @Transactional
    public void rejectKYC(Long kycId, Long reviewerId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessException("拒绝原因不能为空");
        }
        KYCRecord record = kycMapper.selectById(kycId);
        if (record == null || record.getVerificationStatus() != 1) {
            throw new BusinessException("KYC记录不存在或不在审核中");
        }
        record.setVerificationStatus(3);
        record.setVerifiedBy(reviewerId);
        record.setRejectReason(reason.trim());
        record.setUpdatedAt(Instant.now());
        kycMapper.updateById(record);
    }

    /**
     * 获取KYC状态
     */
    public KYCDto.KYCStatusResponse getKYCStatus(Long userId) {
        KYCRecord record = kycMapper.findByUserId(userId);

        KYCDto.KYCStatusResponse response = new KYCDto.KYCStatusResponse();
        if (record == null) {
            response.setVerified(false);
            response.setKycLevel(0);
            response.setStatus("NOT_SUBMITTED");
            response.setMessage("尚未提交KYC认证");
        } else {
            response.setVerified(record.getVerificationStatus() == 2);
            response.setKycLevel(record.getKycLevel());
            response.setStatus(getStatusText(record.getVerificationStatus()));
            response.setMessage(getStatusMessage(record.getVerificationStatus()));
        }

        return response;
    }

    /**
     * 获取KYC详情
     */
    public KYCDto.KYCResponse getKYCDetail(Long userId) {
        KYCRecord record = kycMapper.findByUserId(userId);
        if (record == null) {
            return null;
        }

        KYCDto.KYCResponse response = new KYCDto.KYCResponse();
        response.setId(record.getId());
        response.setDid(record.getDid());
        response.setKycLevel(record.getKycLevel());
        response.setKycLevelName(getLevelName(record.getKycLevel()));
        response.setFullName(maskName(record.getFullName()));
        response.setNationality(record.getNationality());
        response.setIdType(record.getIdType());
        response.setStatus(getStatusText(record.getVerificationStatus()));
        response.setStatusName(getStatusName(record.getVerificationStatus()));
        response.setVerifiedAt(record.getVerifiedAt() != null ? record.getVerifiedAt().toString() : null);
        response.setExpiresAt(record.getExpiresAt() != null ? record.getExpiresAt().toString() : null);
        response.setVcId(record.getVcId());

        return response;
    }

    /**
     * 检查用户是否通过KYC
     */
    public boolean isKYCVerified(Long userId) {
        KYCRecord record = kycMapper.findByUserId(userId);
        return isApprovedAndCurrent(record);
    }

    public boolean isDIDKYCVerified(String did) {
        return isApprovedAndCurrent(kycMapper.findByDid(did));
    }

    /**
     * 将订单受益人姓名与已审核姓名做保守的规范化精确匹配。
     * 这不是模糊人名识别；不一致时只能触发人工复核，不能证明欺诈。
     */
    public boolean isBeneficiaryNameConsistent(String did, String beneficiaryName) {
        KYCRecord record = kycMapper.findByDid(did);
        if (!isApprovedAndCurrent(record) || beneficiaryName == null) {
            return false;
        }
        return normalizeName(record.getFullName()).equals(normalizeName(beneficiaryName));
    }

    private String normalizeName(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFKC)
            .replaceAll("[\\s·・.'’-]", "")
            .toUpperCase(Locale.ROOT);
    }

    private boolean isApprovedAndCurrent(KYCRecord record) {
        return record != null && record.getVerificationStatus() == 2
            && record.getExpiresAt() != null && record.getExpiresAt().isAfter(Instant.now());
    }

    public List<KYCDto.KYCReviewResponse> getReviewQueue(Integer status) {
        if (status == null || status < 1 || status > 3) {
            throw new BusinessException("审核状态必须是1、2或3");
        }
        return kycMapper.findByStatus(status).stream().map(this::toReviewResponse).toList();
    }

    public KYCDto.KYCReviewResponse getReviewSubmission(Long id) {
        KYCRecord record = kycMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("KYC记录不存在");
        }
        return toReviewResponse(record);
    }

    private KYCDto.KYCReviewResponse toReviewResponse(KYCRecord record) {
        KYCDto.KYCReviewResponse response = new KYCDto.KYCReviewResponse();
        response.setId(record.getId());
        response.setDid(record.getDid());
        response.setFullName(record.getFullName());
        response.setNationality(record.getNationality());
        response.setIdType(record.getIdType());
        response.setIdNumber(record.getIdNumber());
        response.setStatus(getStatusText(record.getVerificationStatus()));
        response.setSubmittedAt(record.getSubmittedAt() == null ? null : record.getSubmittedAt().toString());
        return response;
    }

    private String getStatusText(Integer status) {
        return switch (status) {
            case 0 -> "NOT_SUBMITTED";
            case 1 -> "PENDING";
            case 2 -> "APPROVED";
            case 3 -> "REJECTED";
            default -> "UNKNOWN";
        };
    }

    private String getStatusName(Integer status) {
        return switch (status) {
            case 0 -> "未提交";
            case 1 -> "审核中";
            case 2 -> "已通过";
            case 3 -> "已拒绝";
            default -> "未知";
        };
    }

    private String getStatusMessage(Integer status) {
        return switch (status) {
            case 0 -> "尚未提交KYC认证";
            case 1 -> "KYC认证审核中，请耐心等待";
            case 2 -> "KYC认证已通过";
            case 3 -> "KYC认证被拒绝，请重新提交";
            default -> "未知状态";
        };
    }

    private String getLevelName(Integer level) {
        return switch (level) {
            case 0 -> "未认证";
            case 1 -> "基础认证";
            case 2 -> "中级认证";
            case 3 -> "高级认证";
            default -> "未知";
        };
    }

    private String maskName(String name) {
        if (name == null || name.length() <= 1) {
            return name;
        }
        return name.charAt(0) + "*".repeat(name.length() - 1);
    }
}
