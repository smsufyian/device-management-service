package com.devices.domain;

import lombok.Getter;
import java.util.UUID;

@Getter
public class DeviceNotFoundException extends RuntimeException {

    private final UUID deviceId;

    public DeviceNotFoundException(UUID deviceId) {
        super("Device with id %s not found".formatted(deviceId));
        this.deviceId = deviceId;
    }
}
