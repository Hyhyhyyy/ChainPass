package com.chainpass.vo;

/**
 * OAuth 配置响应
 */
public class OAuthConfig {

    private String clientId;
    private String redirectUri;
    private String responseType;
    private String scope;

    public OAuthConfig() {}

    public OAuthConfig(String clientId, String redirectUri, String responseType, String scope) {
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.responseType = responseType;
        this.scope = scope;
    }

    // Getters and Setters
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getRedirectUri() { return redirectUri; }
    public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }

    public String getResponseType() { return responseType; }
    public void setResponseType(String responseType) { this.responseType = responseType; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    // Builder
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String clientId;
        private String redirectUri;
        private String responseType;
        private String scope;

        public Builder clientId(String clientId) { this.clientId = clientId; return this; }
        public Builder redirectUri(String redirectUri) { this.redirectUri = redirectUri; return this; }
        public Builder responseType(String responseType) { this.responseType = responseType; return this; }
        public Builder scope(String scope) { this.scope = scope; return this; }

        public OAuthConfig build() {
            return new OAuthConfig(clientId, redirectUri, responseType, scope);
        }
    }
}