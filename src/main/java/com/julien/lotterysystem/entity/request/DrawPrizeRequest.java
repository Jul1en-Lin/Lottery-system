package com.julien.lotterysystem.entity.request;

import com.julien.lotterysystem.common.enums.PrizeStatusEnum;
import com.julien.lotterysystem.common.enums.PrizeTiersEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


import java.util.Date;
import java.util.List;

@Data
public class DrawPrizeRequest {
     /**
     * 活动id
     */
    @NotNull(message = "活动id不能为空")
    private Long activityId;

    /**
     * 奖品id
     */
    @NotNull(message = "奖品id不能为空")
    private Long prizeId;

    /**
     * 中奖时间
     */
    @NotNull(message = "中奖时间不能为空")
    private Date winningTime;

    /**
     * 奖品等级
     */
    @NotNull(message = "奖品等级不能为空")
    private PrizeTiersEnum prizeTiers;

    /**
     * 中奖者列表
     */
    @NotEmpty(message = "中奖者列表不能为空")
    private List<@Valid Winner> winnerList;

    @Data
    public static class Winner {
        /**
         * 中奖者id
         */
        @NotNull(message = "中奖者id不能为空")
        private Long userId;

        /**
         * 中奖者姓名
         */
        @NotBlank(message = "中奖者姓名不能为空")
        private String userName;
    }

}
