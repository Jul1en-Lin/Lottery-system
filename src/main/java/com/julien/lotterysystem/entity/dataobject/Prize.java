package com.julien.lotterysystem.entity.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("prize")
public class Prize {
    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /** 奖品名称 */
    private String name;
    /** 创建时间 */
    private LocalDateTime gmtCreate;
    /** 更新时间 */
    private LocalDateTime gmtModified;
    /** 奖品图片地址 */
    private String imageUrl;
    /** 奖品描述 */
    private String description;
    /** 奖品价格 */
    private BigDecimal price;
}
