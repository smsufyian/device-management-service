package com.devices;

import com.devices.api.dto.DeviceResponse;
import com.devices.model.DeviceState;
import com.devices.persistence.DeviceRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.*;

class CreateDeviceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private DeviceRepository deviceRepository;

    @Test
    void shouldCreateDeviceSuccessfullyWhenNameAndBrandAreValid() {
        String payload = """
                {
                    "name": "Thermostat",
                    "brand": "Nest"
                }
                """;

        DeviceResponse response = given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/v1/devices")
                .then()
                .statusCode(201)
                .header("Location", matchesPattern(".*/api/v1/devices/[0-9a-f-]{36}"))
                .contentType(ContentType.JSON)
                .body("id", notNullValue())
                .body("name", equalTo("Thermostat"))
                .body("brand", equalTo("Nest"))
                .body("state", equalTo("AVAILABLE"))
                .body("creationTime", notNullValue())
                .extract()
                .as(DeviceResponse.class);


        var saved = deviceRepository.findById(response.id()).orElseThrow();
        assertThat(saved.getName()).isEqualTo("Thermostat");
        assertThat(saved.getBrand()).isEqualTo("Nest");
        assertThat(saved.getState()).isEqualTo(DeviceState.AVAILABLE);
        assertThat(saved.getId()).isEqualTo(response.id());
    }
}
