package com.julien.lotterysystem.entity.dto;

import com.julien.lotterysystem.common.enums.ActivityStatusEnum;
import com.julien.lotterysystem.common.enums.PrizeStatusEnum;
import com.julien.lotterysystem.common.enums.UserStatusEnum;
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
    private Long activityId;

    /**
     * 活动目标状态
     */
    private ActivityStatusEnum targetActivityStatus;

    /**
     * 奖品id
     */
    private Long prizeId;

    /**
     * 奖品目标状态
     */
    private PrizeStatusEnum targetPrizeStatus;

    /**
     * 人员id列表
     */
    private List<Long> userIds;

    /**
     * 人员目标状态
     */
    private UserStatusEnum targetUserStatus;

}
