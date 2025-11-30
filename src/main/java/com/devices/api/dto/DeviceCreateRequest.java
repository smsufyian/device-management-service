package com.devices.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeviceCreateRequest(
        @NotBlank @Size(min = 1, max = 100) String name,
        @NotBlank @Size(min = 1, max = 50) String brand
) {
}
