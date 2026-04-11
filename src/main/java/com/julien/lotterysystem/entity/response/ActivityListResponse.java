package com.julien.lotterysystem.entity.response;

import lombok.Data;

import java.util.List;

@Data
public class ActivityListResponse {
    /**
     * 活动总数
     */
    private Long total;
    /**
     * 当前页活动列表
     */
    private List<ActivityInfo> records;

    @Data
    public static class ActivityInfo {
        /**
         * 活动id
         */
        private Long activityId;

        /**
         * 活动名称
         */
        private String activityName;

        /**
         * 活动描述
         */
        private String description;

        /**
         * 活动是否有效
         */
        private Boolean valid;

        /**
         * 奖品总数（该活动下所有奖品数量的总和）
         */
        private Long prizeCount;
    }
}
