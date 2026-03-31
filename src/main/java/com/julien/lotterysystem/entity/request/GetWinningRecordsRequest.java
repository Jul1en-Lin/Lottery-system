package com.julien.lotterysystem.entity.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GetWinningRecordsRequest {
    /**
     * 活动id
     */
    @NotNull(message = "活动id不能为空！")
    private Long activityId;

    /**
     * 奖品id
     */
    private Long prizeId;

}
