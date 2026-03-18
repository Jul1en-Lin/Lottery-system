package com.julien.lotterysystem.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.julien.lotterysystem.entity.dataobject.Activity;
import com.julien.lotterysystem.entity.dto.ActivityDetailDto;
import com.julien.lotterysystem.entity.request.CreateActivityRequest;
import com.julien.lotterysystem.entity.response.ActivityListResponse;
import com.julien.lotterysystem.entity.response.CreateActivityResponse;
import jakarta.validation.Valid;

public interface ActivityService {

    /**
     * 创建活动
     * @return 活动id
     */
    CreateActivityResponse create(@Valid CreateActivityRequest request);

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
