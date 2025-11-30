package com.devices.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDeviceRequest(
        @NotBlank(message = "Device name must not be blank")
        @Size(max = 100, message = "Device name must not exceed 100 characters")
        String name,

        @NotBlank(message = "Device brand must not be blank")
        @Size(max = 50, message = "Device brand must not exceed 50 characters")
        String brand
) {
}
