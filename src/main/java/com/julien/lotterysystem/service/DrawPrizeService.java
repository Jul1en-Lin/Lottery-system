package com.julien.lotterysystem.service;

import com.julien.lotterysystem.entity.request.DrawPrizeRequest;

public interface DrawPrizeService {

    /**
     * 异步抽奖接口，无需返回结果
     */
    void drawPrize(DrawPrizeRequest request);

    /**
     * 校验抽奖请求
     */
    Boolean checkDrawPrizeRequest(DrawPrizeRequest request);
}
