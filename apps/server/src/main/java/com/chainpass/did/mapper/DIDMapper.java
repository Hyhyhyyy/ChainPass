package com.chainpass.did.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chainpass.did.entity.DIDRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * DID Mapper
 */
@Mapper
public interface DIDMapper extends BaseMapper<DIDRecord> {

    /**
     * 根据DID查询记录
     */
    @Select("SELECT * FROM chain_did WHERE did = #{did}")
    DIDRecord findByDid(String did);

    /**
     * 根据用户ID查询DID
     */
    @Select("SELECT * FROM chain_did WHERE user_id = #{userId} AND status != 2")
    DIDRecord findByUserId(Long userId);

    /**
     * 吊销DID
     */
    @Update("UPDATE chain_did SET status = 2, revoked_at = NOW(), revoke_reason = #{reason} WHERE did = #{did}")
    int revokeDID(String did, String reason);

    /**
     * 检查DID是否存在且有效
     */
    @Select("SELECT COUNT(*) FROM chain_did WHERE did = #{did} AND status = 0 AND expires_at > NOW()")
    int countValidDID(String did);
}