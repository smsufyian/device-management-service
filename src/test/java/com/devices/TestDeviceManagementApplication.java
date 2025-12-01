package com.devices;

import org.springframework.boot.SpringApplication;

public class TestDeviceManagementApplication {

    public static void main(String[] args) {
        SpringApplication.from(DeviceManagementApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
