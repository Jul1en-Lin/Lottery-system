package com.julien.lotterysystem.entity.dto;

import com.julien.lotterysystem.common.enums.ActivityStatusEnum;
import com.julien.lotterysystem.common.enums.PrizeStatusEnum;
import com.julien.lotterysystem.common.enums.UserStatusEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 活动状态扭转DTO
 */
@Data
public class ConvertActivityStatusDTO {
    /**
     * 活动id
     */
    @NotNull(message = "活动id不能为空")
    private Long activityId;

    /**
     * 活动目标状态
     */
    @NotNull(message = "活动目标状态不能为空")
    private ActivityStatusEnum targetActivityStatus;

    /**
     * 奖品id
     */
    @NotNull(message = "奖品id不能为空")
    private Long prizeId;

    /**
     * 奖品目标状态
     */
    @NotNull(message = "奖品目标状态不能为空")
    private PrizeStatusEnum targetPrizeStatus;

    /**
     * 人员id列表
     */
    @NotNull(message = "人员id列表不能为空")
    private List<Long> userIds;

    /**
     * 人员目标状态
     */
    @NotNull(message = "人员目标状态不能为空")
    private UserStatusEnum targetUserStatus;

}
