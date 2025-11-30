package com.devices.service;

import com.devices.api.dto.CreateDeviceRequest;
import com.devices.api.dto.CreateDeviceResponse;
import com.devices.api.mapper.DeviceMapper;
import com.devices.api.dto.DeviceResponse;
import com.devices.persistence.Device;
import com.devices.persistence.DeviceRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public List<DeviceResponse> getAll() {
        var devices = deviceRepository.findAll();
        return deviceMapper.toDeviceResponses(devices);
    }
}
