package com.julien.lotterysystem.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatusEnum {
    /** 初始化 */
    INIT(1,"初始化"),
    /** 已被抽取 */
    COMPLETED(2,"已被抽取");

    private Integer code;
    private final String msg;

    public static UserStatusEnum forName(String name) {
        for (UserStatusEnum param : UserStatusEnum.values()) {
            if (param.name().equalsIgnoreCase(name)) {
                return param;
            }
        }
        return null;
    }
}
