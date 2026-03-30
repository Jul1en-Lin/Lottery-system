package com.julien.lotterysystem.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.julien.lotterysystem.entity.dataobject.Activity;
import com.julien.lotterysystem.entity.dataobject.ActivityPrize;
import com.julien.lotterysystem.entity.dto.ActivityDetailDto;
import com.julien.lotterysystem.entity.dto.ConvertActivityStatusDTO;
import com.julien.lotterysystem.entity.request.CreateActivityRequest;
import com.julien.lotterysystem.entity.response.ActivityListResponse;
import com.julien.lotterysystem.entity.response.CreateActivityResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface ActivityService {

    /**
     * 创建活动
     * @return 活动id
     */
    CreateActivityResponse create(@Valid CreateActivityRequest request);

    /**
     * 创建活动模块的创建活动缓存
     * 缓存活动详情到 Redis 中
     * @param detailDto 活动详情
     */
    void cacheActivity(ActivityDetailDto detailDto);

    /**
     * 抽奖模块的更新缓存
     * 将扭转状态后的活动数据更新缓存到 Redis 中
     * @param activityStatusDTO 状态扭转数据
     */
    void cacheActivityStatus(ConvertActivityStatusDTO activityStatusDTO);

    /**
     * 翻页查询活动列表
     * @param page
     * @return
     */
    ActivityListResponse queryActivityList(Page<Activity> page);

    /**
     * 查询活动详情
     * @param activityId
     * @return
     */
    ActivityDetailDto getActivityDetail(Long activityId);


}
