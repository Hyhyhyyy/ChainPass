package com.chainpass.payment.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * 支付相关DTO
 */
public class PaymentDto {

    /**
     * 创建支付订单请求
     */
    public static class CreatePaymentRequest {
        /**
         * 收款人DID
         */
        @NotBlank(message = "收款人DID不能为空")
        private String payeeDid;

        /**
         * 支付金额
         */
        @NotNull(message = "金额不能为空")
        @Positive(message = "金额必须大于0")
        private BigDecimal amount;

        /**
         * 支付货币: CNY/USD/ETH
         */
        @NotBlank(message = "货币类型不能为空")
        private String currency;

        /**
         * 目标货币（用于跨境支付）
         */
        private String targetCurrency;

        /**
         * 支付方式: wallet/mock
         */
        private String paymentMethod = "wallet";

        /**
         * 支付描述
         */
        private String description;

        public CreatePaymentRequest() {}

        // Getters and Setters
        public String getPayeeDid() { return payeeDid; }
        public void setPayeeDid(String payeeDid) { this.payeeDid = payeeDid; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }

        public String getTargetCurrency() { return targetCurrency; }
        public void setTargetCurrency(String targetCurrency) { this.targetCurrency = targetCurrency; }

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    /**
     * 支付订单响应
     */
    public static class PaymentOrderResponse {
        private String orderNo;
        private String payerDid;
        private String payeeDid;
        private BigDecimal amount;
        private String currency;
        private BigDecimal originalAmount;
        private String originalCurrency;
        private BigDecimal exchangeRate;
        private BigDecimal feeAmount;
        private String status;
        private String createdAt;
        private String paidAt;

        public PaymentOrderResponse() {}

        // Getters and Setters
        public String getOrderNo() { return orderNo; }
        public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

        public String getPayerDid() { return payerDid; }
        public void setPayerDid(String payerDid) { this.payerDid = payerDid; }

        public String getPayeeDid() { return payeeDid; }
        public void setPayeeDid(String payeeDid) { this.payeeDid = payeeDid; }

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

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

        public String getPaidAt() { return paidAt; }
        public void setPaidAt(String paidAt) { this.paidAt = paidAt; }
    }

    /**
     * 钱包响应
     */
    public static class WalletResponse {
        private Long id;
        private String did;
        private String address;
        private BigDecimal balanceCny;
        private BigDecimal balanceUsd;
        private BigDecimal balanceEth;
        private BigDecimal totalBalanceCny;
        private String status;

        public WalletResponse() {}

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

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

        public BigDecimal getTotalBalanceCny() { return totalBalanceCny; }
        public void setTotalBalanceCny(BigDecimal totalBalanceCny) { this.totalBalanceCny = totalBalanceCny; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    /**
     * 交易历史响应
     */
    public static class TransactionResponse {
        private String orderNo;
        private String type; // IN/OUT
        private String counterpartyDid;
        private BigDecimal amount;
        private String currency;
        private String status;
        private String description;
        private String createdAt;

        public TransactionResponse() {}

        // Getters and Setters
        public String getOrderNo() { return orderNo; }
        public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getCounterpartyDid() { return counterpartyDid; }
        public void setCounterpartyDid(String counterpartyDid) { this.counterpartyDid = counterpartyDid; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }

    /**
     * 交易历史分页响应
     */
    public static class TransactionPageResponse {
        private List<TransactionResponse> list;
        private int total;
        private int page;
        private int size;
        private int totalPages;

        public TransactionPageResponse() {}

        // Getters and Setters
        public List<TransactionResponse> getList() { return list; }
        public void setList(List<TransactionResponse> list) { this.list = list; }

        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }

        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }

        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }

        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    }

    /**
     * 汇率响应
     */
    public static class ExchangeRateResponse {
        private String fromCurrency;
        private String toCurrency;
        private BigDecimal rate;
        private List<CurrencyInfo> availableCurrencies;

        public ExchangeRateResponse() {}

        // Getters and Setters
        public String getFromCurrency() { return fromCurrency; }
        public void setFromCurrency(String fromCurrency) { this.fromCurrency = fromCurrency; }

        public String getToCurrency() { return toCurrency; }
        public void setToCurrency(String toCurrency) { this.toCurrency = toCurrency; }

        public BigDecimal getRate() { return rate; }
        public void setRate(BigDecimal rate) { this.rate = rate; }

        public List<CurrencyInfo> getAvailableCurrencies() { return availableCurrencies; }
        public void setAvailableCurrencies(List<CurrencyInfo> availableCurrencies) { this.availableCurrencies = availableCurrencies; }
    }

    public static class CurrencyInfo {
        private String code;
        private String name;
        private String symbol;

        public CurrencyInfo() {}

        // Getters and Setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
    }
}