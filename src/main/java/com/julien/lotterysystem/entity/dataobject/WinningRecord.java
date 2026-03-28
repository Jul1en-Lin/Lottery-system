package com.julien.lotterysystem.entity.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.julien.lotterysystem.mapper.handler.EncryptTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 中奖记录
 */
@Data
@TableName("winning_record")
public class WinningRecord {

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 创建时间 */
    private LocalDateTime gmtCreate;

    /** 更新时间 */
    private LocalDateTime gmtModified;

    // -----------活动模块-----------

    /** 活动 ID */
    private Long activityId;

    /** 活动名称 */
    private String activityName;

    // -----------奖品模块-----------

    /** 奖品 ID */
    private Long prizeId;

    /** 奖品名称 */
    private String prizeName;

    /** 奖品等级 */
    private String prizeTier;

    // -----------用户模块-----------

    /** 中奖用户 ID */
    private Long winnerId;

    /** 中奖用户姓名 */
    private String winnerName;

    /** 中奖用户邮箱 */
    private String winnerEmail;

    /** 中奖用户手机号 */
    @TableField(typeHandler = EncryptTypeHandler.class)
    private Encrypt winnerPhoneNumber;

    /** 中奖时间 */
    private Date winningTime;
}
