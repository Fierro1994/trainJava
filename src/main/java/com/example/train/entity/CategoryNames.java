package com.example.train.entity;

import lombok.Getter;

@Getter
public enum CategoryNames {
    PRINCIPLES("Основы"),
    JAVA_CORE("Java Core"),
    JAVA_COLLECTIONS("Java Collections"),
    JAVA_EE("Java EE"),
    WEB("Web"),
    ALGORITHMS("Алгоритмы"),
    PATTERNS("Паттерны"),
    MIKRO_SERVICES("Микросервисы"),
    MULTITHREADING("Многопоточность"),
    BD("Базы данных"),
    SPRING("Spring"),
    HIBERNATE("Hibernate"),
    KAFKA("Kafka"),
    KUBERNETE("Kubernetes"),
    REDIS("Redis");
    private final String displayName;

    CategoryNames(String displayName) {
        this.displayName = displayName;
    }

}
