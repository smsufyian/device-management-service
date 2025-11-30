package com.devices.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "CreateDeviceRequest", description = "Payload to create a new device")
public record CreateDeviceRequest(
        @Schema(description = "Human-friendly device name", example = "Thermostat Living Room", maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Device name must not be blank")
        @Size(max = 100, message = "Device name must not exceed 100 characters")
        String name,

        @Schema(description = "Device manufacturer brand", example = "Nest", maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Device brand must not be blank")
        @Size(max = 50, message = "Device brand must not exceed 50 characters")
        String brand
) {
}
