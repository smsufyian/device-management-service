package com.devices.api.mapper;

import com.devices.api.dto.CreateDeviceRequest;
import com.devices.api.dto.CreateDeviceResponse;
import com.devices.persistence.Device;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DeviceMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "state", constant = "AVAILABLE")
    @Mapping(target = "createdAt", ignore = true)
    Device toEntity(CreateDeviceRequest request);

    @Mapping(source = "createdAt", target = "creationTime")
    CreateDeviceResponse toResponse(Device device);
}
