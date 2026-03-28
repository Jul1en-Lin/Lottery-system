package com.julien.lotterysystem.service;

import com.julien.lotterysystem.entity.dataobject.WinningRecord;
import com.julien.lotterysystem.entity.request.DrawPrizeRequest;

import java.util.List;

public interface DrawPrizeService {

    /**
     * 异步抽奖接口，无需返回结果
     */
    void drawPrize(DrawPrizeRequest request);

    /**
     * 校验抽奖请求
     */
    Boolean checkDrawPrizeRequest(DrawPrizeRequest request);

    /**
     * 保存中奖记录
     */
    List<WinningRecord> saveWinningRecord(DrawPrizeRequest param);
}
