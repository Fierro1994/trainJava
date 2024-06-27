package com.example.train.entity;

public enum RoleName {
    ROLE_USER("Пользователь"),
    ROLE_MODERATOR("Модератор"),
    ROLE_ADMIN("Администратор");

    private final String displayName;

    RoleName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}