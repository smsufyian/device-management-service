package com.devices.service;

import com.devices.api.dto.CreateDeviceRequest;
import com.devices.api.dto.CreateDeviceResponse;
import com.devices.api.dto.DeviceFilterRequest;
import com.devices.api.dto.DeviceResponse;
import com.devices.api.mapper.DeviceMapper;
import com.devices.persistence.Device;
import com.devices.persistence.DeviceRepository;
import com.devices.persistence.DeviceSpecification;
import com.devices.service.exception.DeviceNotFoundException;
import com.devices.service.exception.DeviceInUseException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
}
