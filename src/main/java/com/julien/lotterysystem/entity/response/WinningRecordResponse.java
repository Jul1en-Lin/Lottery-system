package com.julien.lotterysystem.entity.response;

import lombok.Data;

import java.util.Date;

@Data
public class WinningRecordResponse {
    /**
     * 中奖者id
     */
    private Long winnerId;

    /**
     * 中奖者姓名
     */
    private String winnerName;

    /**
     * 奖品名
     */
    private String prizeName;

    /**
     * 奖品等级
     */
    private String prizeTier;

    /**
     * 中奖时间
     */
    private Date winningTime;

}
