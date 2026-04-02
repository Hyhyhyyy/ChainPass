package com.chainpass.payment.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 支付交易记录实体
 */
@TableName("pay_transaction")
public class Transaction {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 区块链交易哈希(预留)
     */
    private String txHash;

    /**
     * 支付网关: wallet/mock/stripe/alipay/crypto
     */
    private String gateway;

    /**
     * 网关交易ID
     */
    private String gatewayTxId;

    /**
     * 付款地址
     */
    private String fromAddress;

    /**
     * 收款地址
     */
    private String toAddress;

    /**
     * 交易金额
     */
    private BigDecimal amount;

    /**
     * 货币
     */
    private String currency;

    /**
     * 手续费
     */
    private BigDecimal feeAmount;

    /**
     * 状态: 0-处理中 1-成功 2-失败
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private Instant createdAt;

    /**
     * 确认时间
     */
    private Instant confirmedAt;

    // Constructors
    public Transaction() {}

    public Transaction(Long id, String orderNo, String txHash, String gateway, String gatewayTxId,
                       String fromAddress, String toAddress, BigDecimal amount, String currency,
                       BigDecimal feeAmount, Integer status, String errorMessage, Instant createdAt,
                       Instant confirmedAt) {
        this.id = id;
        this.orderNo = orderNo;
        this.txHash = txHash;
        this.gateway = gateway;
        this.gatewayTxId = gatewayTxId;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.amount = amount;
        this.currency = currency;
        this.feeAmount = feeAmount;
        this.status = status;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.confirmedAt = confirmedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public String getTxHash() { return txHash; }
    public void setTxHash(String txHash) { this.txHash = txHash; }

    public String getGateway() { return gateway; }
    public void setGateway(String gateway) { this.gateway = gateway; }

    public String getGatewayTxId() { return gatewayTxId; }
    public void setGatewayTxId(String gatewayTxId) { this.gatewayTxId = gatewayTxId; }

    public String getFromAddress() { return fromAddress; }
    public void setFromAddress(String fromAddress) { this.fromAddress = fromAddress; }

    public String getToAddress() { return toAddress; }
    public void setToAddress(String toAddress) { this.toAddress = toAddress; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getFeeAmount() { return feeAmount; }
    public void setFeeAmount(BigDecimal feeAmount) { this.feeAmount = feeAmount; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(Instant confirmedAt) { this.confirmedAt = confirmedAt; }

    // Builder
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String orderNo;
        private String txHash;
        private String gateway;
        private String gatewayTxId;
        private String fromAddress;
        private String toAddress;
        private BigDecimal amount;
        private String currency;
        private BigDecimal feeAmount;
        private Integer status;
        private String errorMessage;
        private Instant createdAt;
        private Instant confirmedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder orderNo(String orderNo) { this.orderNo = orderNo; return this; }
        public Builder txHash(String txHash) { this.txHash = txHash; return this; }
        public Builder gateway(String gateway) { this.gateway = gateway; return this; }
        public Builder gatewayTxId(String gatewayTxId) { this.gatewayTxId = gatewayTxId; return this; }
        public Builder fromAddress(String fromAddress) { this.fromAddress = fromAddress; return this; }
        public Builder toAddress(String toAddress) { this.toAddress = toAddress; return this; }
        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder feeAmount(BigDecimal feeAmount) { this.feeAmount = feeAmount; return this; }
        public Builder status(Integer status) { this.status = status; return this; }
        public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder confirmedAt(Instant confirmedAt) { this.confirmedAt = confirmedAt; return this; }

        public Transaction build() {
            return new Transaction(id, orderNo, txHash, gateway, gatewayTxId, fromAddress, toAddress,
                    amount, currency, feeAmount, status, errorMessage, createdAt, confirmedAt);
        }
    }
}