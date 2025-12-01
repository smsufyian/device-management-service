package com.devices.domain;

import lombok.Getter;

@Getter
public class ImmutableFieldViolationException extends RuntimeException {
    
    private final String fieldName;

    public ImmutableFieldViolationException(String fieldName) {
        super("Field '%s' is immutable and cannot be changed".formatted(fieldName));
        this.fieldName = fieldName;
    }
}
