package com.julien.lotterysystem.entity.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class EmailRegisterRequest {

    @NotBlank(message = "邮箱不能为空")
    private String email;

    @NotBlank(message = "验证码不能为空")
    private String code;

    @NotBlank(message = "用户姓名不能为空")
    private String userName;

    @NotBlank(message = "手机号不能为空")
    @Length(min = 11, max = 11, message = "手机号长度必须为11位")
    private String phoneNumber;

    /** 登录密码（非管理员可为空） */
    @Length(min = 6, message = "密码长度必须为6位以上")
    private String password;

    @NotBlank(message = "用户身份不能为空")
    private String identity;
}
