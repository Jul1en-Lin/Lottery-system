package com.julien.lotterysystem.entity.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class EmailRegisterRequest extends UserRequest {
    @NotBlank(message = "验证码不能为空")
    private String code;
}
