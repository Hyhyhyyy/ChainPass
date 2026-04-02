package com.chainpass.payment.controller;

import com.chainpass.did.entity.DIDDocument;
import com.chainpass.did.service.DIDService;
import com.chainpass.entity.LoginUser;
import com.chainpass.payment.dto.PaymentDto;
import com.chainpass.payment.entity.PaymentOrder;
import com.chainpass.payment.entity.Transaction;
import com.chainpass.payment.entity.Wallet;
import com.chainpass.payment.service.PaymentService;
import com.chainpass.vo.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 支付控制器 - 跨境支付API
 */

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Tag(name = "支付管理", description = "跨境支付与钱包管理接口")
public class PaymentController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;
    private final DIDService didService;

    /**
     * 获取我的钱包
     */
    @GetMapping("/wallet")
    @Operation(summary = "获取我的钱包", description = "获取当前用户的钱包信息")
    public ApiResponse<PaymentDto.WalletResponse> getWallet(@AuthenticationPrincipal LoginUser loginUser) {
        // 获取用户DID
        DIDDocument didDoc = didService.getDIDByUserId(loginUser.getUserId());
        if (didDoc == null) {
            return ApiResponse.error("请先创建DID");
        }

        Wallet wallet = paymentService.getOrCreateWallet(loginUser.getUserId(), didDoc.getId());

        PaymentDto.WalletResponse response = new PaymentDto.WalletResponse();
        response.setId(wallet.getId());
        response.setDid(wallet.getDid());
        response.setAddress(wallet.getAddress());
        response.setBalanceCny(wallet.getBalanceCny());
        response.setBalanceUsd(wallet.getBalanceUsd());
        response.setBalanceEth(wallet.getBalanceEth());

        // 计算总余额（折合人民币）
        BigDecimal total = wallet.getBalanceCny();
        if (wallet.getBalanceUsd().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal rate = paymentService.getExchangeRate("USD", "CNY");
            if (rate != null) {
                total = total.add(wallet.getBalanceUsd().multiply(rate));
            }
        }
        response.setTotalBalanceCny(total.setScale(2, BigDecimal.ROUND_HALF_UP));
        response.setStatus(wallet.getStatus() == 0 ? "ACTIVE" : "FROZEN");

        return ApiResponse.success(response);
    }

    /**
     * 创建支付订单
     */
    @PostMapping("/create")
    @Operation(summary = "创建支付订单", description = "创建跨境支付订单")
    public ApiResponse<PaymentDto.PaymentOrderResponse> createPayment(
            @AuthenticationPrincipal LoginUser loginUser,
            @Valid @RequestBody PaymentDto.CreatePaymentRequest request) {

        log.info("Creating payment for user: {}", loginUser.getUserId());

        // 获取用户DID
        DIDDocument didDoc = didService.getDIDByUserId(loginUser.getUserId());
        if (didDoc == null) {
            return ApiResponse.error("请先创建DID");
        }

        PaymentOrder order = paymentService.createPayment(didDoc.getId(), request);

        PaymentDto.PaymentOrderResponse response = new PaymentDto.PaymentOrderResponse();
        response.setOrderNo(order.getOrderNo());
        response.setPayerDid(order.getPayerDid());
        response.setPayeeDid(order.getPayeeDid());
        response.setAmount(order.getAmount());
        response.setCurrency(order.getCurrency());
        response.setOriginalAmount(order.getOriginalAmount());
        response.setOriginalCurrency(order.getOriginalCurrency());
        response.setExchangeRate(order.getExchangeRate());
        response.setFeeAmount(order.getFeeAmount());
        response.setStatus("PENDING");
        response.setCreatedAt(order.getCreatedAt().toString());

        return ApiResponse.success(response);
    }

    /**
     * 执行支付
     */
    @PostMapping("/execute/{orderNo}")
    @Operation(summary = "执行支付", description = "执行支付订单")
    public ApiResponse<Transaction> executePayment(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable String orderNo) {

        log.info("Executing payment: {} for user: {}", orderNo, loginUser.getUserId());

        Transaction transaction = paymentService.executePayment(orderNo);
        return ApiResponse.success(transaction);
    }

    /**
     * 获取交易历史
     */
    @GetMapping("/history")
    @Operation(summary = "交易历史", description = "获取用户的交易历史")
    public ApiResponse<List<PaymentDto.TransactionResponse>> getHistory(
            @AuthenticationPrincipal LoginUser loginUser) {

        // 获取用户DID
        DIDDocument didDoc = didService.getDIDByUserId(loginUser.getUserId());
        if (didDoc == null) {
            return ApiResponse.error("请先创建DID");
        }

        List<PaymentDto.TransactionResponse> history =
            paymentService.getTransactionHistory(didDoc.getId());

        return ApiResponse.success(history);
    }

    /**
     * 获取汇率
     */
    @GetMapping("/rate/{from}/{to}")
    @Operation(summary = "获取汇率", description = "获取两种货币之间的汇率")
    public ApiResponse<BigDecimal> getExchangeRate(
            @PathVariable String from,
            @PathVariable String to) {

        BigDecimal rate = paymentService.getExchangeRate(from, to);
        if (rate == null) {
            return ApiResponse.error("不支持的货币兑换");
        }

        return ApiResponse.success(rate);
    }
}