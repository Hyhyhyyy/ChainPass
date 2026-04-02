package com.chainpass.payment.service;

import com.chainpass.did.service.DIDService;
import com.chainpass.exception.BusinessException;
import com.chainpass.payment.dto.PaymentDto;
import com.chainpass.payment.entity.*;
import com.chainpass.payment.mapper.*;
import com.chainpass.util.RedisCache;
import com.chainpass.vc.entity.VerifiableCredential;
import com.chainpass.vc.service.VCService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 支付服务 - 模拟跨境支付核心服务
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
    private final VCService vcService;
    private final RedisCache redisCache;

    // 手续费率
    private static final BigDecimal FEE_RATE = new BigDecimal("0.001"); // 0.1%

    // 乐观锁重试次数
    private static final int MAX_RETRY = 3;

    // 汇率缓存时间：1小时
    private static final long RATE_CACHE_TTL = 60 * 60 * 1000;
    private static final String RATE_CACHE_PREFIX = "exchange:rate:";

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
        String address = "0x" + UUID.randomUUID().toString().replace("-", "").substring(0, 40);

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

        // 初始化模拟余额
        initMockBalance(wallet.getId());

        return wallet;
    }

    /**
     * 初始化模拟余额（测试用）
     */
    private void initMockBalance(Long walletId) {
        walletMapper.addCnyBalance(walletId, new BigDecimal("10000.00"));
        walletMapper.addUsdBalance(walletId, new BigDecimal("1500.00"));
        walletMapper.addEthBalance(walletId, new BigDecimal("0.5"));
    }

    /**
     * 创建跨境支付订单
     */
    @Transactional
    public PaymentOrder createPayment(String payerDid, PaymentDto.CreatePaymentRequest request) {
        log.info("Creating payment: payer={}, payee={}, amount={} {}",
            payerDid, request.getPayeeDid(), request.getAmount(), request.getCurrency());

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
        BigDecimal feeAmount = finalAmount.multiply(FEE_RATE).setScale(2, RoundingMode.HALF_UP);

        // 6. 检查余额
        if (!checkBalance(payerWallet, finalAmount.add(feeAmount), finalCurrency)) {
            throw new BusinessException("余额不足");
        }

        // 7. 创建订单
        String orderNo = generateOrderNo();
        PaymentOrder order = PaymentOrder.builder()
            .orderNo(orderNo)
            .payerDid(payerDid)
            .payeeDid(request.getPayeeDid())
            .payerWalletId(payerWallet.getId())
            .amount(finalAmount)
            .currency(finalCurrency)
            .originalAmount(originalAmount)
            .originalCurrency(originalCurrency)
            .exchangeRate(exchangeRate)
            .feeAmount(feeAmount)
            .feeCurrency(finalCurrency)
            .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "wallet")
            .description(request.getDescription())
            .vcRequired("PaymentCredential") // 默认需要支付凭证
            .vcVerified(0)
            .kycRequired(0)
            .riskScore(0)
            .riskLevel("LOW")
            .status(0) // 待支付
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .expiredAt(Instant.now().plus(30, ChronoUnit.MINUTES))
            .build();

        orderMapper.insert(order);

        log.info("Payment order created: {}", orderNo);
        return order;
    }

    /**
     * 执行支付 - 使用乐观锁确保并发安全
     */
    @Transactional
    public Transaction executePayment(String orderNo) {
        log.info("Executing payment: {}", orderNo);

        // 1. 获取订单
        PaymentOrder order = orderMapper.findByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态不正确");
        }

        // 2. 检查过期
        if (order.getExpiredAt().isBefore(Instant.now())) {
            orderMapper.updateStatus(orderNo, 3); // 失败
            throw new BusinessException("订单已过期");
        }

        // 3. 更新订单状态为处理中
        orderMapper.updateStatus(orderNo, 1);

        try {
            // 4. 执行转账 - 使用乐观锁
            Wallet payerWallet = walletMapper.selectById(order.getPayerWalletId());
            if (payerWallet == null) {
                throw new BusinessException("付款人钱包不存在");
            }

            Wallet payeeWallet = walletMapper.findByDid(order.getPayeeDid());
            if (payeeWallet == null) {
                // 如果收款人没有钱包，自动创建
                payeeWallet = createWallet(null, order.getPayeeDid());
            }

            // 扣除付款人余额（含手续费）- 使用乐观锁
            BigDecimal totalDeduct = order.getAmount().add(order.getFeeAmount());
            boolean deductSuccess = deductBalanceWithRetry(payerWallet, totalDeduct, order.getCurrency());
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
                .gateway("mock")
                .gatewayTxId("mock_" + System.currentTimeMillis())
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
            PaymentOrder updateOrder = new PaymentOrder();
            updateOrder.setOrderNo(orderNo);
            updateOrder.setStatus(2); // 成功
            updateOrder.setPaidAt(Instant.now());
            updateOrder.setUpdatedAt(Instant.now());
            orderMapper.updateById(updateOrder);

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

    /**
     * 获取汇率（带缓存）
     */
    public BigDecimal getExchangeRate(String from, String to) {
        if (from.equals(to)) {
            return BigDecimal.ONE;
        }

        // 尝试从缓存获取
        String cacheKey = RATE_CACHE_PREFIX + from + ":" + to;
        BigDecimal cached = redisCache.getCacheObject(cacheKey);
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
            response.setAmount(order.getAmount());
            response.setCurrency(order.getCurrency());
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
            response.setAmount(order.getAmount());
            response.setCurrency(order.getCurrency());
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
            default -> "UNKNOWN";
        };
    }
}