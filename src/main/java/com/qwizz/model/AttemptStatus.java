package com.qwizz.model;

public enum AttemptStatus {
    STARTED("STARTED"),
    IN_PROGRESS("IN_PROGRESS"),
    COMPLETED("COMPLETED"),
    ABANDONED("ABANDONED"),
    EXPIRED("EXPIRED");

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
