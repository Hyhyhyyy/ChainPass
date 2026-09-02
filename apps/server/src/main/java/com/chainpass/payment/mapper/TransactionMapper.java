package com.chainpass.payment.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chainpass.payment.entity.Transaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
@Mapper
public interface TransactionMapper extends BaseMapper<Transaction> {
    @Select("SELECT * FROM pay_transaction WHERE order_no = #{orderNo}") Transaction findByOrderNo(String orderNo);
    @Select("SELECT * FROM pay_transaction WHERE tx_hash = #{txHash}") Transaction findByTxHash(String txHash);
}
