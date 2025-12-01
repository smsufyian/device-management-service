package com.devices.service;

import com.devices.api.dto.CreateDeviceRequest;
import com.devices.api.dto.CreateDeviceResponse;
import com.devices.api.dto.DeviceFilterRequest;
import com.devices.api.dto.DeviceResponse;
import com.devices.api.dto.PutDeviceRequest;
import com.devices.api.mapper.DeviceMapper;
import com.devices.persistence.Device;
import com.devices.persistence.DeviceRepository;
import com.devices.persistence.DeviceSpecification;
import com.devices.service.exception.DeviceNotFoundException;
import com.devices.service.exception.DeviceInUseException;
import com.devices.service.exception.DeviceFieldLockedException;
import com.devices.service.exception.ImmutableFieldViolationException;
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
import java.util.Map;
import java.util.UUID;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;

    @PersistenceContext
    private EntityManager entityManager;

    public DeviceService(DeviceRepository deviceRepository, DeviceMapper deviceMapper) {
        this.deviceRepository = deviceRepository;
        this.deviceMapper = deviceMapper;
    }

    @Transactional
    public CreateDeviceResponse create(CreateDeviceRequest request) {
        Device device = deviceMapper.toEntity(request);
        Device savedDevice = deviceRepository.save(device);
        deviceRepository.flush();
        entityManager.refresh(savedDevice);
        return deviceMapper.toResponse(savedDevice);
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> findDevices(DeviceFilterRequest filter) {

        List<Device> devices = deviceRepository.findAll(
                Specification
                        .where(DeviceSpecification.hasBrand(filter.brand()))
                        .and(DeviceSpecification.nameContains(filter.name()))
                        .and(DeviceSpecification.hasState(filter.status()))
        );

        return deviceMapper.toDeviceResponses(devices);
    }

    @Transactional(readOnly = true)
    public DeviceResponse findById(UUID id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Device with id %s not found".formatted(id)));
        return deviceMapper.toDeviceResponse(device);
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
    public DeviceResponse updateFull(UUID id, PutDeviceRequest request, String ifMatch) {
        if (request.id() != null && !request.id().equals(id)) {
            throw new IllegalArgumentException("ID in body does not match path parameter");
        }

        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Device with id %s not found".formatted(id)));

        // Optimistic locking using If-Match ETag if provided
        if (ifMatch != null && !ifMatch.isBlank()) {
            String currentEtag = '"' + computeEtagInternal(device) + '"';
            if (!ifMatch.equals(currentEtag)) {
                throw new VersionConflictException("Version conflict detected for device %s".formatted(id));
            }
        }

        // Business rules
        // creationTime is immutable -> our DTO does not carry it; nothing to check here

        // Lock name/brand when IN_USE
        if (device.getState() == com.devices.model.DeviceStatus.IN_USE) {
            if (!device.getName().equals(request.name())) {
                throw new DeviceFieldLockedException("Cannot update 'name' field when device is in IN_USE state",
                        "name", device.getState());
            }
            if (!device.getBrand().equals(request.brand())) {
                throw new DeviceFieldLockedException("Cannot update 'brand' field when device is in IN_USE state",
                        "brand", device.getState());
            }
        }

        // State transitions: for now allow all among AVAILABLE, IN_USE, INACTIVE

        // Apply full replacement of mutable fields
        device.setName(request.name());
        device.setBrand(request.brand());
        device.setState(request.state());

        try {
            Device saved = deviceRepository.saveAndFlush(device);
            entityManager.refresh(saved);
            return deviceMapper.toDeviceResponse(saved);
        } catch (OptimisticLockingFailureException e) {
            throw new VersionConflictException("Version conflict detected for device %s".formatted(id));
        }
    }

    @Transactional
    public DeviceResponse updatePartial(UUID id, Map<String, Object> patch, String ifMatch) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Device with id %s not found".formatted(id)));

        if (ifMatch != null && !ifMatch.isBlank()) {
            String currentEtag = '"' + computeEtagInternal(device) + '"';
            if (!ifMatch.equals(currentEtag)) {
                throw new VersionConflictException("Version conflict detected for device %s".formatted(id));
            }
        }

        if (patch == null || patch.isEmpty()) {
            throw new IllegalArgumentException("PATCH request must contain at least one updatable field");
        }

        // Reject immutable fields
        if (patch.containsKey("creationTime")) {
            throw new ImmutableFieldViolationException("Cannot update 'creationTime' field", "creationTime");
        }
        // Ignore id in patch for now

        boolean changed = false;

        if (patch.containsKey("name")) {
            Object v = patch.get("name");
            if (v == null) {
                throw new IllegalArgumentException("Field 'name' must not be null");
            }
            String value = v.toString();
            if (device.getState() == com.devices.model.DeviceStatus.IN_USE && !device.getName().equals(value)) {
                throw new DeviceFieldLockedException("Cannot update 'name' field when device is in IN_USE state",
                        "name", device.getState());
            }
            if (!device.getName().equals(value)) {
                device.setName(value);
                changed = true;
            }
        }

        if (patch.containsKey("brand")) {
            Object v = patch.get("brand");
            if (v == null) {
                throw new IllegalArgumentException("Field 'brand' must not be null");
            }
            String value = v.toString();
            if (device.getState() == com.devices.model.DeviceStatus.IN_USE && !device.getBrand().equals(value)) {
                throw new DeviceFieldLockedException("Cannot update 'brand' field when device is in IN_USE state",
                        "brand", device.getState());
            }
            if (!device.getBrand().equals(value)) {
                device.setBrand(value);
                changed = true;
            }
        }

        if (patch.containsKey("state")) {
            Object v = patch.get("state");
            if (v == null) {
                throw new IllegalArgumentException("Field 'state' must not be null");
            }
            com.devices.model.DeviceStatus newState;
            try {
                newState = com.devices.model.DeviceStatus.valueOf(v.toString());
            } catch (IllegalArgumentException ex) {
                throw ex; // will be handled as 400 by existing handlers
            }
            if (device.getState() != newState) {
                device.setState(newState);
                changed = true;
            }
        }

        if (!changed) {
            // No-op patch is idempotent; return current representation
            return deviceMapper.toDeviceResponse(device);
        }

        try {
            Device saved = deviceRepository.saveAndFlush(device);
            entityManager.refresh(saved);
            return deviceMapper.toDeviceResponse(saved);
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
