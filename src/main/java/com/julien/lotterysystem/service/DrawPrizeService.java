package com.julien.lotterysystem.service;

import com.julien.lotterysystem.entity.dataobject.WinningRecord;
import com.julien.lotterysystem.entity.request.DrawPrizeRequest;
import jakarta.validation.constraints.NotNull;

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

    /**
     * 获取特定活动的中奖公示名单（所有中奖记录列表）
     */
    List<WinningRecord> getWinningRecord(Long activityId);

    /**
     * 获取特定活动的特定奖品的中奖者列表
     */
    List<WinningRecord> getWinningRecord(Long activityId, Long prizeId);


    /**
     * 状态扭转，删除中奖记录缓存
     */
    void deleteWinningRecordCache(Long activityId, Long prizeId);
}
