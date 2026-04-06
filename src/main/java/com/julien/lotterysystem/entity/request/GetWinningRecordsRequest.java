package com.julien.lotterysystem.entity.request;

import lombok.Data;

@Data
public class GetWinningRecordsRequest {
    /**
     * 活动id（可选，为空时查询所有中奖记录）
     */
    private Long activityId;

    /**
     * 奖品id
     */
    private Long prizeId;

}
