package com.julien.lotterysystem.entity.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActivityPrizeList {
    // 奖品id
    @NotNull(message = "奖品id不能为空")
    private Long prizeId;
    // 奖品数量
    @NotNull(message = "奖品数量不能为空")
    private Long prizeAmount;
    // 奖品等级
    @NotBlank(message = "奖品等级不能为空")
    private String prizeTiers;
}
