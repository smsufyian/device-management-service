package com.devices;

import static org.assertj.core.api.Assertions.assertThat;

import com.devices.api.dto.CreateDeviceRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DeviceValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setup() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void validRequestPassesValidation() {
        CreateDeviceRequest req = new CreateDeviceRequest("Watch", "Acme");

        assertThat(validator.validate(req)).isEmpty();
    }
}
