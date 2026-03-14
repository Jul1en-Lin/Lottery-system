package com.julien.lotterysystem.service;


import com.julien.lotterysystem.entity.request.CreateActivityRequest;
import com.julien.lotterysystem.entity.response.CreateActivityResponse;
import jakarta.validation.Valid;

public interface ActivityService {

    /**
     * 创建活动
     * @return 活动id
     */
    CreateActivityResponse create(@Valid CreateActivityRequest request);
}
