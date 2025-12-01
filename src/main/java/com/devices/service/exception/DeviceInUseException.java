package com.devices.service.exception;

/**
 * Thrown when a delete operation is attempted on a device that is currently in use.
 */
public class DeviceInUseException extends RuntimeException {
    public DeviceInUseException(String message) {
        super(message);
    }
}
