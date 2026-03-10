package com.julien.lotterysystem.entity.response;

import lombok.Data;

/**
 * 列表人员信息
 */
@Data
public class UserInfoResponse {
    private Long id;
    private String email;
    private String identity;
    private String userName;
}
