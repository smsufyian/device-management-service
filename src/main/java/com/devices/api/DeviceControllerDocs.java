package com.devices.api;

import com.devices.api.dto.CreateDeviceRequest;
import com.devices.api.dto.CreateDeviceResponse;
import com.devices.api.dto.DeviceFilterRequest;
import com.devices.api.dto.DeviceResponse;
import com.devices.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/devices")
@Validated
public class DeviceControllerDocs implements DeviceAPIDocs {

    private final DeviceService deviceService;

    public DeviceControllerDocs(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateDeviceResponse createDevice(@Valid @RequestBody CreateDeviceRequest request) {
        return deviceService.create(request);
    }

    @GetMapping("/{id}")
    public DeviceResponse getByDeviceId(@PathVariable UUID id) {
        return deviceService.findById(id);
    }

    @GetMapping
    public List<DeviceResponse> getAll(@Valid DeviceFilterRequest filter) {
        return deviceService.findDevices(filter);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDevice(@PathVariable UUID id) {
        deviceService.deleteById(id);
    }
}
