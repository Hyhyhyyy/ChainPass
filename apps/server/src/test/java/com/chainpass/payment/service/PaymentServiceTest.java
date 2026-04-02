package com.chainpass.payment.service;

import com.chainpass.did.entity.DIDDocument;
import com.chainpass.did.service.DIDService;
import com.chainpass.payment.dto.PaymentDto;
import com.chainpass.payment.entity.PaymentOrder;
import com.chainpass.payment.entity.Wallet;
import com.chainpass.payment.mapper.*;
import com.chainpass.vc.service.VCService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 支付服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private WalletMapper walletMapper;

    @Mock
    private PaymentOrderMapper orderMapper;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private ExchangeRateMapper rateMapper;

    @Mock
    private DIDService didService;

    @Mock
    private VCService vcService;

    @InjectMocks
    private PaymentService paymentService;

    private String payerDid;
    private String payeeDid;
    private Wallet payerWallet;
    private Wallet payeeWallet;

    @BeforeEach
    void setUp() {
        payerDid = "did:chainpass:payer";
        payeeDid = "did:chainpass:payee";

        payerWallet = new Wallet();
        payerWallet.setId(1L);
        payerWallet.setDid(payerDid);
        payerWallet.setAddress("0xpayer");
        payerWallet.setBalanceCny(new BigDecimal("10000.00"));
        payerWallet.setBalanceUsd(new BigDecimal("1500.00"));
        payerWallet.setBalanceEth(new BigDecimal("0.5"));
        payerWallet.setStatus(0);

        payeeWallet = new Wallet();
        payeeWallet.setId(2L);
        payeeWallet.setDid(payeeDid);
        payeeWallet.setAddress("0xpayee");
        payeeWallet.setBalanceCny(BigDecimal.ZERO);
        payeeWallet.setStatus(0);
    }

    @Test
    @DisplayName("创建支付订单 - 成功")
    void testCreatePayment_Success() {
        // Given
        PaymentDto.CreatePaymentRequest request = new PaymentDto.CreatePaymentRequest();
        request.setPayeeDid(payeeDid);
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("CNY");

        when(didService.isValidDID(payerDid)).thenReturn(true);
        when(didService.isValidDID(payeeDid)).thenReturn(true);
        when(walletMapper.findByDid(payerDid)).thenReturn(payerWallet);
        when(orderMapper.insert(any())).thenReturn(1);

        // When
        PaymentOrder order = paymentService.createPayment(payerDid, request);

        // Then
        assertNotNull(order);
        assertNotNull(order.getOrderNo());
        assertTrue(order.getOrderNo().startsWith("PAY"));
        assertEquals(payerDid, order.getPayerDid());
        assertEquals(payeeDid, order.getPayeeDid());
        assertEquals(0, order.getStatus());

        verify(orderMapper, times(1)).insert(any());
    }

    @Test
    @DisplayName("创建支付订单 - 付款人DID无效")
    void testCreatePayment_InvalidPayerDid() {
        // Given
        PaymentDto.CreatePaymentRequest request = new PaymentDto.CreatePaymentRequest();
        request.setPayeeDid(payeeDid);
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("CNY");

        when(didService.isValidDID(payerDid)).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            paymentService.createPayment(payerDid, request);
        });

        verify(orderMapper, never()).insert(any());
    }

    @Test
    @DisplayName("获取汇率 - 成功")
    void testGetExchangeRate_Success() {
        // Given
        when(rateMapper.getRate("CNY", "USD")).thenReturn(new BigDecimal("0.1389"));

        // When
        var rate = paymentService.getExchangeRate("CNY", "USD");

        // Then
        assertNotNull(rate);
        assertEquals(new BigDecimal("0.1389"), rate);
    }

    @Test
    @DisplayName("获取汇率 - 相同货币")
    void testGetExchangeRate_SameCurrency() {
        // When
        var rate = paymentService.getExchangeRate("CNY", "CNY");

        // Then
        assertEquals(BigDecimal.ONE, rate);
    }

    @Test
    @DisplayName("创建钱包 - 成功")
    void testCreateWallet_Success() {
        // Given
        Long userId = 1L;
        when(walletMapper.findByUserId(userId)).thenReturn(null);
        when(walletMapper.insert(any())).thenReturn(1);

        // When
        Wallet wallet = paymentService.createWallet(userId, payerDid);

        // Then
        assertNotNull(wallet);
        assertTrue(wallet.getAddress().startsWith("0x"));
        assertEquals(payerDid, wallet.getDid());

        verify(walletMapper, times(4)).addCnyBalance(any(), any()); // 初始化余额
    }
}