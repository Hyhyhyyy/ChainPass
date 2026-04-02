package com.chainpass.vc.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * VC请求/响应DTO
 */
public class VCDto {

    /**
     * 签发VC请求
     */
    public static class IssueVCRequest {
        /**
         * 持有者DID
         */
        @NotBlank(message = "持有者DID不能为空")
        private String holderDid;

        /**
         * 凭证类型
         */
        @NotBlank(message = "凭证类型不能为空")
        private String vcType;

        /**
         * 凭证属性
         */
        private Map<String, Object> claims;

        public IssueVCRequest() {}

        // Getters and Setters
        public String getHolderDid() { return holderDid; }
        public void setHolderDid(String holderDid) { this.holderDid = holderDid; }

        public String getVcType() { return vcType; }
        public void setVcType(String vcType) { this.vcType = vcType; }

        public Map<String, Object> getClaims() { return claims; }
        public void setClaims(Map<String, Object> claims) { this.claims = claims; }
    }

    /**
     * 验证VC请求
     */
    public static class VerifyVCRequest {
        @NotBlank(message = "VC ID不能为空")
        private String vcId;

        public VerifyVCRequest() {}

        // Getters and Setters
        public String getVcId() { return vcId; }
        public void setVcId(String vcId) { this.vcId = vcId; }
    }

    /**
     * VC响应
     */
    public static class VCResponse {
        private String vcId;
        private String holderDid;
        private String vcType;
        private String typeName;
        private com.chainpass.vc.entity.VerifiableCredential vc;
        private String status;
        private String issuedAt;
        private String expiresAt;

        public VCResponse() {}

        // Getters and Setters
        public String getVcId() { return vcId; }
        public void setVcId(String vcId) { this.vcId = vcId; }

        public String getHolderDid() { return holderDid; }
        public void setHolderDid(String holderDid) { this.holderDid = holderDid; }

        public String getVcType() { return vcType; }
        public void setVcType(String vcType) { this.vcType = vcType; }

        public String getTypeName() { return typeName; }
        public void setTypeName(String typeName) { this.typeName = typeName; }

        public com.chainpass.vc.entity.VerifiableCredential getVc() { return vc; }
        public void setVc(com.chainpass.vc.entity.VerifiableCredential vc) { this.vc = vc; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getIssuedAt() { return issuedAt; }
        public void setIssuedAt(String issuedAt) { this.issuedAt = issuedAt; }

        public String getExpiresAt() { return expiresAt; }
        public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
    }

    /**
     * 验证结果
     */
    public static class VerifyResult {
        private boolean valid;
        private String vcId;
        private String vcType;
        private String holderDid;
        private String message;

        public VerifyResult() {}

        // Getters and Setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }

        public String getVcId() { return vcId; }
        public void setVcId(String vcId) { this.vcId = vcId; }

        public String getVcType() { return vcType; }
        public void setVcType(String vcType) { this.vcType = vcType; }

        public String getHolderDid() { return holderDid; }
        public void setHolderDid(String holderDid) { this.holderDid = holderDid; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public static VerifyResult valid(String vcId, String vcType, String holderDid) {
            VerifyResult result = new VerifyResult();
            result.setValid(true);
            result.setVcId(vcId);
            result.setVcType(vcType);
            result.setHolderDid(holderDid);
            result.setMessage("凭证验证通过");
            return result;
        }

        public static VerifyResult invalid(String message) {
            VerifyResult result = new VerifyResult();
            result.setValid(false);
            result.setMessage(message);
            return result;
        }
    }
}