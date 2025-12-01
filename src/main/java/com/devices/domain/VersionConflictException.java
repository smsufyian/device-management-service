package com.devices.domain;

import lombok.Getter;
import java.util.UUID;

@Getter
public class VersionConflictException extends RuntimeException {

    private final UUID deviceId;

    public VersionConflictException(UUID deviceId) {
        super("Version conflict detected for device %s".formatted(deviceId));
        this.deviceId = deviceId;
    }
}
