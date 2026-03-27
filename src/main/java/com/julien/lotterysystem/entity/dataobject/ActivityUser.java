package com.julien.lotterysystem.entity.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动关联用户表
 */
@Data
@TableName("activity_user")
public class ActivityUser {
    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /** 创建时间 */
    private LocalDateTime gmtCreate;
    /** 更新时间 */
    private LocalDateTime gmtModified;
    /** 活动ID */
    private Long activityId;
    /** 用户ID */
    private Long userId;
    /** 用户名 */
    private String userName;
    /** 用户状态: 初始化 / 已被抽取 */
    private String status;
}