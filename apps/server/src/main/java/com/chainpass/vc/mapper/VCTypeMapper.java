package com.chainpass.vc.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chainpass.vc.entity.VCType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
@Mapper
public interface VCTypeMapper extends BaseMapper<VCType> {
    @Select("SELECT * FROM chain_vc_type WHERE type_code = #{typeCode}") VCType findByTypeCode(String typeCode);
    @Select("SELECT * FROM chain_vc_type WHERE status = 0 ORDER BY sort_order") List<VCType> findAllEnabled();
}
