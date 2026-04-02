package com.chainpass.vc.service;

import com.alibaba.fastjson2.JSON;
import com.chainpass.did.entity.DIDDocument;
import com.chainpass.did.service.DIDService;
import com.chainpass.exception.BusinessException;
import com.chainpass.vc.dto.VCDto;
import com.chainpass.vc.entity.*;
import com.chainpass.vc.mapper.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * VC服务 - 可验证凭证核心服务
 *
 * 实现W3C VC标准的凭证签发、验证、吊销功能
 * 使用真实的Ed25519签名验证
 */
@Service
@RequiredArgsConstructor
public class VCService {

    private static final Logger log = LoggerFactory.getLogger(VCService.class);

    private final VCRecordMapper vcRecordMapper;
    private final VCTypeMapper vcTypeMapper;
    private final DIDService didService;
    private final IssuerKeyService issuerKeyService;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 签发可验证凭证
     *
     * @param request 签发请求
     * @return 可验证凭证
     */
    @Transactional
    public VerifiableCredential issueCredential(VCDto.IssueVCRequest request) {
        log.info("Issuing VC for holder: {}, type: {}", request.getHolderDid(), request.getVcType());

        // 1. 验证持有者DID
        if (!didService.isValidDID(request.getHolderDid())) {
            throw new BusinessException("持有者DID无效或不存在");
        }

        // 2. 获取VC类型定义
        VCType vcType = vcTypeMapper.findByTypeCode(request.getVcType());
        if (vcType == null || vcType.getStatus() != 0) {
            throw new BusinessException("凭证类型不存在或已禁用");
        }

        // 3. 检查签发者密钥是否有效
        if (!issuerKeyService.isKeyValid()) {
            log.warn("Issuer key is expired, rotating keys...");
            issuerKeyService.rotateKeys();
        }

        try {
            // 4. 生成VC ID
            String vcId = "urn:uuid:" + UUID.randomUUID().toString();

            // 5. 构建凭证主体
            Map<String, Object> claims = request.getClaims() != null ? request.getClaims() : new HashMap<>();
            claims.put("credentialType", vcType.getTypeName());
            claims.put("issuer", IssuerKeyService.ISSUER_DID);

            VerifiableCredential.CredentialSubject subject = VerifiableCredential.CredentialSubject.builder()
                .id(request.getHolderDid())
                .claims(claims)
                .build();

            // 6. 构建VC（不含签名）
            Instant now = Instant.now();
            Instant expiresAt = now.plus(vcType.getValidityDays(), ChronoUnit.DAYS);

            VerifiableCredential vc = VerifiableCredential.builder()
                .id(vcId)
                .type(List.of("VerifiableCredential", request.getVcType()))
                .issuer(VerifiableCredential.Issuer.builder()
                    .id(IssuerKeyService.ISSUER_DID)
                    .name("ChainPass")
                    .build())
                .issuanceDate(now.toString())
                .expirationDate(expiresAt.toString())
                .credentialSubject(subject)
                .build();

            // 7. 计算凭证哈希（不含proof字段）
            String vcJson = JSON.toJSONString(vc);
            String credentialHash = hashSHA256(vcJson);

            // 8. 使用真实Ed25519签名
            String signature = issuerKeyService.sign(credentialHash);

            // 9. 添加证明
            vc.setProof(VerifiableCredential.Proof.builder()
                .type("Ed25519Signature2020")
                .created(now.toString())
                .proofPurpose("assertionMethod")
                .verificationMethod(issuerKeyService.getVerificationMethodId())
                .proofValue(signature)
                .build());

            // 10. 存储到数据库
            VCRecord record = VCRecord.builder()
                .vcId(vcId)
                .holderDid(request.getHolderDid())
                .issuerDid(IssuerKeyService.ISSUER_DID)
                .vcType(request.getVcType())
                .vcData(JSON.toJSONString(vc))
                .credentialHash(credentialHash)
                .signature(signature)
                .status(0) // 有效
                .issuedAt(now)
                .expiresAt(expiresAt)
                .build();

            vcRecordMapper.insert(record);

            log.info("VC issued successfully with real Ed25519 signature: {}", vcId);
            return vc;

        } catch (Exception e) {
            log.error("Failed to issue VC", e);
            throw new BusinessException("签发凭证失败: " + e.getMessage());
        }
    }

