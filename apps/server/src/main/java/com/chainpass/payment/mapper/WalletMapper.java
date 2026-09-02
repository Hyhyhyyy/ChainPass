package com.chainpass.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chainpass.payment.entity.Wallet;
import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;

@Mapper
public interface WalletMapper extends BaseMapper<Wallet> {
    @Select("SELECT * FROM pay_wallet WHERE user_id = #{userId}") Wallet findByUserId(Long userId);
    @Select("SELECT * FROM pay_wallet WHERE did = #{did}") Wallet findByDid(String did);
    @Select("SELECT * FROM pay_wallet WHERE wallet_address = #{address}") Wallet findByAddress(String address);
    @Update("UPDATE pay_wallet SET balance_cny = balance_cny + #{amount}, version = version + 1, updated_at = NOW() WHERE id = #{walletId} AND version = #{version} AND balance_cny + #{amount} >= 0")
    int addCnyBalanceWithVersion(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount, @Param("version") Integer version);
    @Update("UPDATE pay_wallet SET balance_usd = balance_usd + #{amount}, version = version + 1, updated_at = NOW() WHERE id = #{walletId} AND version = #{version} AND balance_usd + #{amount} >= 0")
    int addUsdBalanceWithVersion(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount, @Param("version") Integer version);
    @Update("UPDATE pay_wallet SET balance_eth = balance_eth + #{amount}, version = version + 1, updated_at = NOW() WHERE id = #{walletId} AND version = #{version} AND balance_eth + #{amount} >= 0")
    int addEthBalanceWithVersion(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount, @Param("version") Integer version);
    @Update("UPDATE pay_wallet SET balance_cny = balance_cny + #{amount}, updated_at = NOW() WHERE id = #{walletId}")
    int addCnyBalance(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);
    @Update("UPDATE pay_wallet SET balance_usd = balance_usd + #{amount}, updated_at = NOW() WHERE id = #{walletId}")
    int addUsdBalance(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);
    @Update("UPDATE pay_wallet SET balance_eth = balance_eth + #{amount}, updated_at = NOW() WHERE id = #{walletId}")
    int addEthBalance(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);
}
