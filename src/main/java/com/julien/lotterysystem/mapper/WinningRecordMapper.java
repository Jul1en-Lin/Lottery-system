package com.julien.lotterysystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.julien.lotterysystem.entity.dataobject.WinningRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WinningRecordMapper extends BaseMapper<WinningRecord> {
}
