package com.chainpass.payment.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 汇率实体
 */
@TableName("pay_exchange_rate")
public class ExchangeRate {

    /**
     * 源货币
     */
    private String fromCurrency;

    /**
     * 目标货币
     */
    private String toCurrency;

    /**
     * 汇率
     */
    private BigDecimal rate;

    /**
     * 数据来源: manual/api
     */
    private String source;

    /**
     * 更新时间
     */
    private Instant updatedAt;

    // Constructors
    public ExchangeRate() {}

    public ExchangeRate(String fromCurrency, String toCurrency, BigDecimal rate, String source, Instant updatedAt) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.rate = rate;
        this.source = source;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public String getFromCurrency() { return fromCurrency; }
    public void setFromCurrency(String fromCurrency) { this.fromCurrency = fromCurrency; }

    public String getToCurrency() { return toCurrency; }
    public void setToCurrency(String toCurrency) { this.toCurrency = toCurrency; }

    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    // Builder
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String fromCurrency;
        private String toCurrency;
        private BigDecimal rate;
        private String source;
        private Instant updatedAt;

        public Builder fromCurrency(String fromCurrency) { this.fromCurrency = fromCurrency; return this; }
        public Builder toCurrency(String toCurrency) { this.toCurrency = toCurrency; return this; }
        public Builder rate(BigDecimal rate) { this.rate = rate; return this; }
        public Builder source(String source) { this.source = source; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public ExchangeRate build() {
            return new ExchangeRate(fromCurrency, toCurrency, rate, source, updatedAt);
        }
    }
}