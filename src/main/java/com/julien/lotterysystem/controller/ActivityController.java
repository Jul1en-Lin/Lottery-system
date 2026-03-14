package com.julien.lotterysystem.controller;

import com.julien.lotterysystem.entity.request.CreateActivityRequest;
import com.julien.lotterysystem.entity.response.CreateActivityResponse;
import com.julien.lotterysystem.service.ActivityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/activity")
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
}
