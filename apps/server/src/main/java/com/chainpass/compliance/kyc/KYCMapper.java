package com.chainpass.compliance.kyc;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

/**
 * KYC Mapper
 */
@Mapper
public interface KYCMapper extends BaseMapper<KYCRecord> {

    @Select("SELECT * FROM comp_kyc WHERE user_id = #{userId}")
    KYCRecord findByUserId(Long userId);

    @Select("SELECT * FROM comp_kyc WHERE did = #{did}")
    KYCRecord findByDid(String did);

    @Select("SELECT * FROM comp_kyc WHERE verification_status = #{status} ORDER BY submitted_at ASC LIMIT 200")
    List<KYCRecord> findByStatus(Integer status);

    @Update("UPDATE comp_kyc SET verification_status = #{status}, verified_at = NOW(), verified_by = #{verifiedBy} WHERE id = #{id}")
    int updateStatus(Long id, Integer status, Long verifiedBy);
}
