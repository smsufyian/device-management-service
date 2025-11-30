package com.devices;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class GetDevicesIntegrationTest extends AbstractIntegrationTest {

    @Test
    void testMethod() {
        given().noContentType().when().get("/api/v1/devices").then().statusCode(200);
    }

    @Test
    void shouldReturnEmptyListWhenNoDevicesExist() {
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
    void shouldReturnAllDevicesWhenTheyExist() {
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
}