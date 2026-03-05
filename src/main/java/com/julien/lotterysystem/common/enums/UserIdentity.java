package com.julien.lotterysystem.common.enums;


import lombok.Getter;

@Getter
public enum UserIdentity {
    ADMIN("管理员"),
    NORMAL("普通用户");
    private final String identity;

    UserIdentity(String identity) {
        this.identity = identity;
    }
}
