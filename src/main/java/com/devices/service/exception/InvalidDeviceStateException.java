package com.devices.service.exception;

/**
 * Thrown when a provided device state is invalid for the requested operation or does not
 * conform to the allowed set of states.
 */
public class InvalidDeviceStateException extends RuntimeException {
    public InvalidDeviceStateException(String message) {
        super(message);
    }
}
