package com.devices.service.exception;

import com.devices.model.DeviceStatus;

public class DeviceFieldLockedException extends RuntimeException {
    private final String fieldName;
    private final DeviceStatus currentState;

    public DeviceFieldLockedException(String message, String fieldName, DeviceStatus currentState) {
        super(message);
        this.fieldName = fieldName;
        this.currentState = currentState;
    }

    public String getFieldName() { return fieldName; }
    public DeviceStatus getCurrentState() { return currentState; }
}
