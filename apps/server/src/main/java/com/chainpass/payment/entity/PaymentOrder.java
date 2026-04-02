package com.chainpass.payment.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 支付订单实体
 */
@TableName("pay_order")
public class PaymentOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 付款人DID
     */
    private String payerDid;

    /**
     * 收款人DID
     */
    private String payeeDid;

    /**
     * 付款人钱包ID
     */
    private Long payerWalletId;

    /**
     * 收款人钱包ID
     */
    private Long payeeWalletId;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付货币: CNY/USD/ETH
     */
    private String currency;

    /**
     * 原始金额
     */
    private BigDecimal originalAmount;

    /**
     * 原始货币
     */
    private String originalCurrency;

    /**
     * 汇率
     */
    private BigDecimal exchangeRate;

    /**
     * 手续费
     */
    private BigDecimal feeAmount;

    /**
     * 手续费币种
     */
    private String feeCurrency;

    /**
     * 支付方式: wallet/stripe/alipay/crypto/mock
     */
    private String paymentMethod;

    /**
     * 支付目的
     */
    private String paymentPurpose;

    /**
     * 支付描述
     */
    private String description;

    /**
     * 需要的VC类型
     */
    private String vcRequired;

    /**
     * VC验证状态
     */
    private Integer vcVerified;

    /**
     * 是否需要KYC
     */
    private Integer kycRequired;

    /**
     * KYC验证状态
     */
    private Integer kycVerified;

    /**
     * 风控评分(0-100)
     */
    private Integer riskScore;

    /**
     * 风险等级: LOW/MEDIUM/HIGH
     */
    private String riskLevel;

    /**
     * 状态: 0-待支付 1-处理中 2-成功 3-失败 4-已退款
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
     * 支付时间
     */
    private Instant paidAt;

    /**
     * 订单过期时间
     */
    private Instant expiredAt;

    // Constructors
    public PaymentOrder() {}

    public PaymentOrder(Long id, String orderNo, String payerDid, String payeeDid, Long payerWalletId,
                        Long payeeWalletId, BigDecimal amount, String currency, BigDecimal originalAmount,
                        String originalCurrency, BigDecimal exchangeRate, BigDecimal feeAmount,
                        String feeCurrency, String paymentMethod, String paymentPurpose, String description,
                        String vcRequired, Integer vcVerified, Integer kycRequired, Integer kycVerified,
                        Integer riskScore, String riskLevel, Integer status, Instant createdAt,
                        Instant updatedAt, Instant paidAt, Instant expiredAt) {
        this.id = id;
        this.orderNo = orderNo;
        this.payerDid = payerDid;
        this.payeeDid = payeeDid;
        this.payerWalletId = payerWalletId;
        this.payeeWalletId = payeeWalletId;
        this.amount = amount;
        this.currency = currency;
        this.originalAmount = originalAmount;
        this.originalCurrency = originalCurrency;
        this.exchangeRate = exchangeRate;
        this.feeAmount = feeAmount;
        this.feeCurrency = feeCurrency;
        this.paymentMethod = paymentMethod;
        this.paymentPurpose = paymentPurpose;
        this.description = description;
        this.vcRequired = vcRequired;
        this.vcVerified = vcVerified;
        this.kycRequired = kycRequired;
        this.kycVerified = kycVerified;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.paidAt = paidAt;
        this.expiredAt = expiredAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public String getPayerDid() { return payerDid; }
    public void setPayerDid(String payerDid) { this.payerDid = payerDid; }

    public String getPayeeDid() { return payeeDid; }
    public void setPayeeDid(String payeeDid) { this.payeeDid = payeeDid; }

    public Long getPayerWalletId() { return payerWalletId; }
    public void setPayerWalletId(Long payerWalletId) { this.payerWalletId = payerWalletId; }

    public Long getPayeeWalletId() { return payeeWalletId; }
    public void setPayeeWalletId(Long payeeWalletId) { this.payeeWalletId = payeeWalletId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getOriginalAmount() { return originalAmount; }
    public void setOriginalAmount(BigDecimal originalAmount) { this.originalAmount = originalAmount; }

    public String getOriginalCurrency() { return originalCurrency; }
    public void setOriginalCurrency(String originalCurrency) { this.originalCurrency = originalCurrency; }

    public BigDecimal getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }

    public BigDecimal getFeeAmount() { return feeAmount; }
    public void setFeeAmount(BigDecimal feeAmount) { this.feeAmount = feeAmount; }

    public String getFeeCurrency() { return feeCurrency; }
    public void setFeeCurrency(String feeCurrency) { this.feeCurrency = feeCurrency; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentPurpose() { return paymentPurpose; }
    public void setPaymentPurpose(String paymentPurpose) { this.paymentPurpose = paymentPurpose; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getVcRequired() { return vcRequired; }
    public void setVcRequired(String vcRequired) { this.vcRequired = vcRequired; }

    public Integer getVcVerified() { return vcVerified; }
    public void setVcVerified(Integer vcVerified) { this.vcVerified = vcVerified; }

    public Integer getKycRequired() { return kycRequired; }
    public void setKycRequired(Integer kycRequired) { this.kycRequired = kycRequired; }

    public Integer getKycVerified() { return kycVerified; }
    public void setKycVerified(Integer kycVerified) { this.kycVerified = kycVerified; }

    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }

    public Instant getExpiredAt() { return expiredAt; }
    public void setExpiredAt(Instant expiredAt) { this.expiredAt = expiredAt; }

    // Builder
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String orderNo;
        private String payerDid;
        private String payeeDid;
        private Long payerWalletId;
        private Long payeeWalletId;
        private BigDecimal amount;
        private String currency;
        private BigDecimal originalAmount;
        private String originalCurrency;
        private BigDecimal exchangeRate;
        private BigDecimal feeAmount;
        private String feeCurrency;
        private String paymentMethod;
        private String paymentPurpose;
        private String description;
        private String vcRequired;
        private Integer vcVerified;
        private Integer kycRequired;
        private Integer kycVerified;
        private Integer riskScore;
        private String riskLevel;
        private Integer status;
        private Instant createdAt;
        private Instant updatedAt;
        private Instant paidAt;
        private Instant expiredAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder orderNo(String orderNo) { this.orderNo = orderNo; return this; }
        public Builder payerDid(String payerDid) { this.payerDid = payerDid; return this; }
        public Builder payeeDid(String payeeDid) { this.payeeDid = payeeDid; return this; }
        public Builder payerWalletId(Long payerWalletId) { this.payerWalletId = payerWalletId; return this; }
        public Builder payeeWalletId(Long payeeWalletId) { this.payeeWalletId = payeeWalletId; return this; }
        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder originalAmount(BigDecimal originalAmount) { this.originalAmount = originalAmount; return this; }
        public Builder originalCurrency(String originalCurrency) { this.originalCurrency = originalCurrency; return this; }
        public Builder exchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; return this; }
        public Builder feeAmount(BigDecimal feeAmount) { this.feeAmount = feeAmount; return this; }
        public Builder feeCurrency(String feeCurrency) { this.feeCurrency = feeCurrency; return this; }
        public Builder paymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; return this; }
        public Builder paymentPurpose(String paymentPurpose) { this.paymentPurpose = paymentPurpose; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder vcRequired(String vcRequired) { this.vcRequired = vcRequired; return this; }
        public Builder vcVerified(Integer vcVerified) { this.vcVerified = vcVerified; return this; }
        public Builder kycRequired(Integer kycRequired) { this.kycRequired = kycRequired; return this; }
        public Builder kycVerified(Integer kycVerified) { this.kycVerified = kycVerified; return this; }
        public Builder riskScore(Integer riskScore) { this.riskScore = riskScore; return this; }
        public Builder riskLevel(String riskLevel) { this.riskLevel = riskLevel; return this; }
        public Builder status(Integer status) { this.status = status; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder paidAt(Instant paidAt) { this.paidAt = paidAt; return this; }
        public Builder expiredAt(Instant expiredAt) { this.expiredAt = expiredAt; return this; }

        public PaymentOrder build() {
            return new PaymentOrder(id, orderNo, payerDid, payeeDid, payerWalletId, payeeWalletId,
                    amount, currency, originalAmount, originalCurrency, exchangeRate, feeAmount,
                    feeCurrency, paymentMethod, paymentPurpose, description, vcRequired, vcVerified,
                    kycRequired, kycVerified, riskScore, riskLevel, status, createdAt, updatedAt, paidAt, expiredAt);
        }
    }
}