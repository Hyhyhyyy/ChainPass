package com.chainpass.payment.service;

import com.chainpass.did.service.DIDService;
import com.chainpass.compliance.kyc.KYCService;
import com.chainpass.vc.service.VCService;
import com.chainpass.exception.BusinessException;
import com.chainpass.payment.dto.PaymentDto;
import com.chainpass.payment.entity.*;
import com.chainpass.payment.mapper.*;
import com.chainpass.util.RedisCache;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 沙盒多币种内部账本服务（不连接银行、支付机构或区块链）。
 *
 * 使用乐观锁防止并发余额冲突
 * 支持汇率缓存
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final WalletMapper walletMapper;
    private final PaymentOrderMapper orderMapper;
    private final TransactionMapper transactionMapper;
    private final ExchangeRateMapper rateMapper;
    private final DIDService didService;
    private final KYCService kycService;
    private final VCService vcService;
    private final RedisCache redisCache;

    @Value("${chainpass.payment.sandbox-credits-enabled:false}")
    private boolean sandboxCreditsEnabled;

    // 手续费率
    private static final BigDecimal FEE_RATE = new BigDecimal("0.001"); // 0.1%

    // 乐观锁重试次数
    private static final int MAX_RETRY = 3;

    // 汇率缓存时间：1小时
    private static final long RATE_CACHE_TTL = 60 * 60 * 1000;
    private static final String RATE_CACHE_PREFIX = "exchange:rate:";
    private static final Set<String> SUPPORTED_COUNTRIES = Set.of(
        "CN", "US", "SG", "GB", "JP", "KR", "HK", "DE", "FR", "AU", "CA");
    private static final Set<String> PAYMENT_PURPOSES = Set.of(
        "GOODS_SERVICES", "EDUCATION", "FAMILY_SUPPORT", "TRAVEL", "BUSINESS");

    /**
     * 获取或创建用户钱包
     */
    @Transactional
    public Wallet getOrCreateWallet(Long userId, String did) {
        Wallet wallet = walletMapper.findByUserId(userId);
        if (wallet == null) {
            wallet = createWallet(userId, did);
        }
        return wallet;
    }

    /**
     * 创建钱包
     */
    @Transactional
    public Wallet createWallet(Long userId, String did) {
        log.info("Creating wallet for user: {}, did: {}", userId, did);

        // 检查是否已存在
        if (walletMapper.findByUserId(userId) != null) {
            throw new BusinessException("用户已拥有钱包");
        }

        // 生成钱包地址
        byte[] addressBytes = new byte[20];
        new java.security.SecureRandom().nextBytes(addressBytes);
        String address = "0x" + java.util.HexFormat.of().formatHex(addressBytes);

        Wallet wallet = Wallet.builder()
            .userId(userId)
            .did(did)
            .address(address)
            .balanceCny(BigDecimal.ZERO)
            .balanceUsd(BigDecimal.ZERO)
            .balanceEth(BigDecimal.ZERO)
            .frozenCny(BigDecimal.ZERO)
            .frozenUsd(BigDecimal.ZERO)
            .status(0)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        walletMapper.insert(wallet);

        if (sandboxCreditsEnabled) {
            initSandboxBalance(wallet.getId());
        }

        return wallet;
    }

    /**
     * 仅开发配置可启用的沙盒额度；它不是法币或链上资产。
     */
    private void initSandboxBalance(Long walletId) {
        walletMapper.addCnyBalance(walletId, new BigDecimal("10000.00"));
        walletMapper.addUsdBalance(walletId, new BigDecimal("1500.00"));
        walletMapper.addEthBalance(walletId, new BigDecimal("0.5"));
    }

    /**
     * 创建沙盒账本转账订单
     */
    @Transactional
    public PaymentOrder createPayment(String payerDid, PaymentDto.CreatePaymentRequest request) {
        log.info("Creating payment: payer={}, payee={}, amount={} {}",
            payerDid, request.getPayeeDid(), request.getAmount(), request.getCurrency());

        request.setCurrency(request.getCurrency().toUpperCase(Locale.ROOT));
        if (request.getTargetCurrency() != null) {
            request.setTargetCurrency(request.getTargetCurrency().toUpperCase(Locale.ROOT));
        }
        request.setSourceCountry(request.getSourceCountry().toUpperCase(Locale.ROOT));
        request.setTargetCountry(request.getTargetCountry().toUpperCase(Locale.ROOT));
        request.setPaymentPurpose(request.getPaymentPurpose().toUpperCase(Locale.ROOT));
        Set<String> supportedCurrencies = Set.of("CNY", "USD", "ETH");
        if (!supportedCurrencies.contains(request.getCurrency()) ||
            (request.getTargetCurrency() != null && !supportedCurrencies.contains(request.getTargetCurrency()))) {
            throw new BusinessException("仅支持CNY、USD和ETH沙盒记账单位");
        }
        if (request.getPaymentMethod() != null && !"wallet".equals(request.getPaymentMethod())) {
            throw new BusinessException("仅支持内部wallet记账方式");
        }
        if (payerDid.equals(request.getPayeeDid())) {
            throw new BusinessException("不能向自己的钱包转账");
        }
        if (!SUPPORTED_COUNTRIES.contains(request.getSourceCountry()) ||
            !SUPPORTED_COUNTRIES.contains(request.getTargetCountry())) {
            throw new BusinessException("当前沙盒未开放该国家或地区走廊");
        }
        if (request.getSourceCountry().equals(request.getTargetCountry())) {
            throw new BusinessException("跨境订单的汇出地和收款地不能相同");
        }
        if (!PAYMENT_PURPOSES.contains(request.getPaymentPurpose())) {
            throw new BusinessException("不支持的跨境支付用途");
        }

        // 1. 验证付款人DID
        if (!didService.isValidDID(payerDid)) {
            throw new BusinessException("付款人DID无效");
        }

        // 2. 验证收款人DID
        if (!didService.isValidDID(request.getPayeeDid())) {
            throw new BusinessException("收款人DID无效");
        }

        // 3. 获取钱包
        Wallet payerWallet = walletMapper.findByDid(payerDid);
        if (payerWallet == null) {
            throw new BusinessException("付款人钱包不存在");
        }

        Wallet payeeWallet = walletMapper.findByDid(request.getPayeeDid());
        if (payeeWallet == null) {
            throw new BusinessException("收款人尚未初始化沙盒钱包");
        }

        // 4. 汇率转换（如果需要）
        BigDecimal finalAmount = request.getAmount();
        String finalCurrency = request.getCurrency();
        BigDecimal exchangeRate = null;
        BigDecimal originalAmount = request.getAmount();
        String originalCurrency = request.getCurrency();

        if (request.getTargetCurrency() != null &&
            !request.getCurrency().equals(request.getTargetCurrency())) {

            exchangeRate = getExchangeRate(request.getCurrency(), request.getTargetCurrency());
            if (exchangeRate == null) {
                throw new BusinessException("不支持的货币兑换");
            }

            finalAmount = request.getAmount().multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
            finalCurrency = request.getTargetCurrency();
        }

        // 5. 计算手续费
        int feeScale = "ETH".equals(originalCurrency) ? 8 : 2;
        BigDecimal feeAmount = originalAmount.multiply(FEE_RATE).setScale(feeScale, RoundingMode.HALF_UP);

        // 6. 检查余额
        if (!checkBalance(payerWallet, originalAmount.add(feeAmount), originalCurrency)) {
            throw new BusinessException("余额不足");
        }

        ComplianceDecision compliance = evaluateCompliance(payerDid, request, originalAmount, originalCurrency);

        // 7. 创建订单；待复核和拒绝订单同样落库，形成可追溯的合规决策记录
        String orderNo = generateOrderNo();
        PaymentOrder order = PaymentOrder.builder()
            .orderNo(orderNo)
            .payerDid(payerDid)
            .payeeDid(request.getPayeeDid())
            .payerWalletId(payerWallet.getId())
            .payeeWalletId(payeeWallet.getId())
            .amount(finalAmount)
            .currency(finalCurrency)
            .originalAmount(originalAmount)
            .originalCurrency(originalCurrency)
            .exchangeRate(exchangeRate)
            .feeAmount(feeAmount)
            .feeCurrency(originalCurrency)
            .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "wallet")
            .paymentPurpose(request.getPaymentPurpose())
            .description(request.getDescription())
            .sourceCountry(request.getSourceCountry())
            .targetCountry(request.getTargetCountry())
            .beneficiaryName(request.getBeneficiaryName().trim())
            .vcRequired("KYCCredential")
            .vcVerified(compliance.payerKycVerified() ? 1 : 0)
            .kycRequired(1)
            .kycVerified(compliance.payerKycVerified() ? 1 : 0)
            .riskScore(compliance.score())
            .riskLevel(compliance.riskLevel())
            .complianceDecision(compliance.decision())
            .complianceReasons(String.join("；", compliance.reasons()))
            .status(switch (compliance.decision()) {
                case "APPROVED" -> 0;
                case "REVIEW" -> 5;
                default -> 6;
            })
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .expiredAt(Instant.now().plus(30, ChronoUnit.MINUTES))
            .build();

        orderMapper.insert(order);

        log.info("Payment order created: {}", orderNo);
        return order;
    }

    private ComplianceDecision evaluateCompliance(String payerDid,
                                                    PaymentDto.CreatePaymentRequest request,
                                                    BigDecimal amount,
                                                    String currency) {
        List<String> reasons = new ArrayList<>();
        int score = 0;
        boolean payerKyc = kycService.isDIDKYCVerified(payerDid)
            && vcService.hasValidCredential(payerDid, "KYCCredential");
        boolean payeeKyc = kycService.isDIDKYCVerified(request.getPayeeDid())
            && vcService.hasValidCredential(request.getPayeeDid(), "KYCCredential");

        if (!payerKyc) {
            score = 100;
            reasons.add("付款方缺少有效的人工审核结论凭证");
        }
        if (!payeeKyc) {
            score += 25;
            reasons.add("收款方尚无有效审核结论，需增强复核");
        } else if (!kycService.isBeneficiaryNameConsistent(
                request.getPayeeDid(), request.getBeneficiaryName())) {
            score += 50;
            reasons.add("受益人姓名与收款方已审核身份不一致");
        }

        BigDecimal cnyAmount = amount;
        if (!"CNY".equals(currency)) {
            BigDecimal cnyRate = getExchangeRate(currency, "CNY");
            if (cnyRate == null) {
                throw new BusinessException("缺少用于合规计价的CNY换算汇率");
            }
            cnyAmount = amount.multiply(cnyRate);
        }
        if (cnyAmount.compareTo(new BigDecimal("50000")) >= 0) {
            score += 60;
            reasons.add("沙盒等值金额达到高额复核阈值");
        } else if (cnyAmount.compareTo(new BigDecimal("10000")) >= 0) {
            score += 35;
            reasons.add("沙盒等值金额达到增强审查阈值");
        }
        if ("ETH".equals(currency)) {
            score += 20;
            reasons.add("使用波动性记账单位");
        }
        if ("BUSINESS".equals(request.getPaymentPurpose())) {
            score += 10;
            reasons.add("商业用途需要保留贸易背景材料");
        }

        score = Math.min(score, 100);
        String decision = !payerKyc || score >= 80 ? "REJECTED" : score >= 40 ? "REVIEW" : "APPROVED";
        String riskLevel = score >= 80 ? "HIGH" : score >= 40 ? "MEDIUM" : "LOW";
        if (reasons.isEmpty()) {
            reasons.add("身份、走廊、用途和金额规则均通过");
        }
        return new ComplianceDecision(score, riskLevel, decision, reasons, payerKyc);
    }

    private record ComplianceDecision(int score, String riskLevel, String decision,
                                      List<String> reasons, boolean payerKycVerified) {}

    /**
     * 执行支付 - 使用乐观锁确保并发安全
     */
    @Transactional
    public Transaction executePayment(String orderNo, String requesterDid) {
        log.info("Executing payment: {}", orderNo);

        // 1. 获取订单
        PaymentOrder order = orderMapper.findByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getPayerDid().equals(requesterDid)) {
            throw new BusinessException("无权执行他人的支付订单");
        }

        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态不正确");
        }

        // 2. 检查过期
        if (order.getExpiredAt().isBefore(Instant.now())) {
            orderMapper.updateStatus(orderNo, 3); // 失败
            throw new BusinessException("订单已过期");
        }

        // 3. 以条件更新抢占订单，避免两个并发请求重复扣款
        if (orderMapper.markProcessing(orderNo) != 1) {
            throw new BusinessException("订单正在处理或已被处理");
        }

        try {
            // 4. 执行转账 - 使用乐观锁
            Wallet payerWallet = walletMapper.selectById(order.getPayerWalletId());
            if (payerWallet == null) {
                throw new BusinessException("付款人钱包不存在");
            }

            Wallet payeeWallet = walletMapper.selectById(order.getPayeeWalletId());
            if (payeeWallet == null) {
                throw new BusinessException("收款人钱包不存在");
            }

            // 扣除付款人余额（含手续费）- 使用乐观锁
            BigDecimal totalDeduct = order.getOriginalAmount().add(order.getFeeAmount());
            boolean deductSuccess = deductBalanceWithRetry(
                payerWallet, totalDeduct, order.getOriginalCurrency());
            if (!deductSuccess) {
                throw new BusinessException("余额扣除失败，请重试");
            }

            // 增加收款人余额 - 使用乐观锁
            boolean addSuccess = addBalanceWithRetry(payeeWallet, order.getAmount(), order.getCurrency());
            if (!addSuccess) {
                // 理论上不应该发生，但做保护性处理
                log.error("Failed to add balance to payee wallet, rolling back...");
                throw new BusinessException("支付执行失败，请联系客服");
            }

            // 5. 创建交易记录
            String txHash = "tx_" + UUID.randomUUID().toString().replace("-", "");
            Transaction transaction = Transaction.builder()
                .orderNo(orderNo)
                .txHash(txHash)
                .gateway("internal-ledger")
                .gatewayTxId("ledger_" + UUID.randomUUID().toString().replace("-", ""))
                .fromAddress(payerWallet.getAddress())
                .toAddress(payeeWallet.getAddress())
                .amount(order.getAmount())
                .currency(order.getCurrency())
                .feeAmount(order.getFeeAmount())
                .status(1) // 成功
                .createdAt(Instant.now())
                .confirmedAt(Instant.now())
                .build();

            transactionMapper.insert(transaction);

            // 6. 更新订单状态
            if (orderMapper.markPaid(orderNo, Instant.now()) != 1) {
                throw new BusinessException("订单状态更新失败，转账已回滚");
            }

            log.info("Payment executed successfully with optimistic lock: {}", orderNo);
            return transaction;

        } catch (BusinessException e) {
            // 业务异常，回滚订单状态
            orderMapper.updateStatus(orderNo, 3); // 失败
            throw e;
        } catch (Exception e) {
            log.error("Payment execution failed unexpectedly", e);
            orderMapper.updateStatus(orderNo, 3);
            throw new BusinessException("支付执行失败: " + e.getMessage());
        }
    }

    public List<PaymentOrder> getComplianceReviewQueue() {
        return orderMapper.findComplianceReviewQueue();
    }

    public PaymentOrder getOwnedOrder(String orderNo, String requesterDid) {
        PaymentOrder order = orderMapper.findByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getPayerDid().equals(requesterDid)) {
            throw new BusinessException("无权查看他人的支付订单");
        }
        return order;
    }

    @Transactional
    public void approveComplianceReview(String orderNo, Long reviewerId, String note) {
        if (note == null || note.isBlank()) {
            throw new BusinessException("复核意见不能为空");
        }
        if (orderMapper.approveComplianceReview(orderNo, reviewerId, Instant.now(), note.trim()) != 1) {
            throw new BusinessException("订单不存在或不在待复核状态");
        }
    }

    @Transactional
    public void rejectComplianceReview(String orderNo, Long reviewerId, String note) {
        if (note == null || note.isBlank()) {
            throw new BusinessException("拒绝原因不能为空");
        }
        if (orderMapper.rejectComplianceReview(orderNo, reviewerId, Instant.now(), note.trim()) != 1) {
            throw new BusinessException("订单不存在或不在待复核状态");
        }
    }

    /**
     * 获取汇率（带缓存）
     */
    public BigDecimal getExchangeRate(String from, String to) {
        if (from.equals(to)) {
            return BigDecimal.ONE;
        }

        // 尝试从缓存获取
        String cacheKey = RATE_CACHE_PREFIX + from + ":" + to;
        BigDecimal cached = redisCache.getCacheObject(cacheKey, BigDecimal.class);
        if (cached != null) {
            return cached;
        }

        // 从数据库查询
        BigDecimal rate = rateMapper.getRate(from, to);
        if (rate != null) {
            // 写入缓存
            redisCache.setCacheObject(cacheKey, rate, RATE_CACHE_TTL, TimeUnit.MILLISECONDS);
        }

        return rate;
    }

    /**
     * 获取交易历史（双向：包括收入和支出）
     */
    public List<PaymentDto.TransactionResponse> getTransactionHistory(String did) {
        // 查询作为付款人和收款人的所有订单
        List<PaymentOrder> orders = orderMapper.findByDidWithPagination(did, 100, 0);
        List<PaymentDto.TransactionResponse> responses = new ArrayList<>();

        for (PaymentOrder order : orders) {
            PaymentDto.TransactionResponse response = new PaymentDto.TransactionResponse();
            response.setOrderNo(order.getOrderNo());

            // 判断交易类型：付款人是自己则为OUT，收款人是自己则为IN
            boolean isPayer = order.getPayerDid().equals(did);
            response.setType(isPayer ? "OUT" : "IN");
            response.setCounterpartyDid(isPayer ? order.getPayeeDid() : order.getPayerDid());
            response.setAmount(isPayer ? order.getOriginalAmount() : order.getAmount());
            response.setCurrency(isPayer ? order.getOriginalCurrency() : order.getCurrency());
            response.setStatus(getStatusText(order.getStatus()));
            response.setDescription(order.getDescription());
            response.setCreatedAt(order.getCreatedAt().toString());

            responses.add(response);
        }

        return responses;
    }

    /**
     * 获取交易历史（分页）
     */
    public PaymentDto.TransactionPageResponse getTransactionHistoryPage(String did, int page, int size) {
        int offset = (page - 1) * size;
        List<PaymentOrder> orders = orderMapper.findByDidWithPagination(did, size, offset);
        int total = orderMapper.countByDid(did);

        List<PaymentDto.TransactionResponse> responses = new ArrayList<>();
        for (PaymentOrder order : orders) {
            PaymentDto.TransactionResponse response = new PaymentDto.TransactionResponse();
            response.setOrderNo(order.getOrderNo());

            boolean isPayer = order.getPayerDid().equals(did);
            response.setType(isPayer ? "OUT" : "IN");
            response.setCounterpartyDid(isPayer ? order.getPayeeDid() : order.getPayerDid());
            response.setAmount(isPayer ? order.getOriginalAmount() : order.getAmount());
            response.setCurrency(isPayer ? order.getOriginalCurrency() : order.getCurrency());
            response.setStatus(getStatusText(order.getStatus()));
            response.setDescription(order.getDescription());
            response.setCreatedAt(order.getCreatedAt().toString());

            responses.add(response);
        }

        PaymentDto.TransactionPageResponse pageResponse = new PaymentDto.TransactionPageResponse();
        pageResponse.setList(responses);
        pageResponse.setTotal(total);
        pageResponse.setPage(page);
        pageResponse.setSize(size);
        pageResponse.setTotalPages((total + size - 1) / size);

        return pageResponse;
    }

    /**
     * 检查余额
     */
    private boolean checkBalance(Wallet wallet, BigDecimal amount, String currency) {
        return switch (currency.toUpperCase()) {
            case "CNY" -> wallet.getBalanceCny().compareTo(amount) >= 0;
            case "USD" -> wallet.getBalanceUsd().compareTo(amount) >= 0;
            case "ETH" -> wallet.getBalanceEth().compareTo(amount) >= 0;
            default -> false;
        };
    }

    /**
     * 扣除余额 - 使用乐观锁和重试机制
     *
     * @param wallet 钱包对象（需要包含最新版本号）
     * @param amount 扣除金额（正数）
     * @param currency 货币类型
     * @return 是否成功
     */
    private boolean deductBalanceWithRetry(Wallet wallet, BigDecimal amount, String currency) {
        for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
            // 重新获取钱包最新状态
            Wallet currentWallet = walletMapper.selectById(wallet.getId());
            if (currentWallet == null) {
                throw new BusinessException("钱包不存在");
            }

            // 检查余额是否充足
            if (!checkBalance(currentWallet, amount, currency)) {
                throw new BusinessException("余额不足");
            }

            int affected;
            BigDecimal deductAmount = amount.negate(); // 扣除使用负数

            switch (currency.toUpperCase()) {
                case "CNY" -> affected = walletMapper.addCnyBalanceWithVersion(
                    currentWallet.getId(), deductAmount, currentWallet.getVersion());
                case "USD" -> affected = walletMapper.addUsdBalanceWithVersion(
                    currentWallet.getId(), deductAmount, currentWallet.getVersion());
                case "ETH" -> affected = walletMapper.addEthBalanceWithVersion(
                    currentWallet.getId(), deductAmount, currentWallet.getVersion());
                default -> throw new BusinessException("不支持的货币类型: " + currency);
            }

            if (affected > 0) {
                log.debug("Balance deduction successful on attempt {}", attempt + 1);
                return true;
            }

            // 乐观锁冲突，等待后重试
            log.debug("Optimistic lock conflict on attempt {}, retrying...", attempt + 1);
            try {
                Thread.sleep(50 + new Random().nextInt(50)); // 50-100ms随机延迟
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BusinessException("支付操作被中断");
            }
        }

        log.error("Failed to deduct balance after {} retries", MAX_RETRY);
        throw new BusinessException("系统繁忙，请稍后重试");
    }

    /**
     * 增加余额 - 使用乐观锁
     */
    private boolean addBalanceWithRetry(Wallet wallet, BigDecimal amount, String currency) {
        for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
            Wallet currentWallet = walletMapper.selectById(wallet.getId());
            if (currentWallet == null) {
                throw new BusinessException("收款钱包不存在");
            }

            int affected;
            switch (currency.toUpperCase()) {
                case "CNY" -> affected = walletMapper.addCnyBalanceWithVersion(
                    currentWallet.getId(), amount, currentWallet.getVersion());
                case "USD" -> affected = walletMapper.addUsdBalanceWithVersion(
                    currentWallet.getId(), amount, currentWallet.getVersion());
                case "ETH" -> affected = walletMapper.addEthBalanceWithVersion(
                    currentWallet.getId(), amount, currentWallet.getVersion());
                default -> throw new BusinessException("不支持的货币类型: " + currency);
            }

            if (affected > 0) {
                return true;
            }

            log.debug("Optimistic lock conflict on add balance attempt {}", attempt + 1);
            try {
                Thread.sleep(50 + new Random().nextInt(50));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BusinessException("支付操作被中断");
            }
        }

        throw new BusinessException("系统繁忙，请稍后重试");
    }

    /**
     * 扣除余额（向后兼容，不使用乐观锁）
     * @deprecated 使用 {@link #deductBalanceWithRetry} 替代
     */
    @Deprecated
    private void deductBalance(Wallet wallet, BigDecimal amount, String currency) {
        switch (currency.toUpperCase()) {
            case "CNY" -> walletMapper.addCnyBalance(wallet.getId(), amount.negate());
            case "USD" -> walletMapper.addUsdBalance(wallet.getId(), amount.negate());
            case "ETH" -> walletMapper.addEthBalance(wallet.getId(), amount.negate());
        }
    }

    /**
     * 增加余额（向后兼容）
     * @deprecated 使用 {@link #addBalanceWithRetry} 替代
     */
    @Deprecated
    private void addBalance(Wallet wallet, BigDecimal amount, String currency) {
        switch (currency.toUpperCase()) {
            case "CNY" -> walletMapper.addCnyBalance(wallet.getId(), amount);
            case "USD" -> walletMapper.addUsdBalance(wallet.getId(), amount);
            case "ETH" -> walletMapper.addEthBalance(wallet.getId(), amount);
        }
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "PAY" + System.currentTimeMillis() + String.format("%04d", new Random().nextInt(10000));
    }

    /**
     * 状态文本转换
     */
    private String getStatusText(Integer status) {
        return switch (status) {
            case 0 -> "PENDING";
            case 1 -> "PROCESSING";
            case 2 -> "SUCCESS";
            case 3 -> "FAILED";
            case 4 -> "REFUNDED";
            case 5 -> "COMPLIANCE_REVIEW";
            case 6 -> "COMPLIANCE_REJECTED";
            default -> "UNKNOWN";
        };
    }
}
