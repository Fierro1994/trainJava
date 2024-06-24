package com.example.train.entity;

import lombok.Getter;

@Getter
public enum CategoryNames {
    PRINCIPLES("Основы"),
    JAVA_CORE("Java Core"),
    JAVA_COLLECTIONS("Java Collections"),
    JAVA_EE("Java EE"),
    ALGORITHMS("Алгоритмы"),
    PATTERNS("Паттерны"),
    MULTITHREADING("Многопоточность"),
    BD("Базы данных"),
    SPRING("Spring"),
    HIBERNATE("Hibernate"),
    SERVLETS("Servlets");

    private final String displayName;

    CategoryNames(String displayName) {
        this.displayName = displayName;
    }

}
