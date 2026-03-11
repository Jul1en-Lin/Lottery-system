package com.julien.lotterysystem.entity.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.julien.lotterysystem.mapper.handler.EncryptTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "user", autoResultMap = true)// autoResultMap：让 MyBatis-Plus typeHandler 之类的配置生效
public class User {
    /** 主键 */
    @TableId(value = "id",type = IdType.AUTO)
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
    @TableField(typeHandler = EncryptTypeHandler.class)
    private Encrypt phoneNumber;
    /** 登录密码（可为空） */
    private String password;
    /** 用户身份 */
    private String identity;
}