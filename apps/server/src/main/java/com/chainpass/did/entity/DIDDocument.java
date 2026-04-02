package com.chainpass.did.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * DID文档实体 - W3C DID标准
 * 参考: https://www.w3.org/TR/did-core/
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DIDDocument {

    /**
     * DID上下文
     */
    private List<String> context = List.of("https://www.w3.org/ns/did/v1");

    /**
     * DID标识符
     */
    private String id;

    /**
     * 验证方法列表
     */
    private List<VerificationMethod> verificationMethod;

    /**
     * 认证方法列表
     */
    private List<String> authentication;

    /**
     * 断言方法列表
     */
    private List<String> assertionMethod;

    /**
     * 密钥协商方法列表
     */
    private List<String> keyAgreement;

    /**
     * 服务端点列表
     */
    private List<Service> service;

    /**
     * 创建时间
     */
    private String created;

    /**
     * 更新时间
     */
    private String updated;

    // Constructors
    public DIDDocument() {}

    public DIDDocument(List<String> context, String id, List<VerificationMethod> verificationMethod,
                       List<String> authentication, List<String> assertionMethod, List<String> keyAgreement,
                       List<Service> service, String created, String updated) {
        this.context = context;
        this.id = id;
        this.verificationMethod = verificationMethod;
        this.authentication = authentication;
        this.assertionMethod = assertionMethod;
        this.keyAgreement = keyAgreement;
        this.service = service;
        this.created = created;
        this.updated = updated;
    }

    // Getters and Setters
    public List<String> getContext() { return context; }
    public void setContext(List<String> context) { this.context = context; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public List<VerificationMethod> getVerificationMethod() { return verificationMethod; }
    public void setVerificationMethod(List<VerificationMethod> verificationMethod) { this.verificationMethod = verificationMethod; }

    public List<String> getAuthentication() { return authentication; }
    public void setAuthentication(List<String> authentication) { this.authentication = authentication; }

    public List<String> getAssertionMethod() { return assertionMethod; }
    public void setAssertionMethod(List<String> assertionMethod) { this.assertionMethod = assertionMethod; }

    public List<String> getKeyAgreement() { return keyAgreement; }
    public void setKeyAgreement(List<String> keyAgreement) { this.keyAgreement = keyAgreement; }

    public List<Service> getService() { return service; }
    public void setService(List<Service> service) { this.service = service; }

    public String getCreated() { return created; }
    public void setCreated(String created) { this.created = created; }

    public String getUpdated() { return updated; }
    public void setUpdated(String updated) { this.updated = updated; }

    // Builder
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private List<String> context = List.of("https://www.w3.org/ns/did/v1");
        private String id;
        private List<VerificationMethod> verificationMethod;
        private List<String> authentication;
        private List<String> assertionMethod;
        private List<String> keyAgreement;
        private List<Service> service;
        private String created;
        private String updated;

        public Builder context(List<String> context) { this.context = context; return this; }
        public Builder id(String id) { this.id = id; return this; }
        public Builder verificationMethod(List<VerificationMethod> verificationMethod) { this.verificationMethod = verificationMethod; return this; }
        public Builder authentication(List<String> authentication) { this.authentication = authentication; return this; }
        public Builder assertionMethod(List<String> assertionMethod) { this.assertionMethod = assertionMethod; return this; }
        public Builder keyAgreement(List<String> keyAgreement) { this.keyAgreement = keyAgreement; return this; }
        public Builder service(List<Service> service) { this.service = service; return this; }
        public Builder created(String created) { this.created = created; return this; }
        public Builder updated(String updated) { this.updated = updated; return this; }

        public DIDDocument build() {
            return new DIDDocument(context, id, verificationMethod, authentication, assertionMethod,
                    keyAgreement, service, created, updated);
        }
    }

    /**
     * 验证方法
     */
    public static class VerificationMethod {
        /**
         * 验证方法ID
         */
        private String id;

        /**
         * 验证方法类型
         */
        private String type;

        /**
         * 控制者DID
         */
        private String controller;

        /**
         * 公钥(Base64编码)
         */
        private String publicKeyBase64;

        /**
         * 公钥(Multibase编码)
         */
        private String publicKeyMultibase;

        public VerificationMethod() {}

        public VerificationMethod(String id, String type, String controller, String publicKeyBase64, String publicKeyMultibase) {
            this.id = id;
            this.type = type;
            this.controller = controller;
            this.publicKeyBase64 = publicKeyBase64;
            this.publicKeyMultibase = publicKeyMultibase;
        }

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getController() { return controller; }
        public void setController(String controller) { this.controller = controller; }

        public String getPublicKeyBase64() { return publicKeyBase64; }
        public void setPublicKeyBase64(String publicKeyBase64) { this.publicKeyBase64 = publicKeyBase64; }

        public String getPublicKeyMultibase() { return publicKeyMultibase; }
        public void setPublicKeyMultibase(String publicKeyMultibase) { this.publicKeyMultibase = publicKeyMultibase; }

        // Builder
        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String id;
            private String type;
            private String controller;
            private String publicKeyBase64;
            private String publicKeyMultibase;

            public Builder id(String id) { this.id = id; return this; }
            public Builder type(String type) { this.type = type; return this; }
            public Builder controller(String controller) { this.controller = controller; return this; }
            public Builder publicKeyBase64(String publicKeyBase64) { this.publicKeyBase64 = publicKeyBase64; return this; }
            public Builder publicKeyMultibase(String publicKeyMultibase) { this.publicKeyMultibase = publicKeyMultibase; return this; }

            public VerificationMethod build() {
                return new VerificationMethod(id, type, controller, publicKeyBase64, publicKeyMultibase);
            }
        }
    }

    /**
     * 服务端点
     */
    public static class Service {
        /**
         * 服务ID
         */
        private String id;

        /**
         * 服务类型
         */
        private String type;

        /**
         * 服务端点URL
         */
        private String serviceEndpoint;

        public Service() {}

        public Service(String id, String type, String serviceEndpoint) {
            this.id = id;
            this.type = type;
            this.serviceEndpoint = serviceEndpoint;
        }

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getServiceEndpoint() { return serviceEndpoint; }
        public void setServiceEndpoint(String serviceEndpoint) { this.serviceEndpoint = serviceEndpoint; }

        // Builder
        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String id;
            private String type;
            private String serviceEndpoint;

            public Builder id(String id) { this.id = id; return this; }
            public Builder type(String type) { this.type = type; return this; }
            public Builder serviceEndpoint(String serviceEndpoint) { this.serviceEndpoint = serviceEndpoint; return this; }

            public Service build() {
                return new Service(id, type, serviceEndpoint);
            }
        }
    }
}