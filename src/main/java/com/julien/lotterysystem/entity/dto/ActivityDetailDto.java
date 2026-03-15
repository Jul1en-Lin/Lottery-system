package com.julien.lotterysystem.entity.dto;

import lombok.Data;

import java.util.List;

/**
 * 整合活动详情,包含活动关联的奖品列表和用户列表
 * 缓存于 Redis 中
 */
@Data
public class ActivityDetailDto {
    /** 活动ID */
    private Long acivityId;
    /** 活动名称 */
    private String activityName;
    /** 活动描述 */
    private String description;
    /** 活动状态 */
    private String status;
    /** 活动奖品Dto列表 */
    List<ActivityPrizeDto> activityPrizeList;
    /** 活动用户Dto列表 */
    List<ActivityUserDto> activityUserList;
}
