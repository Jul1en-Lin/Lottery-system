package com.julien.lotterysystem.entity.dto;

import lombok.Data;

@Data
public class ActivityUserDto {
    // 用户id
    private Long userId;
    // 用户名
    private String userName;
    // 用户状态
    private String userStatus;
}
