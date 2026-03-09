package com.julien.lotterysystem.entity.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailRegisterRequest extends UserRegisterRequest {
    @NotBlank(message = "验证码不能为空")
    private String code;
}
