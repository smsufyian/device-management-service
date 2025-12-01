package com.devices.api;

import com.devices.api.dto.CreateDeviceRequest;
import com.devices.api.dto.DeviceResponse;
import com.devices.api.dto.PatchDeviceRequest;
import com.devices.domain.DeviceStatus;
import com.devices.domain.Device;
import org.mapstruct.*;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = {UUID.class, DeviceStatus.class})
public interface DeviceAdministrationMapper {

    default Device toEntity(CreateDeviceRequest request) {
        return new Device(
                UUID.randomUUID(),
                request.name(),
                request.brand(),
                DeviceStatus.AVAILABLE
        );
    }

    @Mapping(source = "createdAt", target = "creationTime")
    DeviceResponse toResponse(Device device);

    List<DeviceResponse> toResponseList(List<Device> devices);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDeviceFromPatch(PatchDeviceRequest patch, @MappingTarget Device device);
}
