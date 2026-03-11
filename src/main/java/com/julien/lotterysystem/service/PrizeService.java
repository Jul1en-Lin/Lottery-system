package com.julien.lotterysystem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.julien.lotterysystem.entity.dataobject.Prize;
import com.julien.lotterysystem.entity.response.PrizeInfoListResponse;
import com.julien.lotterysystem.entity.response.PrizeInfoResponse;

public interface PrizeService {

    // 翻页查询列表
    PrizeInfoListResponse<PrizeInfoResponse> getPrizeInfoList(Page<Prize> page);
}
