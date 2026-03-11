package com.julien.lotterysystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.julien.lotterysystem.entity.dataobject.Activity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {
}