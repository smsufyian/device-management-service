package com.devices.api.dto;

import com.devices.model.DeviceState;
import java.time.OffsetDateTime;
import java.util.UUID;

public record DeviceResponse(
        UUID id,
        String name,
        String brand,
        DeviceState state,
        OffsetDateTime creationTime
) {}
