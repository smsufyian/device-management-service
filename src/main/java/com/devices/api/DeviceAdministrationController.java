package com.devices.api;

import com.devices.api.dto.*;
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
public class DeviceAdministrationController implements DeviceAdministrationAPI {

    private final DeviceService deviceService;

    public DeviceAdministrationController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeviceResponse createDevice(@Valid @RequestBody CreateDeviceRequest request) {
        return deviceService.create(request);
    }

    @Override
    @GetMapping("/{id}")
    public DeviceResponse getByDeviceId(@PathVariable UUID id) {
        return deviceService.findById(id);
    }

    @Override
    @GetMapping
    public List<DeviceResponse> getAll(@Valid DeviceFilterRequest filter) {
        return deviceService.findDevices(filter);
    }

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDevice(@PathVariable UUID id) {
        deviceService.deleteById(id);
    }

    @Override
    @PutMapping(value = "/{id}")
    public DeviceResponse updateDevice(
            @PathVariable UUID id,
            @Valid @RequestBody PutDeviceRequest request
    ) {
        return deviceService.updateFull(id, request);
    }

    @Override
    @PatchMapping(value = "/{id}")
    public DeviceResponse patchDevice(
            @PathVariable UUID id,
            @Valid @RequestBody PatchDeviceRequest patch
    ) {
        return deviceService.updatePartial(id, patch);

    }
}
