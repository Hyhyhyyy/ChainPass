package com.chainpass.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/** Cross-border sandbox order plus its deterministic compliance decision. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("pay_order")
public class PaymentOrder {
    @TableId(type = IdType.AUTO)
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
    private String sourceCountry;
    private String targetCountry;
    private String beneficiaryName;
    private String vcRequired;
    private Integer vcVerified;
    private Integer kycRequired;
    private Integer kycVerified;
    private Integer riskScore;
    private String riskLevel;
    private String complianceDecision;
    private String complianceReasons;
    private Long reviewedBy;
    private Instant reviewedAt;
    /** 0 pending, 1 processing, 2 success, 3 failed, 4 refunded, 5 review, 6 rejected. */
    private Integer status;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant paidAt;
    private Instant expiredAt;
}
