package com.chainpass.vo;

/**
 * ZKP 挑战响应
 */
public class ZKPChallenge {

    private String sessionId;
    private String challenge;
    private Long expiresAt;

    public ZKPChallenge() {}

    public ZKPChallenge(String sessionId, String challenge, Long expiresAt) {
        this.sessionId = sessionId;
        this.challenge = challenge;
        this.expiresAt = expiresAt;
    }

    // Getters and Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getChallenge() { return challenge; }
    public void setChallenge(String challenge) { this.challenge = challenge; }

    public Long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }

    // Builder
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String sessionId;
        private String challenge;
        private Long expiresAt;

        public Builder sessionId(String sessionId) { this.sessionId = sessionId; return this; }
        public Builder challenge(String challenge) { this.challenge = challenge; return this; }
        public Builder expiresAt(Long expiresAt) { this.expiresAt = expiresAt; return this; }

        public ZKPChallenge build() {
            return new ZKPChallenge(sessionId, challenge, expiresAt);
        }
    }
}