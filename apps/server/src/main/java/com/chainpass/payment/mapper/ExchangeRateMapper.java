package com.chainpass.payment.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chainpass.payment.entity.ExchangeRate;
import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;
@Mapper
public interface ExchangeRateMapper extends BaseMapper<ExchangeRate> {
    @Select("SELECT rate FROM pay_exchange_rate WHERE from_currency = #{from} AND to_currency = #{to}")
    BigDecimal getRate(@Param("from") String fromCurrency, @Param("to") String toCurrency);
}
