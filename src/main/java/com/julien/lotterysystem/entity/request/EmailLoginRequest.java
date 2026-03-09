package com.julien.lotterysystem.entity.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailLoginRequest {

    @NotBlank(message = "邮箱不能为空")
    private String email;

    @NotBlank(message = "验证码不能为空")
    private String code;
}
