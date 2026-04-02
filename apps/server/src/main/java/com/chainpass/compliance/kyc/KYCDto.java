package com.chainpass.compliance.kyc;

import jakarta.validation.constraints.NotBlank;

/**
 * KYC DTO
 */
public class KYCDto {

    public static class KYCSubmitRequest {
        @NotBlank(message = "姓名不能为空")
        private String fullName;

        @NotBlank(message = "国籍不能为空")
        private String nationality;

        @NotBlank(message = "证件类型不能为空")
        private String idType;

        @NotBlank(message = "证件号码不能为空")
        private String idNumber;

        private String idDocumentFront;
        private String idDocumentBack;
        private String facePhoto;

        public KYCSubmitRequest() {}

        // Getters and Setters
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
    }

    public static class KYCResponse {
        private Long id;
        private String did;
        private Integer kycLevel;
        private String kycLevelName;
        private String fullName;
        private String nationality;
        private String idType;
        private String status;
        private String statusName;
        private String verifiedAt;
        private String expiresAt;
        private String vcId;

        public KYCResponse() {}

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getDid() { return did; }
        public void setDid(String did) { this.did = did; }

        public Integer getKycLevel() { return kycLevel; }
        public void setKycLevel(Integer kycLevel) { this.kycLevel = kycLevel; }

        public String getKycLevelName() { return kycLevelName; }
        public void setKycLevelName(String kycLevelName) { this.kycLevelName = kycLevelName; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getNationality() { return nationality; }
        public void setNationality(String nationality) { this.nationality = nationality; }

        public String getIdType() { return idType; }
        public void setIdType(String idType) { this.idType = idType; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getStatusName() { return statusName; }
        public void setStatusName(String statusName) { this.statusName = statusName; }

        public String getVerifiedAt() { return verifiedAt; }
        public void setVerifiedAt(String verifiedAt) { this.verifiedAt = verifiedAt; }

        public String getExpiresAt() { return expiresAt; }
        public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }

        public String getVcId() { return vcId; }
        public void setVcId(String vcId) { this.vcId = vcId; }
    }

    public static class KYCStatusResponse {
        private boolean verified;
        private Integer kycLevel;
        private String status;
        private String message;

        public KYCStatusResponse() {}

        // Getters and Setters
        public boolean isVerified() { return verified; }
        public void setVerified(boolean verified) { this.verified = verified; }

        public Integer getKycLevel() { return kycLevel; }
        public void setKycLevel(Integer kycLevel) { this.kycLevel = kycLevel; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}