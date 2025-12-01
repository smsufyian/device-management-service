package com.devices;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class DeviceFindingTest extends AbstractIntegrationTest {

    @Test
    void shouldGetEmptyListWhenNoDevicesExist() {
        given()
                .noContentType()
                .when()
                .get("/api/v1/devices")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(0));
    }

    @Test
    void shouldGetEmptyListWhenThereAreNoDevicesForStatusIsInUse() {
        given()
                .noContentType()
                .when()
                .get("/api/v1/devices?status={status}", "IN_USE")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(0));
    }

    @Test
    void shouldGetAllDevicesWhenDevicesExist() {
        String device1 = """
                {
                  "name": "Thermostat",
                  "brand": "Nest"
                }
                """;

        String device2 = """
                {
                  "name": "iPhone",
                  "brand": "Apple"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(device1)
                .when()
                .post("/api/v1/devices")
                .then()
                .statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .body(device2)
                .when()
                .post("/api/v1/devices")
                .then()
                .statusCode(201);

        given()
                .noContentType()
                .when()
                .get("/api/v1/devices")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(2))
                .body("name", containsInAnyOrder("Thermostat", "iPhone"))
                .body("brand", containsInAnyOrder("Nest", "Apple"))
                .body("state", everyItem(equalTo("AVAILABLE")))
                .body("id", everyItem(notNullValue()))
                .body("creationTime", everyItem(notNullValue()));
    }


    @Test
    void shouldGetDeviceByIdWhenItExists() {
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

    @Test
    void shouldGetDevicesByBrand() {
        String nestDevice1 = """
                {
                  "name": "Thermostat",
                  "brand": "Nest"
                }
                """;

        String nestDevice2 = """
                {
                  "name": "Camera",
                  "brand": "Nest"
                }
                """;

        String appleDevice = """
                {
                  "name": "iPhone",
                  "brand": "Apple"
                }
                """;

        given().contentType(ContentType.JSON).body(nestDevice1).when().post("/api/v1/devices").then().statusCode(201);
        given().contentType(ContentType.JSON).body(nestDevice2).when().post("/api/v1/devices").then().statusCode(201);
        given().contentType(ContentType.JSON).body(appleDevice).when().post("/api/v1/devices").then().statusCode(201);

        given()
                .noContentType()
                .when()
                .get("/api/v1/devices?brand={brand}", "Nest")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(2))
                .body("brand", everyItem(equalTo("Nest")))
                .body("name", containsInAnyOrder("Thermostat", "Camera"))
                .body("id", everyItem(notNullValue()))
                .body("creationTime", everyItem(notNullValue()));
    }

    @Test
    void shouldGetDevicesByStatusCreated() {
        String device1 = """
                {
                  "name": "Thermostat",
                  "brand": "Nest"
                }
                """;

        String device2 = """
                {
                  "name": "iPhone",
                  "brand": "Apple"
                }
                """;

        given().contentType(ContentType.JSON).body(device1).when().post("/api/v1/devices").then().statusCode(201);
        given().contentType(ContentType.JSON).body(device2).when().post("/api/v1/devices").then().statusCode(201);

        given()
                .noContentType()
                .when()
            .get("/api/v1/devices?status={status}", "AVAILABLE")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(2))
                .body("state", everyItem(equalTo("AVAILABLE")))
                .body("id", everyItem(notNullValue()))
                .body("creationTime", everyItem(notNullValue()));
    }
}
