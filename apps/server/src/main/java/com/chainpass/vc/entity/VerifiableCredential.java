package com.chainpass.vc.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

/**
 * 可验证凭证实体 - W3C VC标准
 * 参考: https://www.w3.org/TR/vc-data-model/
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerifiableCredential {

    /**
     * 上下文
     */
    private List<String> context = List.of("https://www.w3.org/2018/credentials/v1");

    /**
     * VC唯一标识
     */
    private String id;

    /**
     * 类型列表
     */
    private List<String> type;

    /**
     * 签发者
     */
    private Issuer issuer;

    /**
     * 签发时间
     */
    private String issuanceDate;

    /**
     * 过期时间
     */
    private String expirationDate;

    /**
     * 凭证主体
     */
    private CredentialSubject credentialSubject;

    /**
     * 证明(签名)
     */
    private Proof proof;

    // Constructors
    public VerifiableCredential() {}

    public VerifiableCredential(List<String> context, String id, List<String> type, Issuer issuer,
                                String issuanceDate, String expirationDate, CredentialSubject credentialSubject,
                                Proof proof) {
        this.context = context;
        this.id = id;
        this.type = type;
        this.issuer = issuer;
        this.issuanceDate = issuanceDate;
        this.expirationDate = expirationDate;
        this.credentialSubject = credentialSubject;
        this.proof = proof;
    }

    // Getters and Setters
    public List<String> getContext() { return context; }
    public void setContext(List<String> context) { this.context = context; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public List<String> getType() { return type; }
    public void setType(List<String> type) { this.type = type; }

    public Issuer getIssuer() { return issuer; }
    public void setIssuer(Issuer issuer) { this.issuer = issuer; }

    public String getIssuanceDate() { return issuanceDate; }
    public void setIssuanceDate(String issuanceDate) { this.issuanceDate = issuanceDate; }

    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }

    public CredentialSubject getCredentialSubject() { return credentialSubject; }
    public void setCredentialSubject(CredentialSubject credentialSubject) { this.credentialSubject = credentialSubject; }

    public Proof getProof() { return proof; }
    public void setProof(Proof proof) { this.proof = proof; }

    // Builder
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private List<String> context = List.of("https://www.w3.org/2018/credentials/v1");
        private String id;
        private List<String> type;
        private Issuer issuer;
        private String issuanceDate;
        private String expirationDate;
        private CredentialSubject credentialSubject;
        private Proof proof;

        public Builder context(List<String> context) { this.context = context; return this; }
        public Builder id(String id) { this.id = id; return this; }
        public Builder type(List<String> type) { this.type = type; return this; }
        public Builder issuer(Issuer issuer) { this.issuer = issuer; return this; }
        public Builder issuanceDate(String issuanceDate) { this.issuanceDate = issuanceDate; return this; }
        public Builder expirationDate(String expirationDate) { this.expirationDate = expirationDate; return this; }
        public Builder credentialSubject(CredentialSubject credentialSubject) { this.credentialSubject = credentialSubject; return this; }
        public Builder proof(Proof proof) { this.proof = proof; return this; }

        public VerifiableCredential build() {
            return new VerifiableCredential(context, id, type, issuer, issuanceDate, expirationDate, credentialSubject, proof);
        }
    }

    /**
     * 签发者信息
     */
    public static class Issuer {
        private String id;
        private String name;

        public Issuer() {}

        public Issuer(String id, String name) {
            this.id = id;
            this.name = name;
        }

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        // Builder
        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String id;
            private String name;

            public Builder id(String id) { this.id = id; return this; }
            public Builder name(String name) { this.name = name; return this; }

            public Issuer build() {
                return new Issuer(id, name);
            }
        }
    }

    /**
     * 凭证主体
     */
    public static class CredentialSubject {
        /**
         * 持有者DID
         */
        private String id;

        /**
         * 凭证属性
         */
        private Map<String, Object> claims;

        public CredentialSubject() {}

        public CredentialSubject(String id, Map<String, Object> claims) {
            this.id = id;
            this.claims = claims;
        }

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public Map<String, Object> getClaims() { return claims; }
        public void setClaims(Map<String, Object> claims) { this.claims = claims; }

        // Builder
        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String id;
            private Map<String, Object> claims;

            public Builder id(String id) { this.id = id; return this; }
            public Builder claims(Map<String, Object> claims) { this.claims = claims; return this; }

            public CredentialSubject build() {
                return new CredentialSubject(id, claims);
            }
        }
    }

    /**
     * 证明信息
     */
    public static class Proof {
        /**
         * 证明类型
         */
        private String type;

        /**
         * 创建时间
         */
        private String created;

        /**
         * 证明目的
         */
        private String proofPurpose;

        /**
         * 验证方法
         */
        private String verificationMethod;

        /**
         * 证明值(签名)
         */
        private String proofValue;

        public Proof() {}

        public Proof(String type, String created, String proofPurpose, String verificationMethod, String proofValue) {
            this.type = type;
            this.created = created;
            this.proofPurpose = proofPurpose;
            this.verificationMethod = verificationMethod;
            this.proofValue = proofValue;
        }

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getCreated() { return created; }
        public void setCreated(String created) { this.created = created; }

        public String getProofPurpose() { return proofPurpose; }
        public void setProofPurpose(String proofPurpose) { this.proofPurpose = proofPurpose; }

        public String getVerificationMethod() { return verificationMethod; }
        public void setVerificationMethod(String verificationMethod) { this.verificationMethod = verificationMethod; }

        public String getProofValue() { return proofValue; }
        public void setProofValue(String proofValue) { this.proofValue = proofValue; }

        // Builder
        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String type;
            private String created;
            private String proofPurpose;
            private String verificationMethod;
            private String proofValue;

            public Builder type(String type) { this.type = type; return this; }
            public Builder created(String created) { this.created = created; return this; }
            public Builder proofPurpose(String proofPurpose) { this.proofPurpose = proofPurpose; return this; }
            public Builder verificationMethod(String verificationMethod) { this.verificationMethod = verificationMethod; return this; }
            public Builder proofValue(String proofValue) { this.proofValue = proofValue; return this; }

            public Proof build() {
                return new Proof(type, created, proofPurpose, verificationMethod, proofValue);
            }
        }
    }
}