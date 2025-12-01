package com.devices.api;

import com.devices.api.dto.CreateDeviceRequest;
import com.devices.api.dto.CreateDeviceResponse;
import com.devices.api.dto.DeviceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@Tag(name = "Devices", description = "Device management operations")
public interface DeviceAPIDocs {
    @Operation(
            summary = "Create a new device",
            description = "Creates a device with the provided name and brand",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Device created successfully",
                            headers = @Header(
                                    name = "Location",
                                    description = "URI of the created device",
                                    schema = @Schema(type = "string")
                            ),
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CreateDeviceResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request payload",
                            content = @Content(mediaType = "application/problem+json")
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Device already exists",
                            content = @Content(mediaType = "application/problem+json")
                    )
            }
    )
    CreateDeviceResponse createDevice(@Valid @RequestBody CreateDeviceRequest request);

    @Operation(
            summary = "Get device by ID",
            description = "Returns a device for the given identifier",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Device details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = DeviceResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid ID format",
                            content = @Content(mediaType = "application/problem+json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Device not found",
                            content = @Content(mediaType = "application/problem+json")
                    )
            }
    )
    DeviceResponse getByDeviceId(@PathVariable UUID id);

}
