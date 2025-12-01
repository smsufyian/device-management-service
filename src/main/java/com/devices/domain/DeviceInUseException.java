package com.devices.domain;

import lombok.Getter;
import java.util.UUID;

@Getter
public class DeviceInUseException extends RuntimeException {

    private final UUID deviceId;

    public DeviceInUseException(UUID deviceId) {
        super("Device with id %s is in use and cannot be deleted".formatted(deviceId));
        this.deviceId = deviceId;
    }
}
