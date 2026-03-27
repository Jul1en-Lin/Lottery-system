package com.julien.lotterysystem.service.activitystatus.operator;

import com.julien.lotterysystem.entity.dto.ConvertActivityStatusDTO;

/**
 * 策略模式：活动状态操作器抽象类
 * 使得每个实现类都能独立实现自己的操作逻辑，不会影响到其他实现类，无需修改责任链
 */
public abstract class AbstractActivityOperator {
    /**
     * 用于确定操作器在责任链中的执行顺序
     */
    public abstract int sequence();

    /**
     * 判断操作器是否需要状态扭转
     */
    public abstract boolean isNeedConvert(ConvertActivityStatusDTO activityStatusDTO);

    /**
     * 若判断需要则进行扭转
     * 状态扭转操作
     */
    public abstract Boolean convert(ConvertActivityStatusDTO activityStatusDTO);
}
