package com.julien.lotterysystem.entity.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class UserLoginResponse {
    // Jwt令牌
    private String token;
    // 用户邮箱
    private String email;
}
