package com.julien.lotterysystem.entity.dto;

import com.julien.lotterysystem.common.enums.UserStatusEnum;
import lombok.Data;

@Data
public class ActivityUserDto {
    // 用户id
    private Long userId;
    // 用户名
    private String userName;
    // 用户状态
    private UserStatusEnum userStatus;
}
