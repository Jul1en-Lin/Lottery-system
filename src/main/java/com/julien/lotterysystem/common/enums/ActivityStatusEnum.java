package com.julien.lotterysystem.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ActivityStatusEnum {
    /** 初始化 */
    START(1,"正在进行"),
    /** 已被抽取 */
    END(2,"已结束");

    private Integer code;
    private final String msg;

    public static ActivityStatusEnum forName(String name) {
        for (ActivityStatusEnum param : ActivityStatusEnum.values()) {
            if (param.name().equalsIgnoreCase(name)) {
                return param;
            }
        }
        return null;
    }
}
