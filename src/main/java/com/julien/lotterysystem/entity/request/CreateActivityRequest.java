package com.julien.lotterysystem.entity.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateActivityRequest {
    // 活动名
    @NotBlank(message = "活动名不能为空")
    private String name;
    // 活动描述
    @NotBlank(message = "活动描述不能为空")
    private String description;
    // 活动关联用户列表
    @NotEmpty(message = "活动关联用户列表不能为空")
    private List<@Valid ActivityUserList> activityUserList;
    // 活动关联奖品列表
    @NotEmpty(message = "活动关联奖品列表不能为空")
    private List<@Valid ActivityPrizeList> activityPrizeList;
}
