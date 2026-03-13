package com.julien.lotterysystem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.julien.lotterysystem.entity.dataobject.Prize;
import com.julien.lotterysystem.entity.request.CreatePrizeRequest;
import com.julien.lotterysystem.entity.response.PrizeInfoListResponse;
import com.julien.lotterysystem.entity.response.PrizeInfoResponse;
import jakarta.validation.Valid;

public interface PrizeService {

    // 翻页查询列表
    PrizeInfoListResponse<PrizeInfoResponse> getPrizeInfoList(Page<Prize> page);

    /**
     * 创建奖品
     * @param pictureFileName 存入图片
     * @return 奖品id
     */
    Long createPrize(@Valid CreatePrizeRequest createPrizeRequest, String pictureFileName);
}
