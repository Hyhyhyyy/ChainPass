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

        // 模拟自动审核通过
        approveKYC(record.getId(), userId);

        log.info("KYC submitted and auto-approved for user: {}", userId);
        return record;
    }

    /**
     * 审核通过KYC
     */
    @Transactional
    public void approveKYC(Long kycId, Long reviewerId) {
        log.info("Approving KYC: {}", kycId);

        KYCRecord record = kycMapper.selectById(kycId);
        if (record == null) {
            throw new BusinessException("KYC记录不存在");
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
        vcRequest.setClaims(Map.of(
            "level", record.getKycLevel(),
            "verifiedAt", record.getVerifiedAt().toString(),
            "nationality", record.getNationality(),
            "expiresAt", record.getExpiresAt().toString()
        ));

        var vc = vcService.issueCredential(vcRequest);
        record.setVcId(vc.getId());

        kycMapper.updateById(record);

        log.info("KYC approved and VC issued: {}", kycId);
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
        return record != null && record.getVerificationStatus() == 2;
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