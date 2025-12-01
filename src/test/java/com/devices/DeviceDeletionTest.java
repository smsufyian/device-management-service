package com.devices;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

class DeviceDeletionTest extends AbstractIntegrationTest {

    @Test
    void shouldDeleteDeviceWhenStateIsAvailable() {
        String payload = """
                {
                  "name": "Thermostat",
                  "brand": "Nest"
                }
                """;

        String deviceId =
                given()
                        .contentType(ContentType.JSON)
                        .body(payload)
                        .when()
                        .post("/api/v1/devices")
                        .then()
                        .statusCode(201)
                        .extract()
                        .jsonPath()
                        .getString("id");

        given()
                .noContentType()
                .when()
                .delete("/api/v1/devices/{id}", deviceId)
                .then()
                .statusCode(204);

        given()
                .noContentType()
                .when()
                .get("/api/v1/devices/{id}", deviceId)
                .then()
                .statusCode(404)
                .contentType("application/problem+json");
    }

    @Test
    void shouldDeleteDeviceWhenStateIsInactive() {
        String payload = """
                {
                  "name": "Camera",
                  "brand": "Nest"
                }
                """;

        String deviceId =
                given()
                        .contentType(ContentType.JSON)
                        .body(payload)
                        .when()
                        .post("/api/v1/devices")
                        .then()
                        .statusCode(201)
                        .extract()
                        .jsonPath()
                        .getString("id");

        // Set state to INACTIVE directly in DB
        jdbcTemplate.update("UPDATE devices SET state = 'INACTIVE' WHERE device_id = ?::uuid", deviceId);

        given()
                .noContentType()
                .when()
                .delete("/api/v1/devices/{id}", deviceId)
                .then()
                .statusCode(204);
    }

    @Test
    void shouldReturnConflictWhenDeviceIsInUse() {
        String payload = """
                {
                  "name": "Door Lock",
                  "brand": "August"
                }
                """;

        String deviceId =
                given()
                        .contentType(ContentType.JSON)
                        .body(payload)
                        .when()
                        .post("/api/v1/devices")
                        .then()
                        .statusCode(201)
                        .extract()
                        .jsonPath()
                        .getString("id");

        jdbcTemplate.update("UPDATE devices SET state = 'IN_USE' WHERE device_id = ?::uuid", deviceId);

        given()
                .noContentType()
                .when()
                .delete("/api/v1/devices/{id}", deviceId)
                .then()
                .statusCode(409)
                .contentType("application/problem+json")
                .body("title", equalTo("Device in use"))
                .body("status", equalTo(409))
                .body("detail", containsString("cannot be deleted"))
                .body("type", equalTo("https://api.example.com/errors/device-in-use"));
    }

    @Test
    void shouldReturnNotFoundWhenDeviceDoesNotExist() {
        String nonExistingId = java.util.UUID.randomUUID().toString();

        given()
                .noContentType()
                .when()
                .delete("/api/v1/devices/{id}", nonExistingId)
                .then()
                .statusCode(404)
                .contentType("application/problem+json")
                .body("title", equalTo("Device Not Found"))
                .body("status", equalTo(404))
                .body("type", equalTo("https://api.example.com/errors/device-not-found"));
    }
}
