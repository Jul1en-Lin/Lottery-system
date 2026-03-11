package com.julien.lotterysystem.entity.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PrizeInfoResponse {
    // 奖品id
    private Long id;
    // 奖品名
    private String name;
    // 图片索引
    private String imageUrl;
    // 价格
    private BigDecimal price;
    // 描述
    private String description;
}
