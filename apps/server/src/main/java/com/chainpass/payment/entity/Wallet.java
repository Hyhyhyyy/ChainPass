package com.chainpass.payment.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 钱包实体
 * 使用乐观锁防止并发余额冲突
 */
@TableName("pay_wallet")
public class Wallet {

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
     * 钱包地址
     */
    private String address;

    /**
     * 人民币余额
     */
    private BigDecimal balanceCny;

    /**
     * 美元余额
     */
    private BigDecimal balanceUsd;

    /**
     * ETH余额
     */
    private BigDecimal balanceEth;

    /**
     * 冻结人民币
     */
    private BigDecimal frozenCny;

    /**
     * 冻结美元
     */
    private BigDecimal frozenUsd;

    /**
     * 状态: 0-正常 1-冻结 2-注销
     */
    private Integer status;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;

    /**
     * 创建时间
     */
    private Instant createdAt;

    /**
     * 更新时间
     */
    private Instant updatedAt;

    // Constructors
    public Wallet() {}

    public Wallet(Long id, Long userId, String did, String address, BigDecimal balanceCny,
                  BigDecimal balanceUsd, BigDecimal balanceEth, BigDecimal frozenCny,
                  BigDecimal frozenUsd, Integer status, Integer version, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.did = did;
        this.address = address;
        this.balanceCny = balanceCny;
        this.balanceUsd = balanceUsd;
        this.balanceEth = balanceEth;
        this.frozenCny = frozenCny;
        this.frozenUsd = frozenUsd;
        this.status = status;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getDid() { return did; }
    public void setDid(String did) { this.did = did; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public BigDecimal getBalanceCny() { return balanceCny; }
    public void setBalanceCny(BigDecimal balanceCny) { this.balanceCny = balanceCny; }

    public BigDecimal getBalanceUsd() { return balanceUsd; }
    public void setBalanceUsd(BigDecimal balanceUsd) { this.balanceUsd = balanceUsd; }

    public BigDecimal getBalanceEth() { return balanceEth; }
    public void setBalanceEth(BigDecimal balanceEth) { this.balanceEth = balanceEth; }

    public BigDecimal getFrozenCny() { return frozenCny; }
    public void setFrozenCny(BigDecimal frozenCny) { this.frozenCny = frozenCny; }

    public BigDecimal getFrozenUsd() { return frozenUsd; }
    public void setFrozenUsd(BigDecimal frozenUsd) { this.frozenUsd = frozenUsd; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    // Builder
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Long userId;
        private String did;
        private String address;
        private BigDecimal balanceCny;
        private BigDecimal balanceUsd;
        private BigDecimal balanceEth;
        private BigDecimal frozenCny;
        private BigDecimal frozenUsd;
        private Integer status;
        private Integer version = 0; // 默认版本号
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder did(String did) { this.did = did; return this; }
        public Builder address(String address) { this.address = address; return this; }
        public Builder balanceCny(BigDecimal balanceCny) { this.balanceCny = balanceCny; return this; }
        public Builder balanceUsd(BigDecimal balanceUsd) { this.balanceUsd = balanceUsd; return this; }
        public Builder balanceEth(BigDecimal balanceEth) { this.balanceEth = balanceEth; return this; }
        public Builder frozenCny(BigDecimal frozenCny) { this.frozenCny = frozenCny; return this; }
        public Builder frozenUsd(BigDecimal frozenUsd) { this.frozenUsd = frozenUsd; return this; }
        public Builder status(Integer status) { this.status = status; return this; }
        public Builder version(Integer version) { this.version = version; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public Wallet build() {
            return new Wallet(id, userId, did, address, balanceCny, balanceUsd, balanceEth,
                    frozenCny, frozenUsd, status, version, createdAt, updatedAt);
        }
    }
}