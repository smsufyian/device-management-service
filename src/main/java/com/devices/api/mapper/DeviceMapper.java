package com.devices.api.mapper;

import com.devices.api.dto.CreateDeviceRequest;
import com.devices.api.dto.CreateDeviceResponse;
import com.devices.api.dto.DeviceResponse;
import com.devices.model.DeviceStatus;
import com.devices.persistence.Device;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import com.devices.api.dto.PatchDeviceRequest;

@Mapper(componentModel = "spring")
public interface DeviceMapper {

    default Device toEntity(CreateDeviceRequest request) {
        return new Device(
                java.util.UUID.randomUUID(),
                request.name(),
                request.brand(),
                DeviceStatus.AVAILABLE
        );
    }

    @Mapping(source = "createdAt", target = "creationTime")
    CreateDeviceResponse toResponse(Device device);

    @Mapping(source = "createdAt", target = "creationTime")
    DeviceResponse toDeviceResponse(Device device);

    java.util.List<DeviceResponse> toDeviceResponses(java.util.List<Device> devices);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDeviceFromPatch(PatchDeviceRequest patch, @MappingTarget Device device);
}
