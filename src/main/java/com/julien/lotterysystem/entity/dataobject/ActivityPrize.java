package com.julien.lotterysystem.entity.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动关联奖品表
 */
@Data
@TableName("activity_prize")
public class ActivityPrize {
    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /** 创建时间 */
    private LocalDateTime gmtCreate;
    /** 更新时间 */
    private LocalDateTime gmtModified;
    /** 活动ID */
    private Long activityId;
    /** 奖品ID */
    private Long prizeId;
    /** 奖品数量 */
    private Long prizeAmount;
    /** 奖品档位 */
    private String prizeTiers;
    /** 奖品状态——初始化/已被抽取 */
    private String status;
}