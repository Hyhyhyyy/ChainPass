package com.chainpass.vc.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.Instant;

/**
 * VC记录实体 - 数据库映射
 */
@TableName("chain_vc")
public class VCRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * VC唯一标识: urn:uuid:xxx
     */
    private String vcId;

    /**
     * 持有者DID
     */
    private String holderDid;

    /**
     * 签发者DID
     */
    private String issuerDid;

    /**
     * 凭证类型编码
     */
    private String vcType;

    /**
     * 完整VC JSON数据
     */
    private String vcData;

    /**
     * 凭证内容哈希(SHA256)
     */
    private String credentialHash;

    /**
     * Ed25519签名(Base64)
     */
    private String signature;

    /**
     * IPFS存储哈希(预留)
     */
    private String ipfsHash;

    /**
     * 区块链交易哈希(预留)
     */
    private String blockchainTxHash;

    /**
     * 状态: 0-有效 1-过期 2-吊销
     */
    private Integer status;

    /**
     * 签发时间
     */
    private Instant issuedAt;

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
    public VCRecord() {}

    public VCRecord(Long id, String vcId, String holderDid, String issuerDid, String vcType,
                    String vcData, String credentialHash, String signature, String ipfsHash,
                    String blockchainTxHash, Integer status, Instant issuedAt, Instant expiresAt,
                    Instant revokedAt, String revokeReason) {
        this.id = id;
        this.vcId = vcId;
        this.holderDid = holderDid;
        this.issuerDid = issuerDid;
        this.vcType = vcType;
        this.vcData = vcData;
        this.credentialHash = credentialHash;
        this.signature = signature;
        this.ipfsHash = ipfsHash;
        this.blockchainTxHash = blockchainTxHash;
        this.status = status;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
        this.revokeReason = revokeReason;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getVcId() { return vcId; }
    public void setVcId(String vcId) { this.vcId = vcId; }

    public String getHolderDid() { return holderDid; }
    public void setHolderDid(String holderDid) { this.holderDid = holderDid; }

    public String getIssuerDid() { return issuerDid; }
    public void setIssuerDid(String issuerDid) { this.issuerDid = issuerDid; }

    public String getVcType() { return vcType; }
    public void setVcType(String vcType) { this.vcType = vcType; }

    public String getVcData() { return vcData; }
    public void setVcData(String vcData) { this.vcData = vcData; }

    public String getCredentialHash() { return credentialHash; }
    public void setCredentialHash(String credentialHash) { this.credentialHash = credentialHash; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }

    public String getIpfsHash() { return ipfsHash; }
    public void setIpfsHash(String ipfsHash) { this.ipfsHash = ipfsHash; }

    public String getBlockchainTxHash() { return blockchainTxHash; }
    public void setBlockchainTxHash(String blockchainTxHash) { this.blockchainTxHash = blockchainTxHash; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Instant getIssuedAt() { return issuedAt; }
    public void setIssuedAt(Instant issuedAt) { this.issuedAt = issuedAt; }

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
        private String vcId;
        private String holderDid;
        private String issuerDid;
        private String vcType;
        private String vcData;
        private String credentialHash;
        private String signature;
        private String ipfsHash;
        private String blockchainTxHash;
        private Integer status;
        private Instant issuedAt;
        private Instant expiresAt;
        private Instant revokedAt;
        private String revokeReason;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder vcId(String vcId) { this.vcId = vcId; return this; }
        public Builder holderDid(String holderDid) { this.holderDid = holderDid; return this; }
        public Builder issuerDid(String issuerDid) { this.issuerDid = issuerDid; return this; }
        public Builder vcType(String vcType) { this.vcType = vcType; return this; }
        public Builder vcData(String vcData) { this.vcData = vcData; return this; }
        public Builder credentialHash(String credentialHash) { this.credentialHash = credentialHash; return this; }
        public Builder signature(String signature) { this.signature = signature; return this; }
        public Builder ipfsHash(String ipfsHash) { this.ipfsHash = ipfsHash; return this; }
        public Builder blockchainTxHash(String blockchainTxHash) { this.blockchainTxHash = blockchainTxHash; return this; }
        public Builder status(Integer status) { this.status = status; return this; }
        public Builder issuedAt(Instant issuedAt) { this.issuedAt = issuedAt; return this; }
        public Builder expiresAt(Instant expiresAt) { this.expiresAt = expiresAt; return this; }
        public Builder revokedAt(Instant revokedAt) { this.revokedAt = revokedAt; return this; }
        public Builder revokeReason(String revokeReason) { this.revokeReason = revokeReason; return this; }

        public VCRecord build() {
            return new VCRecord(id, vcId, holderDid, issuerDid, vcType, vcData, credentialHash,
                    signature, ipfsHash, blockchainTxHash, status, issuedAt, expiresAt, revokedAt, revokeReason);
        }
    }
}