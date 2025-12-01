package com.devices.api;

import com.devices.api.dto.CreateDeviceRequest;
import com.devices.api.dto.CreateDeviceResponse;
import com.devices.api.dto.DeviceFilterRequest;
import com.devices.api.dto.DeviceResponse;
import com.devices.api.dto.PutDeviceRequest;
import com.devices.api.dto.PatchDeviceRequest;
import com.devices.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/devices")
@Validated
public class DeviceController implements DeviceAPI {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
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

    @PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<DeviceResponse> updateDevice(
            @PathVariable UUID id,
            @RequestHeader(value =  "If-Match", required = false) String ifMatch,
            @Valid @RequestBody PutDeviceRequest request
    ) {
        DeviceResponse response = deviceService.updateFull(id, request, ifMatch);
        String etag = '"' + deviceService.computeEtag(response.id()) + '"';
        return ResponseEntity.ok()
                .header("ETag", etag)
                .body(response);
    }

    @PatchMapping(value = "/{id}", consumes = {"application/merge-patch+json", "application/json"}, produces = "application/json")
    public ResponseEntity<DeviceResponse> patchDevice(
            @PathVariable UUID id,
            @RequestHeader(value = "If-Match", required = false) String ifMatch,
            @Valid @RequestBody PatchDeviceRequest patch
    ) {
        DeviceResponse response = deviceService.updatePartial(id, patch, ifMatch);
        String etag = '"' + deviceService.computeEtag(response.id()) + '"';
        return ResponseEntity.ok()
                .header("ETag", etag)
                .body(response);
    }
}
