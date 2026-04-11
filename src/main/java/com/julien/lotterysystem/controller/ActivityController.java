package com.julien.lotterysystem.controller;

import com.julien.lotterysystem.entity.dataobject.Activity;
import com.julien.lotterysystem.entity.dto.ActivityDetailDto;
import com.julien.lotterysystem.entity.request.CreateActivityRequest;
import com.julien.lotterysystem.entity.response.ActivityListResponse;
import com.julien.lotterysystem.entity.response.CreateActivityResponse;
import com.julien.lotterysystem.service.ActivityService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.*;

@Slf4j
@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    /**
     * 创建活动
     */
    @PostMapping("/create")
    public CreateActivityResponse create(@Valid @RequestBody CreateActivityRequest request) {
        return activityService.create(request);
    }

    /**
     * 翻页查询活动列表
     * @param page 分页参数
     */
    @GetMapping("/queryList")
    public ActivityListResponse queryActivityList(Page<Activity> page) {
        return activityService.queryActivityList(page);
    }

    /**
     * 查询活动详情
     * @param activityId 活动id
     * @return 活动详情
     */
    @GetMapping("/getDetail")
    public ActivityDetailDto getActivityDetail(@RequestParam("activityId") Long activityId) {
        return activityService.getActivityDetail(activityId);
    }
}
