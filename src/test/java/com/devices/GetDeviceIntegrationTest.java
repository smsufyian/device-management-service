package com.devices;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class GetDeviceIntegrationTest extends AbstractIntegrationTest {

    @Test
    void shouldReturnDeviceByIdWhenItExists() {
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
                .get("/api/v1/devices/{id}", deviceId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(deviceId))
                .body("name", equalTo("Thermostat"))
                .body("brand", equalTo("Nest"))
                .body("state", equalTo("AVAILABLE"))
                .body("creationTime", notNullValue());
    }

    @Test
    void shouldReturnNotFoundWhenDeviceDoesNotExist() {
        String randomDeviceId = UUID.randomUUID().toString();

        given()
                .noContentType()
                .when()
                .get("/api/v1/devices/{id}", randomDeviceId)
                .then()
                .statusCode(404)
                .contentType("application/problem+json")
                .body("title", equalTo("Device Not Found"))
                .body("status", equalTo(404))
                .body("detail", containsString("Device with id"))
                .body("type", equalTo("https://api.example.com/errors/device-not-found"));
    }

    @Test
    void shouldReturnBadRequestWhenIdIsMalformed() {
        String malformedId = "not-a-uuid";

        given()
                .noContentType()
                .when()
                .get("/api/v1/devices/{id}", malformedId)
                .then()
                .statusCode(400)
                .contentType("application/problem+json")
                .body("title", equalTo("Invalid Parameter"))
                .body("status", equalTo(400))
                .body("detail", containsString("parameter 'id'"))
                .body("type", equalTo("https://api.example.com/errors/invalid-parameter"))
                .body("parameter", equalTo("id"))
                .body("expectedType", equalTo("UUID"));
    }
}
