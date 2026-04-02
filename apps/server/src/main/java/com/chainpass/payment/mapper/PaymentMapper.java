package com.chainpass.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chainpass.payment.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

/**
 * 钱包Mapper
 */
@Mapper
public interface WalletMapper extends BaseMapper<Wallet> {

    @Select("SELECT * FROM pay_wallet WHERE user_id = #{userId}")
    Wallet findByUserId(Long userId);

    @Select("SELECT * FROM pay_wallet WHERE did = #{did}")
    Wallet findByDid(String did);

    @Select("SELECT * FROM pay_wallet WHERE address = #{address}")
    Wallet findByAddress(String address);

    /**
     * 增加CNY余额 - 使用乐观锁检查余额充足
     * 返回影响行数，0表示余额不足或版本不匹配
     */
    @Update("UPDATE pay_wallet SET balance_cny = balance_cny + #{amount}, version = version + 1, updated_at = NOW() " +
            "WHERE id = #{walletId} AND version = #{version} AND balance_cny + #{amount} >= 0")
    int addCnyBalanceWithVersion(@Param("walletId") Long walletId,
                                  @Param("amount") BigDecimal amount,
                                  @Param("version") Integer version);

    /**
     * 增加USD余额 - 使用乐观锁
     */
    @Update("UPDATE pay_wallet SET balance_usd = balance_usd + #{amount}, version = version + 1, updated_at = NOW() " +
            "WHERE id = #{walletId} AND version = #{version} AND balance_usd + #{amount} >= 0")
    int addUsdBalanceWithVersion(@Param("walletId") Long walletId,
                                  @Param("amount") BigDecimal amount,
                                  @Param("version") Integer version);

    /**
     * 增加ETH余额 - 使用乐观锁
     */
    @Update("UPDATE pay_wallet SET balance_eth = balance_eth + #{amount}, version = version + 1, updated_at = NOW() " +
            "WHERE id = #{walletId} AND version = #{version} AND balance_eth + #{amount} >= 0")
    int addEthBalanceWithVersion(@Param("walletId") Long walletId,
                                  @Param("amount") BigDecimal amount,
                                  @Param("version") Integer version);

    /**
     * 无版本检查的余额更新（用于初始化等非并发场景）
     */
    @Update("UPDATE pay_wallet SET balance_cny = balance_cny + #{amount}, updated_at = NOW() WHERE id = #{walletId}")
    int addCnyBalance(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    @Update("UPDATE pay_wallet SET balance_usd = balance_usd + #{amount}, updated_at = NOW() WHERE id = #{walletId}")
    int addUsdBalance(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    @Update("UPDATE pay_wallet SET balance_eth = balance_eth + #{amount}, updated_at = NOW() WHERE id = #{walletId}")
    int addEthBalance(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);
}

/**
 * 支付订单Mapper
 */
@Mapper
interface PaymentOrderMapper extends BaseMapper<PaymentOrder> {

    @Select("SELECT * FROM pay_order WHERE order_no = #{orderNo}")
    PaymentOrder findByOrderNo(String orderNo);

    @Select("SELECT * FROM pay_order WHERE payer_did = #{payerDid} ORDER BY created_at DESC")
    List<PaymentOrder> findByPayerDid(String payerDid);

    @Select("SELECT * FROM pay_order WHERE payee_did = #{payeeDid} ORDER BY created_at DESC")
    List<PaymentOrder> findByPayeeDid(String payeeDid);

    /**
     * 查询用户相关的所有订单（作为付款人或收款人）
     */
    @Select("SELECT * FROM pay_order WHERE payer_did = #{did} OR payee_did = #{did} ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<PaymentOrder> findByDidWithPagination(@Param("did") String did,
                                                @Param("limit") int limit,
                                                @Param("offset") int offset);

    /**
     * 统计用户相关订单总数
     */
    @Select("SELECT COUNT(*) FROM pay_order WHERE payer_did = #{did} OR payee_did = #{did}")
    int countByDid(@Param("did") String did);

    @Update("UPDATE pay_order SET status = #{status}, updated_at = NOW() WHERE order_no = #{orderNo}")
    int updateStatus(@Param("orderNo") String orderNo, @Param("status") Integer status);
}

/**
 * 交易记录Mapper
 */
@Mapper
interface TransactionMapper extends BaseMapper<Transaction> {

    @Select("SELECT * FROM pay_transaction WHERE order_no = #{orderNo}")
    Transaction findByOrderNo(String orderNo);

    @Select("SELECT * FROM pay_transaction WHERE tx_hash = #{txHash}")
    Transaction findByTxHash(String txHash);
}

/**
 * 汇率Mapper
 */
@Mapper
interface ExchangeRateMapper extends BaseMapper<ExchangeRate> {

    @Select("SELECT rate FROM pay_exchange_rate WHERE from_currency = #{from} AND to_currency = #{to}")
    BigDecimal getRate(@Param("from") String fromCurrency, @Param("to") String toCurrency);
}