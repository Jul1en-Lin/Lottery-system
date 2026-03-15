package com.julien.lotterysystem.entity.dto;

import lombok.Data;

@Data
public class ActivityPrizeDto {
    // 奖品id
    private Long prizeId;
    // 奖品名称
    private String prizeName;
    // 奖品数量
    private Long prizeAmount;
    // 奖品等级
    private String prizeTiers;
    // 奖品状态
    private String prizeStatus;
}
