package com.julien.lotterysystem.entity.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePrizeRequest {
    /** 奖品名称 */
    @NotBlank(message = "奖品名称不能为空")
    private String name;

    /** 奖品描述 */
    @NotBlank(message = "奖品描述不能为空")
    private String description;

    /** 奖品价格 */
    @NotNull(message = "奖品价格不能为空")
    private BigDecimal price;

    /** 奖品id */
    private Long id;

}
