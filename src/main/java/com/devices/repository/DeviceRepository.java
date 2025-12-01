package com.devices.repository;

import com.devices.domain.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID>, JpaSpecificationExecutor<Device> { }