package com.devices.service.exception;

public class ImmutableFieldViolationException extends RuntimeException {
    private final String fieldName;

    public ImmutableFieldViolationException(String message, String fieldName) {
        super(message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
