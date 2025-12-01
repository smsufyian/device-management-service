package com.devices.api.dto;

import com.devices.model.DeviceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "PutDeviceRequest", description = "Full update payload for a device (all mutable fields required)")
public record PutDeviceRequest(
        @Schema(description = "Device name", example = "Updated Thermostat", maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Device name must not be blank")
        @Size(max = 100, message = "Device name must not exceed 100 characters")
        String name,

        @Schema(description = "Device brand", example = "Samsung", maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Device brand must not be blank")
        @Size(max = 50, message = "Device brand must not exceed 50 characters")
        String brand,

        @Schema(description = "Device state", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"AVAILABLE", "IN_USE", "INACTIVE"})
        @NotNull(message = "Device state must be provided")
        DeviceStatus state
) {
}
