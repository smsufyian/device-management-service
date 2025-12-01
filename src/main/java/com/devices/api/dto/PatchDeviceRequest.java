package com.devices.api.dto;

import com.devices.model.DeviceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;

@Schema(name = "PatchDeviceRequest", description = "Partial update payload for a device (only provided fields are updated)")
public record PatchDeviceRequest(
        @Schema(description = "Device name", example = "New name", maxLength = 100, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Nullable
        @Size(max = 100, message = "Device name must not exceed 100 characters")
        String name,

        @Schema(description = "Device brand", example = "Samsung", maxLength = 50, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Nullable
        @Size(max = 50, message = "Device brand must not exceed 50 characters")
        String brand,

        @Schema(description = "Device state", allowableValues = {"AVAILABLE","IN_USE","INACTIVE"}, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Nullable
        DeviceStatus state
) {}
