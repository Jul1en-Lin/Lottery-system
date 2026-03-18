package com.julien.lotterysystem.entity.dto;

import com.julien.lotterysystem.common.enums.PrizeStatusEnum;
import com.julien.lotterysystem.common.enums.PrizeTiersEnum;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ActivityPrizeDto {
    // 奖品id
    private Long prizeId;
    // 奖品名称
    private String prizeName;
    // 奖品价格
    private BigDecimal price;
    // 奖品数量
    private Long prizeAmount;
    // 奖品图片url
    private String imageUrl;
    // 奖品等级
    private PrizeTiersEnum prizeTiers;
    // 奖品状态
    private PrizeStatusEnum prizeStatus;
}
