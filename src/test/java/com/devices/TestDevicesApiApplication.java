package com.devices;

import org.springframework.boot.SpringApplication;

public class TestDevicesApiApplication {

    public static void main(String[] args) {
        SpringApplication.from(DevicesApiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
