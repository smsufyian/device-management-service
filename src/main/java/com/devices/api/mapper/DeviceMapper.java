package com.devices.api.mapper;

import com.devices.api.dto.CreateDeviceRequest;
import com.devices.api.dto.CreateDeviceResponse;
import com.devices.model.DeviceState;
import com.devices.persistence.Device;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DeviceMapper {

    // Manual construction to avoid issues mapping into JPA entities and to set defaults
    default Device toEntity(CreateDeviceRequest request) {
        return new Device(
                java.util.UUID.randomUUID(),
                request.name(),
                request.brand(),
                DeviceState.AVAILABLE
        );
    }

    @Mapping(source = "createdAt", target = "creationTime")
    CreateDeviceResponse toResponse(Device device);
}
