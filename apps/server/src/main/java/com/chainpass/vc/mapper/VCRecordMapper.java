package com.chainpass.vc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chainpass.vc.entity.VCRecord;
import com.chainpass.vc.entity.VCType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * VC Mapper
 */
@Mapper
public interface VCRecordMapper extends BaseMapper<VCRecord> {

    /**
     * 根据VC ID查询
     */
    @Select("SELECT * FROM chain_vc WHERE vc_id = #{vcId}")
    VCRecord findByVcId(String vcId);

    /**
     * 根据持有者DID查询VC列表
     */
    @Select("SELECT * FROM chain_vc WHERE holder_did = #{holderDid} ORDER BY issued_at DESC")
    List<VCRecord> findByHolderDid(String holderDid);

    /**
     * 根据持有者DID和类型查询有效VC
     */
    @Select("SELECT * FROM chain_vc WHERE holder_did = #{holderDid} AND vc_type = #{vcType} AND status = 0 AND expires_at > NOW()")
    List<VCRecord> findValidByHolderDidAndType(String holderDid, String vcType);

    /**
     * 吊销VC
     */
    @Update("UPDATE chain_vc SET status = 2, revoked_at = NOW(), revoke_reason = #{reason} WHERE vc_id = #{vcId}")
    int revokeVC(String vcId, String reason);

    /**
     * 更新过期状态
     */
    @Update("UPDATE chain_vc SET status = 1 WHERE status = 0 AND expires_at < NOW()")
    int updateExpired();
}

/**
 * VC类型Mapper
 */
@Mapper
public interface VCTypeMapper extends BaseMapper<VCType> {

    /**
     * 根据类型编码查询
     */
    @Select("SELECT * FROM chain_vc_type WHERE type_code = #{typeCode}")
    VCType findByTypeCode(String typeCode);

    /**
     * 查询所有启用的类型
     */
    @Select("SELECT * FROM chain_vc_type WHERE status = 0 ORDER BY sort_order")
    List<VCType> findAllEnabled();
}