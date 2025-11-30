package com.devices.service;

import com.devices.api.dto.CreateDeviceRequest;
import com.devices.api.dto.CreateDeviceResponse;
import com.devices.api.mapper.DeviceMapper;
import com.devices.persistence.Device;
import com.devices.persistence.DeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;

    public DeviceService(DeviceRepository deviceRepository, DeviceMapper deviceMapper) {
        this.deviceRepository = deviceRepository;
        this.deviceMapper = deviceMapper;
    }

    @Transactional
    public CreateDeviceResponse create(CreateDeviceRequest request) {
        Device device = deviceMapper.toEntity(request);
        Device saved = deviceRepository.save(device);
        deviceRepository.flush();
        return deviceMapper.toResponse(saved);
    }
}
