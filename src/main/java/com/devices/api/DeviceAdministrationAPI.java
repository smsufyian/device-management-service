package com.devices.api;

import com.devices.api.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@Tag(name = "Devices", description = "Device management operations")
public interface DeviceAdministrationAPI {
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
                                    schema = @Schema(implementation = DeviceResponse.class)
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
    DeviceResponse createDevice(@Valid @RequestBody CreateDeviceRequest request);

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

    @Operation(
            summary = "Search devices",
            description = "Get a list of devices filtered by brand, name, or status",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of devices matching criteria",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = DeviceResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid filter parameters",
                            content = @Content(mediaType = "application/problem+json")
                    )
            }
    )
    List<DeviceResponse> getAll(@Parameter(hidden = true) @Valid DeviceFilterRequest filter);

    @Operation(
            summary = "Delete a device",
            description = "Deletes a device by its unique identifier",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Device deleted successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Device not found",
                            content = @Content(mediaType = "application/problem+json")
                    )
            }
    )
    void deleteDevice(@PathVariable UUID id);

    @Operation(
            summary = "Update a device",
            description = "Fully updates a device. All fields must be provided.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Device updated successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = DeviceResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request payload",
                            content = @Content(mediaType = "application/problem+json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Device not found",
                            content = @Content(mediaType = "application/problem+json")
                    )
            }
    )
    DeviceResponse updateDevice(
            @PathVariable UUID id,
            @Valid @RequestBody PutDeviceRequest request
    );

    @Operation(
            summary = "Patch a device",
            description = "Partially updates a device. Only provided fields are updated.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Device updated successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = DeviceResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request payload",
                            content = @Content(mediaType = "application/problem+json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Device not found",
                            content = @Content(mediaType = "application/problem+json")
                    )
            }
    )
    DeviceResponse patchDevice(
            @PathVariable UUID id,
            @Valid @RequestBody PatchDeviceRequest patch
    );
}
