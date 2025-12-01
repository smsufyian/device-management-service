package com.devices.domain;

import lombok.Getter;

@Getter
public class DeviceFieldLockedException extends RuntimeException {
    
    private final String fieldName;
    private final DeviceStatus currentState;

    public DeviceFieldLockedException(String fieldName, DeviceStatus currentState) {
        super("Cannot update '%s' field when device is in %s state".formatted(fieldName, currentState));
        this.fieldName = fieldName;
        this.currentState = currentState;
    }
}
