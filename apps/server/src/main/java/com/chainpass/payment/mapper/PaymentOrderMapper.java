package com.chainpass.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chainpass.payment.entity.PaymentOrder;
import org.apache.ibatis.annotations.*;
import java.time.Instant;
import java.util.List;

@Mapper
public interface PaymentOrderMapper extends BaseMapper<PaymentOrder> {
    @Select("SELECT * FROM pay_order WHERE order_no = #{orderNo}") PaymentOrder findByOrderNo(String orderNo);
    @Select("SELECT * FROM pay_order WHERE payer_did = #{payerDid} ORDER BY created_at DESC") List<PaymentOrder> findByPayerDid(String payerDid);
    @Select("SELECT * FROM pay_order WHERE payee_did = #{payeeDid} ORDER BY created_at DESC") List<PaymentOrder> findByPayeeDid(String payeeDid);
    @Select("SELECT * FROM pay_order WHERE payer_did = #{did} OR payee_did = #{did} ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<PaymentOrder> findByDidWithPagination(@Param("did") String did, @Param("limit") int limit, @Param("offset") int offset);
    @Select("SELECT COUNT(*) FROM pay_order WHERE payer_did = #{did} OR payee_did = #{did}") int countByDid(@Param("did") String did);
    @Update("UPDATE pay_order SET status = #{status}, updated_at = NOW() WHERE order_no = #{orderNo}")
    int updateStatus(@Param("orderNo") String orderNo, @Param("status") Integer status);
    @Update("UPDATE pay_order SET status = 1, updated_at = NOW() WHERE order_no = #{orderNo} AND status = 0")
    int markProcessing(@Param("orderNo") String orderNo);
    @Update("UPDATE pay_order SET status = 2, paid_at = #{paidAt}, updated_at = NOW() WHERE order_no = #{orderNo} AND status = 1")
    int markPaid(@Param("orderNo") String orderNo, @Param("paidAt") Instant paidAt);
    @Select("SELECT * FROM pay_order WHERE status = 5 ORDER BY created_at ASC LIMIT 200")
    List<PaymentOrder> findComplianceReviewQueue();
    @Update("UPDATE pay_order SET status = 0, compliance_decision = 'MANUAL_APPROVED', compliance_reasons = CONCAT(compliance_reasons, '；人工复核通过：', #{note}), reviewed_by = #{reviewerId}, reviewed_at = #{reviewedAt}, expired_at = DATE_ADD(NOW(), INTERVAL 30 MINUTE), updated_at = NOW() WHERE order_no = #{orderNo} AND status = 5")
    int approveComplianceReview(@Param("orderNo") String orderNo, @Param("reviewerId") Long reviewerId,
                                @Param("reviewedAt") Instant reviewedAt, @Param("note") String note);
    @Update("UPDATE pay_order SET status = 6, compliance_decision = 'MANUAL_REJECTED', compliance_reasons = CONCAT(compliance_reasons, '；人工复核拒绝：', #{note}), reviewed_by = #{reviewerId}, reviewed_at = #{reviewedAt}, updated_at = NOW() WHERE order_no = #{orderNo} AND status = 5")
    int rejectComplianceReview(@Param("orderNo") String orderNo, @Param("reviewerId") Long reviewerId,
                               @Param("reviewedAt") Instant reviewedAt, @Param("note") String note);
}
