package com.julien.lotterysystem.service.activitystatus.impl;

import com.julien.lotterysystem.common.constants.ErrorConstants;
import com.julien.lotterysystem.common.exception.LotteryException;
import com.julien.lotterysystem.entity.dto.ConvertActivityStatusDTO;
import com.julien.lotterysystem.service.ActivityService;
import com.julien.lotterysystem.service.activitystatus.ActivityStatusManager;
import com.julien.lotterysystem.service.activitystatus.operator.AbstractActivityOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 设计模式：责任链 + 策略模式
 * 解决扭转状态时的依赖问题与可拓展性问题
 * 依赖问题：如果需要添加新的状态操作器，需要修改责任链，导致代码维护困难
 * 可拓展性问题：如果需要添加新的状态操作器，只需要添加新的策略类，即可满足可拓展性需求，无需修改责任链
 *
 */
@Slf4j
@Component
public class ActivityStatusManagerImpl implements ActivityStatusManager {
    /**
     * Spring自动扫描容器中所有 AbstractActivityOperator 类型的 Bean，并将它们组装成一个 Map。
     * 默认情况下，Map 的 Key 是 Bean 的名称，Value 是对应的 Bean 实例
     */
    @Autowired
    private Map<String, AbstractActivityOperator> operatorMap;
    @Autowired
    private ActivityService activityService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlerEvent(ConvertActivityStatusDTO activityStatusDTO) {
        if (operatorMap == null) {
            log.error("operatorMap 为空！");
            throw new LotteryException(ErrorConstants.OPERATOR_MAP_IS_NULL);
        }
        // 浅拷贝操作器映射，避免修改原始映射，副本 Map 结构与原始 Map 相同
        Map<String, AbstractActivityOperator> currentMap = new HashMap<>(operatorMap);
        // 安排责任链
        Boolean isStatusChanged = false;
        // 先扭转奖品和人员状态
        isStatusChanged = processConvertStatus(activityStatusDTO,currentMap,0);
        // 后再扭转活动状态
        isStatusChanged = processConvertStatus(activityStatusDTO,currentMap,1)
                                    || isStatusChanged;

        if (isStatusChanged) {
            // 更新缓存活动状态
            activityService.cacheActivityStatus(activityStatusDTO);
        }
    }

    /**
     * 处理状态扭转
     * @param activityStatusDTO 构造状态扭转DTO
     * @param currentMap 副本 Map
     * @param sequence 执行顺序
     * @return
     */
    private Boolean processConvertStatus(ConvertActivityStatusDTO activityStatusDTO,
                                      Map<String, AbstractActivityOperator> currentMap,
                                      int sequence) {
        Boolean update = false;
        // 遍历副本 Map，根据活动状态判断是否需要执行操作器
        Iterator<Map.Entry<String, AbstractActivityOperator>> iterator = currentMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, AbstractActivityOperator> entry = iterator.next();
            AbstractActivityOperator operator = entry.getValue();
            // 判断执行顺序是否匹配
            if (operator.sequence() != sequence || !operator.isNeedConvert(activityStatusDTO)) {
                log.info("当前操作器 {} 不需要执行", operator);
                continue;
            }

            if (! operator.convert(activityStatusDTO)) {
                log.info("{}状态扭转失败", operator.getClass().getName());
                throw new LotteryException(ErrorConstants.CONVERT_STATUS_FAILED);
            }
            // 删除已执行的操作器
            iterator.remove();
            update = true;
        }
        return update;
    }
}
