package com.qwizz.model;

public enum Role {
    TEACHER("TEACHER"),
    STUDENT("STUDENT"),
    ADMIN("ADMIN");

    private final String value;

    Role(String value) {
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
