package com.devices.persistence;

import com.devices.model.DeviceState;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "device_id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "brand", nullable = false, length = 50)
    private String brand;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    private DeviceState state;

    @Column(name = "created_at", nullable = false, columnDefinition = "timestamptz")
    @CreationTimestamp
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public DeviceState getState() { return state; }
    public void setState(DeviceState state) { this.state = state; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
