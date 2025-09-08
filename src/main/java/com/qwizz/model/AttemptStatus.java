package com.qwizz.model;

public enum AttemptStatus {
    STARTED("STARTED"),
    COMPLETED("COMPLETED"),
    ABANDONED("ABANDONED");

    private final String value;

    AttemptStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
