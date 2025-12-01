package com.devices.api.dto;

import com.devices.model.DeviceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(name = "DeviceResponse", description = "Device details")
public record DeviceResponse(
        @Schema(description = "Unique identifier of the device", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull UUID id,

        @Schema(description = "Device name", example = "Thermostat Living Room", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull String name,

        @Schema(description = "Device brand", example = "Nest", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull String brand,

        @Schema(description = "Current state of the device", example = "AVAILABLE", allowableValues = {"AVAILABLE", "UNAVAILABLE"}, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull DeviceStatus state,

        @Schema(description = "ISO-8601 timestamp when the device was created", example = "2025-01-01T12:00:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull OffsetDateTime creationTime
) {
}
