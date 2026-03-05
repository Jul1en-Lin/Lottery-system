package com.julien.lotterysystem.entity.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    /** 主键 */
    private Long id;
    /** 创建时间 */
    private LocalDateTime gmtCreate;
    /** 更新时间 */
    private LocalDateTime gmtModified;
    /** 用户姓名 */
    private String userName;
    /** 邮箱 */
    private String email;
    /** 手机号 */
    private String phoneNumber;
    /** 登录密码（可为空） */
    private String password;
    /** 用户身份 */
    private String identity;
}