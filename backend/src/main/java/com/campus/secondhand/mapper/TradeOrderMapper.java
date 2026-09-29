package com.campus.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.secondhand.entity.TradeOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TradeOrderMapper extends BaseMapper<TradeOrder> {
    @Select("SELECT * FROM trade_order WHERE id = #{id} FOR UPDATE")
    TradeOrder selectForUpdate(@Param("id") Long id);
}
