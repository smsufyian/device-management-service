package com.devices.service.exception;

/**
 * Thrown when the device cannot be found.
 */
public class DeviceNotFoundException extends RuntimeException {
    public DeviceNotFoundException(String message) {
        super(message);
    }
}
