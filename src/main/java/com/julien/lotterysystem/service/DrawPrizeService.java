package com.julien.lotterysystem.service;

import com.julien.lotterysystem.entity.dataobject.WinningRecord;
import com.julien.lotterysystem.entity.request.DrawPrizeRequest;
import com.julien.lotterysystem.entity.request.GetWinningRecordsRequest;
import com.julien.lotterysystem.entity.response.WinningRecordResponse;
import jakarta.validation.Valid;
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
     * 保存中奖记录并缓存
     */
    List<WinningRecord> saveWinningRecord(DrawPrizeRequest param);

    /**
     * 获取特定活动的中奖公示名单缓存（所有中奖记录列表）（活动维度）
     */
    List<WinningRecord> getWinningRecordCache(Long activityId);

    /**
     * 获取特定活动的特定奖品的中奖者列表缓存（奖品维度）
     */
    List<WinningRecord> getWinningRecordCache(Long activityId, Long prizeId);


    /**
     * 状态扭转，删除中奖记录缓存
     */
    void deleteWinningRecordCache(Long activityId, Long prizeId);


    /**
     * 获取中奖记录（活动维度/奖品维度）
     * 判断请求参数中是否包含活动id
     * 包含则查询奖品维度的中奖信息
     * 不包含则直接查询整个活动维度的中奖信息
     */
    List<WinningRecordResponse> getWinningRecords(GetWinningRecordsRequest request);
}
