package com.julien.lotterysystem.common.enums;


import lombok.Getter;

@Getter
public enum UserIdentity {
    ADMIN("ADMIN"),
    NORMAL("NORMAL");
    private final String identity;

    UserIdentity(String identity) {
        this.identity = identity;
    }
}
