package com.julien.lotterysystem.common.enums;


import lombok.Getter;

@Getter
public enum UserIdentityEnum {
    ADMIN("ADMIN"),
    NORMAL("NORMAL");
    private final String identity;

    UserIdentityEnum(String identity) {
        this.identity = identity;
    }

    // 根据字符串获取枚举值
    private static UserIdentityEnum getIdentity(String identity) {
        for (UserIdentityEnum userIdentityEnum : UserIdentityEnum.values()) {
            if (userIdentityEnum.identity.equalsIgnoreCase(identity)) {
                return userIdentityEnum;
            }
        }
        return null;
    }
}
