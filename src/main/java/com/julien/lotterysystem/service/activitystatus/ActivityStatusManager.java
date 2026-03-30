package com.julien.lotterysystem.service.activitystatus;

import com.julien.lotterysystem.entity.dto.ConvertActivityStatusDTO;

public interface ActivityStatusManager {

    /**
     * 处理活动相关状态转换，更新缓存
     */
    void handlerEvent(ConvertActivityStatusDTO activityStatusDTO);


    /**
     * 回滚活动相关状态转换，更新缓存
     */
    void rollBackEvent(ConvertActivityStatusDTO activityStatusDTO);
}
