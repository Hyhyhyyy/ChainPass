package com.chainpass.compliance.kyc;

import com.baomidou.mybatisplus.annotation.*;
import java.time.Instant;

/**
 * KYC认证记录实体
 */
@TableName("comp_kyc")
public class KYCRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户DID
     */
    private String did;

    /**
     * KYC等级: 0-未认证 1-基础 2-中级 3-高级
     */
    private Integer kycLevel;

    /**
     * 真实姓名
     */
    private String fullName;

    /**
     * 国籍
     */
    private String nationality;

    /**
     * 证件类型
     */
    private String idType;

    /**
     * 证件号码
     */
    private String idNumber;

    /**
     * 证件正面照片URL
     */
    private String idDocumentFront;

    /**
     * 证件背面照片URL
     */
    private String idDocumentBack;

    /**
     * 人脸照片URL
     */
    private String facePhoto;

    /**
     * 审核状态: 0-待提交 1-审核中 2-通过 3-拒绝
     */
    private Integer verificationStatus;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 审核人ID
     */
    private Long verifiedBy;

    /**
     * 认证通过时间
     */
    private Instant verifiedAt;

    /**
     * 认证过期时间
     */
    private Instant expiresAt;

    /**
     * 签发的KYC凭证ID
     */
    private String vcId;

    /**
     * 创建时间
     */
    private Instant createdAt;

    /**
     * 更新时间
     */
    private Instant updatedAt;

    /**
     * 提交时间
     */
    private Instant submittedAt;

    // Constructors
    public KYCRecord() {}

    public KYCRecord(Long id, Long userId, String did, Integer kycLevel, String fullName,
                     String nationality, String idType, String idNumber, String idDocumentFront,
                     String idDocumentBack, String facePhoto, Integer verificationStatus,
                     String rejectReason, Long verifiedBy, Instant verifiedAt, Instant expiresAt,
                     String vcId, Instant createdAt, Instant updatedAt, Instant submittedAt) {
        this.id = id;
        this.userId = userId;
        this.did = did;
        this.kycLevel = kycLevel;
        this.fullName = fullName;
        this.nationality = nationality;
        this.idType = idType;
        this.idNumber = idNumber;
        this.idDocumentFront = idDocumentFront;
        this.idDocumentBack = idDocumentBack;
        this.facePhoto = facePhoto;
        this.verificationStatus = verificationStatus;
        this.rejectReason = rejectReason;
        this.verifiedBy = verifiedBy;
        this.verifiedAt = verifiedAt;
        this.expiresAt = expiresAt;
        this.vcId = vcId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.submittedAt = submittedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getDid() { return did; }
    public void setDid(String did) { this.did = did; }

    public Integer getKycLevel() { return kycLevel; }
    public void setKycLevel(Integer kycLevel) { this.kycLevel = kycLevel; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public String getIdType() { return idType; }
    public void setIdType(String idType) { this.idType = idType; }

    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }

    public String getIdDocumentFront() { return idDocumentFront; }
    public void setIdDocumentFront(String idDocumentFront) { this.idDocumentFront = idDocumentFront; }

    public String getIdDocumentBack() { return idDocumentBack; }
    public void setIdDocumentBack(String idDocumentBack) { this.idDocumentBack = idDocumentBack; }

    public String getFacePhoto() { return facePhoto; }
    public void setFacePhoto(String facePhoto) { this.facePhoto = facePhoto; }

    public Integer getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(Integer verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

    public Long getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(Long verifiedBy) { this.verifiedBy = verifiedBy; }

    public Instant getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Instant verifiedAt) { this.verifiedAt = verifiedAt; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public String getVcId() { return vcId; }
    public void setVcId(String vcId) { this.vcId = vcId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }

    // Builder
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Long userId;
        private String did;
        private Integer kycLevel;
        private String fullName;
        private String nationality;
        private String idType;
        private String idNumber;
        private String idDocumentFront;
        private String idDocumentBack;
        private String facePhoto;
        private Integer verificationStatus;
        private String rejectReason;
        private Long verifiedBy;
        private Instant verifiedAt;
        private Instant expiresAt;
        private String vcId;
        private Instant createdAt;
        private Instant updatedAt;
        private Instant submittedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder did(String did) { this.did = did; return this; }
        public Builder kycLevel(Integer kycLevel) { this.kycLevel = kycLevel; return this; }
        public Builder fullName(String fullName) { this.fullName = fullName; return this; }
        public Builder nationality(String nationality) { this.nationality = nationality; return this; }
        public Builder idType(String idType) { this.idType = idType; return this; }
        public Builder idNumber(String idNumber) { this.idNumber = idNumber; return this; }
        public Builder idDocumentFront(String idDocumentFront) { this.idDocumentFront = idDocumentFront; return this; }
        public Builder idDocumentBack(String idDocumentBack) { this.idDocumentBack = idDocumentBack; return this; }
        public Builder facePhoto(String facePhoto) { this.facePhoto = facePhoto; return this; }
        public Builder verificationStatus(Integer verificationStatus) { this.verificationStatus = verificationStatus; return this; }
        public Builder rejectReason(String rejectReason) { this.rejectReason = rejectReason; return this; }
        public Builder verifiedBy(Long verifiedBy) { this.verifiedBy = verifiedBy; return this; }
        public Builder verifiedAt(Instant verifiedAt) { this.verifiedAt = verifiedAt; return this; }
        public Builder expiresAt(Instant expiresAt) { this.expiresAt = expiresAt; return this; }
        public Builder vcId(String vcId) { this.vcId = vcId; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder submittedAt(Instant submittedAt) { this.submittedAt = submittedAt; return this; }

        public KYCRecord build() {
            return new KYCRecord(id, userId, did, kycLevel, fullName, nationality, idType,
                    idNumber, idDocumentFront, idDocumentBack, facePhoto, verificationStatus,
                    rejectReason, verifiedBy, verifiedAt, expiresAt, vcId, createdAt, updatedAt, submittedAt);
        }
    }
}