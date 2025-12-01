package com.devices.api.dto;

import com.devices.model.DeviceStatus;
import jakarta.validation.constraints.Size;

public record DeviceFilterRequest(
        @Size(max = 50, message = "Brand must not exceed 50 characters")
        String brand,

        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        DeviceStatus status) {
    public boolean isEmpty() {
        return brand == null && name == null && status == null;
    }
}
