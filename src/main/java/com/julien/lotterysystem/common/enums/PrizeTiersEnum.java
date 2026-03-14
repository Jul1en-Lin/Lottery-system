package com.julien.lotterysystem.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PrizeTiersEnum {
    TIER_SPECIAL(0,"特等奖"),
    TIER_1(1,"一等奖"),
    TIER_2(2,"二等奖"),
    TIER_3(3,"三等奖");


    private Integer code;
    private String msg;

    public static PrizeTiersEnum forName(String name) {
        for (PrizeTiersEnum param : PrizeTiersEnum.values()) {
            if (param.name().equalsIgnoreCase(name)) {
                return param;
            }
        }
        return null;
    }
}
