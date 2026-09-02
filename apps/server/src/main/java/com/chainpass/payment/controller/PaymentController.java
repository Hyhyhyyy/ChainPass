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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 沙盒账本控制器
 */

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Tag(name = "跨境合规支付原型", description = "身份门控、走廊规则、风险决策、人工复核与内部多币种账本；不连接真实资金通道")
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
    @Operation(summary = "创建跨境合规支付订单", description = "执行身份门控和风险分流；仅在内部账本记账，不代表真实支付")
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

        PaymentDto.PaymentOrderResponse response = toResponse(order);
        return ApiResponse.success(response);
    }

    private PaymentDto.PaymentOrderResponse toResponse(PaymentOrder order) {
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
        response.setStatus(switch (order.getStatus()) {
            case 0 -> "PENDING";
            case 5 -> "COMPLIANCE_REVIEW";
            case 6 -> "COMPLIANCE_REJECTED";
            default -> "UNKNOWN";
        });
        response.setCreatedAt(order.getCreatedAt().toString());
        response.setSourceCountry(order.getSourceCountry());
        response.setTargetCountry(order.getTargetCountry());
        response.setBeneficiaryName(order.getBeneficiaryName());
        response.setPaymentPurpose(order.getPaymentPurpose());
        response.setRiskScore(order.getRiskScore());
        response.setRiskLevel(order.getRiskLevel());
        response.setComplianceDecision(order.getComplianceDecision());
        response.setComplianceReasons(order.getComplianceReasons());
        return response;
    }

    /**
     * 执行支付
     */
    @PostMapping("/execute/{orderNo}")
    @Operation(summary = "执行跨境合规支付", description = "仅付款方可执行自己已获放行的内部账本订单")
    public ApiResponse<Transaction> executePayment(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable String orderNo) {

        log.info("Executing payment: {} for user: {}", orderNo, loginUser.getUserId());

        DIDDocument didDoc = didService.getDIDByUserId(loginUser.getUserId());
        if (didDoc == null) {
            return ApiResponse.error("请先创建DID");
        }
        Transaction transaction = paymentService.executePayment(orderNo, didDoc.getId());
        return ApiResponse.success(transaction);
    }

    @GetMapping("/orders/{orderNo}")
    @Operation(summary = "查询本人跨境订单", description = "用于付款方查询人工复核后的最新状态")
    public ApiResponse<PaymentDto.PaymentOrderResponse> getOrder(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable String orderNo) {
        DIDDocument didDoc = didService.getDIDByUserId(loginUser.getUserId());
        if (didDoc == null) {
            return ApiResponse.error("请先创建DID");
        }
        return ApiResponse.success(toResponse(paymentService.getOwnedOrder(orderNo, didDoc.getId())));
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

    @GetMapping("/compliance/reviews")
    @PreAuthorize("hasAuthority('compliance:payment:audit')")
    @Operation(summary = "跨境订单复核队列")
    public ApiResponse<List<PaymentOrder>> getComplianceReviews() {
        return ApiResponse.success(paymentService.getComplianceReviewQueue());
    }

    @PostMapping("/compliance/reviews/{orderNo}/approve")
    @PreAuthorize("hasAuthority('compliance:payment:audit')")
    @Operation(summary = "人工放行跨境订单")
    public ApiResponse<Void> approveComplianceReview(@AuthenticationPrincipal LoginUser reviewer,
                                                     @PathVariable String orderNo,
                                                     @RequestParam String note) {
        paymentService.approveComplianceReview(orderNo, reviewer.getUserId(), note);
        return ApiResponse.success();
    }

    @PostMapping("/compliance/reviews/{orderNo}/reject")
    @PreAuthorize("hasAuthority('compliance:payment:audit')")
    @Operation(summary = "人工拒绝跨境订单")
    public ApiResponse<Void> rejectComplianceReview(@AuthenticationPrincipal LoginUser reviewer,
                                                    @PathVariable String orderNo,
                                                    @RequestParam String note) {
        paymentService.rejectComplianceReview(orderNo, reviewer.getUserId(), note);
        return ApiResponse.success();
    }
}