    /**
     * 验证可验证凭证
     *
     * @param vcId 凭证ID
     * @return 验证结果
     */
    public VCDto.VerifyResult verifyCredential(String vcId) {
        log.info("Verifying VC: {}", vcId);

        // 1. 查询凭证记录
        VCRecord record = vcRecordMapper.findByVcId(vcId);
        if (record == null) {
            return VCDto.VerifyResult.invalid("凭证不存在");
        }

        // 2. 检查是否已吊销
        if (record.getStatus() == 2) {
            return VCDto.VerifyResult.invalid("凭证已被吊销");
        }

        // 3. 检查过期时间
        if (record.getExpiresAt().isBefore(Instant.now())) {
            // 自动更新状态为过期
            if (record.getStatus() == 0) {
                vcRecordMapper.updateStatus(vcId, 1);
            }
            return VCDto.VerifyResult.invalid("凭证已过期");
        }

        // 4. 使用真实Ed25519签名验证
        boolean signatureValid = issuerKeyService.verify(record.getCredentialHash(), record.getSignature());
        if (!signatureValid) {
            log.warn("VC signature verification failed for: {}", vcId);
            return VCDto.VerifyResult.invalid("签名验证失败：凭证可能被篡改");
        }

        // 5. 验证持有者DID是否有效
        if (!didService.isValidDID(record.getHolderDid())) {
            return VCDto.VerifyResult.invalid("持有者DID无效或已吊销");
        }

        log.info("VC verified successfully with real signature: {}", vcId);
        return VCDto.VerifyResult.valid(vcId, record.getVcType(), record.getHolderDid());
    }

    /**
     * 获取用户的VC列表
     */
    public List<VCDto.VCResponse> getVCListByHolder(String holderDid) {
        List<VCRecord> records = vcRecordMapper.findByHolderDid(holderDid);
        List<VCDto.VCResponse> responses = new ArrayList<>();

        for (VCRecord record : records) {
            VCType vcType = vcTypeMapper.findByTypeCode(record.getVcType());
            VerifiableCredential vc = JSON.parseObject(record.getVcData(), VerifiableCredential.class);

            VCDto.VCResponse response = new VCDto.VCResponse();
            response.setVcId(record.getVcId());
            response.setHolderDid(record.getHolderDid());
            response.setVcType(record.getVcType());
            response.setTypeName(vcType != null ? vcType.getTypeName() : record.getVcType());
            response.setVc(vc);
            response.setStatus(getStatusText(record.getStatus()));
            response.setIssuedAt(record.getIssuedAt().toString());
            response.setExpiresAt(record.getExpiresAt().toString());

            responses.add(response);
        }

        return responses;
    }

    /**
     * 获取用户特定类型的有效VC
     */
    public VerifiableCredential getValidVC(String holderDid, String vcType) {
        List<VCRecord> records = vcRecordMapper.findValidByHolderDidAndType(holderDid, vcType);
        if (records.isEmpty()) {
            return null;
        }

        // 返回最新的一条
        VCRecord record = records.get(0);
        return JSON.parseObject(record.getVcData(), VerifiableCredential.class);
    }

    /**
     * 吊销凭证
     */
    @Transactional
    public void revokeCredential(String vcId, String reason) {
        log.info("Revoking VC: {}, reason: {}", vcId, reason);

        int updated = vcRecordMapper.revokeVC(vcId, reason);
        if (updated == 0) {
            throw new BusinessException("凭证不存在或已被吊销");
        }
    }

    /**
     * 获取所有VC类型
     */
    public List<VCType> getAllVCTypes() {
        return vcTypeMapper.findAllEnabled();
    }

    /**
     * 计算SHA256哈希
     */
    private String hashSHA256(String data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 状态文本转换
     */
    private String getStatusText(Integer status) {
        return switch (status) {
            case 0 -> "VALID";
            case 1 -> "EXPIRED";
            case 2 -> "REVOKED";
            default -> "UNKNOWN";
        };
    }
}