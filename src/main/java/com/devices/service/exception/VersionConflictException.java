package com.devices.service.exception;

public class VersionConflictException extends RuntimeException {
    public VersionConflictException(String message) {
        super(message);
    }
}
