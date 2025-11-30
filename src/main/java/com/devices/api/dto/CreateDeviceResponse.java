package com.devices.api.dto;

import com.devices.model.DeviceState;
import jakarta.annotation.Nonnull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateDeviceResponse(
        @Nonnull UUID id,
        @Nonnull String name,
        @Nonnull String brand,
        @Nonnull DeviceState state,
        @Nonnull OffsetDateTime creationTime
) {}
