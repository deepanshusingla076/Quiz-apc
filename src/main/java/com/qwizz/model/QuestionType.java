package com.qwizz.model;

public enum QuestionType {
    MULTIPLE_CHOICE("MULTIPLE_CHOICE"),
    TRUE_FALSE("TRUE_FALSE"),
    SHORT_ANSWER("SHORT_ANSWER");

    private final String value;

    QuestionType(String value) {
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
