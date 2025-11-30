package com.devices.service.exception;

/**
 * Thrown when a device resource cannot be found.
 */
public class DeviceNotFoundException extends RuntimeException {
    public DeviceNotFoundException(String message) {
        super(message);
    }
}
