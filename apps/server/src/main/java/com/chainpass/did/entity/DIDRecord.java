package com.chainpass.did.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.Instant;

/**
 * DID记录实体 - 数据库映射
 */
@TableName("chain_did")
public class DIDRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * DID标识符: did:chainpass:xxx
     */
    private String did;

    /**
     * 关联用户ID
     */
    private Long userId;

    /**
     * Ed25519公钥(Base64编码)
     */
    private String publicKey;

    /**
     * 加密后的私钥(可选，用户自行管理)
     */
    private String privateKeyEncrypted;

    /**
     * 完整DID文档JSON
     */
    private String didDocument;

    /**
     * 状态: 0-激活 1-停用 2-吊销
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Instant createdAt;

    /**
     * 更新时间
     */
    private Instant updatedAt;

    /**
     * 过期时间
     */
    private Instant expiresAt;

    /**
     * 吊销时间
     */
    private Instant revokedAt;

    /**
     * 吊销原因
     */
    private String revokeReason;

    // Constructors
    public DIDRecord() {}

    public DIDRecord(Long id, String did, Long userId, String publicKey, String privateKeyEncrypted,
                     String didDocument, Integer status, Instant createdAt, Instant updatedAt,
                     Instant expiresAt, Instant revokedAt, String revokeReason) {
        this.id = id;
        this.did = did;
        this.userId = userId;
        this.publicKey = publicKey;
        this.privateKeyEncrypted = privateKeyEncrypted;
        this.didDocument = didDocument;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
        this.revokeReason = revokeReason;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDid() { return did; }
    public void setDid(String did) { this.did = did; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }

    public String getPrivateKeyEncrypted() { return privateKeyEncrypted; }
    public void setPrivateKeyEncrypted(String privateKeyEncrypted) { this.privateKeyEncrypted = privateKeyEncrypted; }

    public String getDidDocument() { return didDocument; }
    public void setDidDocument(String didDocument) { this.didDocument = didDocument; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }

    public String getRevokeReason() { return revokeReason; }
    public void setRevokeReason(String revokeReason) { this.revokeReason = revokeReason; }

    // Builder
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String did;
        private Long userId;
        private String publicKey;
        private String privateKeyEncrypted;
        private String didDocument;
        private Integer status;
        private Instant createdAt;
        private Instant updatedAt;
        private Instant expiresAt;
        private Instant revokedAt;
        private String revokeReason;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder did(String did) { this.did = did; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder publicKey(String publicKey) { this.publicKey = publicKey; return this; }
        public Builder privateKeyEncrypted(String privateKeyEncrypted) { this.privateKeyEncrypted = privateKeyEncrypted; return this; }
        public Builder didDocument(String didDocument) { this.didDocument = didDocument; return this; }
        public Builder status(Integer status) { this.status = status; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder expiresAt(Instant expiresAt) { this.expiresAt = expiresAt; return this; }
        public Builder revokedAt(Instant revokedAt) { this.revokedAt = revokedAt; return this; }
        public Builder revokeReason(String revokeReason) { this.revokeReason = revokeReason; return this; }

        public DIDRecord build() {
            return new DIDRecord(id, did, userId, publicKey, privateKeyEncrypted, didDocument,
                    status, createdAt, updatedAt, expiresAt, revokedAt, revokeReason);
        }
    }
}