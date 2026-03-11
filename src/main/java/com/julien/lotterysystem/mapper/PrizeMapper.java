package com.julien.lotterysystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.julien.lotterysystem.entity.dataobject.Prize;
import com.julien.lotterysystem.entity.dataobject.User;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface PrizeMapper extends BaseMapper<Prize> {
}
