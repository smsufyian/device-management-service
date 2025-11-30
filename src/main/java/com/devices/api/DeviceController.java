package com.devices.api;

import com.devices.api.dto.CreateDeviceRequest;
import com.devices.api.dto.CreateDeviceResponse;
import com.devices.service.DeviceService;
import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping
    public ResponseEntity<CreateDeviceResponse> create(@Valid @RequestBody @Nonnull CreateDeviceRequest request) {
        CreateDeviceResponse response = deviceService.create(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }
}
