package com.devices;

import com.devices.api.dto.DeviceResponse;
import com.devices.model.DeviceStatus;
import com.devices.persistence.DeviceRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.*;

class DeviceCreationTest extends AbstractIntegrationTest {

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
        assertThat(saved.getState()).isEqualTo(DeviceStatus.AVAILABLE);
        assertThat(saved.getId()).isEqualTo(response.id());
    }

    @Test
    void shouldReturnBadRequestWhenNameOrBrandExceedMaxLength() {
        String tooLongName = "A".repeat(101);
        String payloadWithTooLongName = String.format("""
                {
                  "name": "%s",
                  "brand": "Apple"
                }
                """, tooLongName);

        given()
                .contentType(ContentType.JSON)
                .body(payloadWithTooLongName)
                .when()
                .post("/api/v1/devices")
                .then()
                .statusCode(400)
                .contentType(startsWith("application/problem+json"))
                .body("status", equalTo(400))
                .body("title", equalTo("Validation Error"))
                .body("detail", equalTo("Request validation failed"))
                .body("instance", equalTo("/api/v1/devices"))
                .body("errors.size()", equalTo(1))
                .body("errors[0].field", equalTo("name"))
                .body("errors[0].message", equalTo("Device name must not exceed 100 characters"))
                .body("errors[0].rejectedValue", equalTo(tooLongName));

        assertThat(deviceRepository.count()).isZero();


        String tooLongBrand = "B".repeat(51);
        String payloadWithTooLongBrand = String.format("""
                {
                  "name": "Random Name",
                  "brand": "%s"
                }
                """, tooLongBrand);

        given()
                .contentType(ContentType.JSON)
                .body(payloadWithTooLongBrand)
                .when()
                .post("/api/v1/devices")
                .then()
                .statusCode(400)
                .contentType(startsWith("application/problem+json"))
                .body("status", equalTo(400))
                .body("title", equalTo("Validation Error"))
                .body("detail", equalTo("Request validation failed"))
                .body("instance", equalTo("/api/v1/devices"))
                .body("errors.size()", equalTo(1))
                .body("errors[0].field", equalTo("brand"))
                .body("errors[0].message", equalTo("Device brand must not exceed 50 characters"))
                .body("errors[0].rejectedValue", equalTo(tooLongBrand));

        assertThat(deviceRepository.count()).isZero();
    }

    @Test
    void shouldReturnBadRequestWhenNameOrBrandAreMissing() {
        String invalidPayloadWithNameMissing = """
                {
                  "name": "",
                  "brand": "Apple"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(invalidPayloadWithNameMissing)
                .when()
                .post("/api/v1/devices")
                .then()
                .statusCode(400)
                .contentType(startsWith("application/problem+json"))
                .body("status", equalTo(400))
                .body("title", equalTo("Validation Error"))
                .body("detail", equalTo("Request validation failed"))
                .body("instance", equalTo("/api/v1/devices"))
                .body("errors.size()", equalTo(1))
                .body("errors[0].field", equalTo("name"))
                .body("errors[0].message", equalTo("Device name must not be blank"))
                .body("errors[0].rejectedValue", equalTo(""));

        assertThat(deviceRepository.count()).isZero();

        String invalidPayloadWithBrandMissing = """
                {
                  "name": "Random Name",
                  "brand": ""
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(invalidPayloadWithBrandMissing)
                .when()
                .post("/api/v1/devices")
                .then()
                .statusCode(400)
                .contentType(startsWith("application/problem+json"))
                .body("status", equalTo(400))
                .body("title", equalTo("Validation Error"))
                .body("detail", equalTo("Request validation failed"))
                .body("instance", equalTo("/api/v1/devices"))
                .body("errors.size()", equalTo(1))
                .body("errors[0].field", equalTo("brand"))
                .body("errors[0].message", equalTo("Device brand must not be blank"))
                .body("errors[0].rejectedValue", equalTo(""));

        assertThat(deviceRepository.count()).isZero();
    }

    @Test
    void shouldReturnBadRequestWhenJsonIsMalformed() {

        String malformedPayloadMissingComma = """
                {
                  "name": "Random Name"
                  "brand": "Apple"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(malformedPayloadMissingComma)
                .when()
                .post("/api/v1/devices")
                .then()
                .statusCode(400)
                .contentType(startsWith("application/problem+json"))
                .body("status", equalTo(400))
                .body("title", equalTo("Malformed Request"))
                .body("detail", equalTo("Malformed JSON request"))
                .body("instance", equalTo("/api/v1/devices"));

        assertThat(deviceRepository.count()).isZero();

        String malformedPayloadUnclosedString = """
                {
                  "name": "Random Name,
                  "brand": "Apple"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(malformedPayloadUnclosedString)
                .when()
                .post("/api/v1/devices")
                .then()
                .statusCode(400)
                .contentType(startsWith("application/problem+json"))
                .body("status", equalTo(400))
                .body("title", equalTo("Malformed Request"))
                .body("detail", equalTo("Malformed JSON request"))
                .body("instance", equalTo("/api/v1/devices"));

        assertThat(deviceRepository.count()).isZero();
    }


}
