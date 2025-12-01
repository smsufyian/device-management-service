package com.devices.service;

import com.devices.api.DeviceAdministrationMapper;
import com.devices.api.dto.CreateDeviceRequest;
import com.devices.api.dto.DeviceFilterRequest;
import com.devices.api.dto.DeviceResponse;
import com.devices.api.dto.PutDeviceRequest;
import com.devices.domain.*;
import com.devices.repository.DeviceRepository;
import com.devices.repository.DeviceSpecification;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DeviceService {

    private static final String DEVICE_NOT_FOUND_MESSAGE = "Device with id %s not found";
    private static final String DEVICE_ALREADY_EXISTS_MESSAGE = "Device with name %s already exists";
    private static final String DEVICE_CANNOT_BE_DELETED_MESSAGE = "Device with id %s is in use and cannot be deleted";

    private final DeviceRepository deviceRepository;
    private final DeviceAdministrationMapper deviceAdministrationMapper;

    public DeviceService(DeviceRepository deviceRepository, DeviceAdministrationMapper deviceAdministrationMapper) {
        this.deviceRepository = deviceRepository;
        this.deviceAdministrationMapper = deviceAdministrationMapper;
    }

    @Transactional
    public DeviceResponse create(CreateDeviceRequest request) {
        Device device = deviceAdministrationMapper.toEntity(request);
        Device savedDevice = deviceRepository.save(device);
        return deviceAdministrationMapper.toResponse(savedDevice);
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> findDevices(DeviceFilterRequest filter) {

        List<Device> devices = deviceRepository.findAll(
                Specification
                        .where(DeviceSpecification.hasBrand(filter.brand()))
                        .and(DeviceSpecification.nameContains(filter.name()))
                        .and(DeviceSpecification.hasState(filter.status()))
        );

        return deviceAdministrationMapper.toResponseList(devices);
    }


    @Transactional(readOnly = true)
    public DeviceResponse findById(UUID id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(DEVICE_NOT_FOUND_MESSAGE.formatted(id)));
        return deviceAdministrationMapper.toResponse(device);
    }

    @Transactional
    public void deleteById(UUID id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(DEVICE_NOT_FOUND_MESSAGE.formatted(id)));

        // Only allow deletion when AVAILABLE or INACTIVE
        switch (device.getState()) {
            case IN_USE ->
                    throw new DeviceInUseException(DEVICE_CANNOT_BE_DELETED_MESSAGE.formatted(id));
            case AVAILABLE, INACTIVE -> {
                deviceRepository.deleteById(id);
                deviceRepository.flush();
            }
        }
    }

    @Transactional
    public DeviceResponse updateFull(UUID id, PutDeviceRequest request) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(DEVICE_CANNOT_BE_DELETED_MESSAGE.formatted(id)));

        try {
            device.updateDetails(request.name(), request.brand(), request.state());

            Device saved = deviceRepository.save(device);
            return deviceAdministrationMapper.toResponse(saved);
        } catch (OptimisticLockingFailureException e) {
            throw new VersionConflictException("Version conflict detected for device %s".formatted(id));
        }
    }

    @Transactional
    public DeviceResponse updatePartial(UUID id, com.devices.api.dto.PatchDeviceRequest patch) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(DEVICE_NOT_FOUND_MESSAGE.formatted(id)));

        if (patch == null || (patch.name() == null && patch.brand() == null && patch.state() == null)) {
            throw new IllegalArgumentException("PATCH request must contain at least one updatable field");
        }

        device.validatePartialUpdate(patch.name(), patch.brand());
        deviceAdministrationMapper.updateDeviceFromPatch(patch, device);

        try {
            Device saved = deviceRepository.save(device);
            return deviceAdministrationMapper.toResponse(saved);
        } catch (OptimisticLockingFailureException e) {
            throw new VersionConflictException("Version conflict detected for device %s".formatted(id));
        }
    }
}
