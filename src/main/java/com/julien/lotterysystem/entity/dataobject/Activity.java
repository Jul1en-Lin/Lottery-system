package com.julien.lotterysystem.entity.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("activity")
public class Activity {
    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /** 创建时间 */
    private LocalDateTime gmtCreate;
    /** 更新时间 */
    private LocalDateTime gmtModified;
    /** 活动名称 */
    private String activityName;
    /** 活动描述 */
    private String description;
    /** 活动状态 */
    private String status;
}
