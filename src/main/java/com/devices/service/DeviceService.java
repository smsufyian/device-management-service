package com.devices.service;

import com.devices.api.dto.CreateDeviceRequest;
import com.devices.api.dto.DeviceFilterRequest;
import com.devices.api.dto.DeviceResponse;
import com.devices.api.dto.PutDeviceRequest;
import com.devices.api.DeviceAdministrationMapper;
import com.devices.model.DeviceStatus;
import com.devices.persistence.Device;
import com.devices.persistence.DeviceRepository;
import com.devices.persistence.DeviceSpecification;
import com.devices.service.exception.DeviceNotFoundException;
import com.devices.service.exception.DeviceInUseException;
import com.devices.service.exception.DeviceFieldLockedException;
import com.devices.service.exception.VersionConflictException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceAdministrationMapper deviceAdministrationMapper;

    @PersistenceContext
    private EntityManager entityManager;

    public DeviceService(DeviceRepository deviceRepository, DeviceAdministrationMapper deviceAdministrationMapper) {
        this.deviceRepository = deviceRepository;
        this.deviceAdministrationMapper = deviceAdministrationMapper;
    }

    @Transactional
    public DeviceResponse create(CreateDeviceRequest request) {
        Device device = deviceAdministrationMapper.toEntity(request);
        Device savedDevice = deviceRepository.save(device);
        deviceRepository.flush();
        entityManager.refresh(savedDevice);
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
                .orElseThrow(() -> new DeviceNotFoundException("Device with id %s not found".formatted(id)));
        return deviceAdministrationMapper.toResponse(device);
    }

    @Transactional
    public void deleteById(UUID id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Device with id %s not found".formatted(id)));

        // Only allow deletion when AVAILABLE or INACTIVE
        switch (device.getState()) {
            case IN_USE -> throw new DeviceInUseException("Device with id %s is in use and cannot be deleted".formatted(id));
            case AVAILABLE, INACTIVE -> {
                deviceRepository.deleteById(id);
                deviceRepository.flush();
            }
        }
    }

    @Transactional
    public DeviceResponse updateFull(UUID id, PutDeviceRequest request) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Device with id %s not found".formatted(id)));

        if (device.getState() == DeviceStatus.IN_USE) {
            if (!device.getName().equals(request.name())) {
                throw new DeviceFieldLockedException("Cannot update 'name' field when device is in IN_USE state",
                        "name", device.getState());
            }
            if (!device.getBrand().equals(request.brand())) {
                throw new DeviceFieldLockedException("Cannot update 'brand' field when device is in IN_USE state",
                        "brand", device.getState());
            }
        }

        device.setName(request.name());
        device.setBrand(request.brand());
        device.setState(request.state());

        try {
            Device saved = deviceRepository.saveAndFlush(device);
            entityManager.refresh(saved);
            return deviceAdministrationMapper.toResponse(saved);
        } catch (OptimisticLockingFailureException e) {
            throw new VersionConflictException("Version conflict detected for device %s".formatted(id));
        }
    }

    @Transactional
    public DeviceResponse updatePartial(UUID id, com.devices.api.dto.PatchDeviceRequest patch) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Device with id %s not found".formatted(id)));

        if (patch == null || (patch.name() == null && patch.brand() == null && patch.state() == null)) {
            throw new IllegalArgumentException("PATCH request must contain at least one updatable field");
        }

        // Enforce field locks when device is IN_USE
        if (device.getState() == DeviceStatus.IN_USE) {
            if (patch.name() != null && !device.getName().equals(patch.name())) {
                throw new DeviceFieldLockedException("Cannot update 'name' field when device is in IN_USE state",
                        "name", device.getState());
            }
            if (patch.brand() != null && !device.getBrand().equals(patch.brand())) {
                throw new DeviceFieldLockedException("Cannot update 'brand' field when device is in IN_USE state",
                        "brand", device.getState());
            }
        }

        // Apply partial update via MapStruct (ignoring nulls)
        deviceAdministrationMapper.updateDeviceFromPatch(patch, device);

        try {
            Device saved = deviceRepository.saveAndFlush(device);
            entityManager.refresh(saved);
            return deviceAdministrationMapper.toResponse(saved);
        } catch (OptimisticLockingFailureException e) {
            throw new VersionConflictException("Version conflict detected for device %s".formatted(id));
        }
    }

    @Transactional(readOnly = true)
    public String computeEtag(UUID id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Device with id %s not found".formatted(id)));
        return computeEtagInternal(device);
    }

    private String computeEtagInternal(Device device) {
        long version = device.getVersion() == null ? 0L : device.getVersion();
        String input = device.getId() + ":" + version;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return Long.toHexString(version);
        }
    }
}
