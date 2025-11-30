package com.devices.api.mapper;

import com.devices.api.dto.DeviceCreateRequest;
import com.devices.api.dto.DeviceResponse;
import com.devices.persistence.Device;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DeviceMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Device toEntity(DeviceCreateRequest request);

    @Mapping(source = "createdAt", target = "creationTime")
    DeviceResponse toResponse(Device device);
}
