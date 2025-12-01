package com.devices.persistence;

import com.devices.model.DeviceStatus;
import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "devices")
public class Device implements Persistable<UUID> {

    @Id
    @Column(name = "device_id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "brand", nullable = false, length = 50)
    private String brand;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    private DeviceStatus state;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false, columnDefinition = "timestamptz")
    private OffsetDateTime createdAt;

    // Protected no-arg constructor required by JPA (Hibernate)
    protected Device() {
        // for JPA
    }

    // Constructor used by application code (createdAt is DB-generated)
    public Device(UUID id, String name, String brand, DeviceStatus state) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.state = state;
        this.createdAt = null;
    }

    // Getters
    @Override
    public UUID getId() { return id; }

    public String getName() { return name; }

    public String getBrand() { return brand; }

    public DeviceStatus getState() { return state; }

    public OffsetDateTime getCreatedAt() { return createdAt; }

    @Override
    @Transient
    public boolean isNew() {
        // Treat entity as new if it has not been assigned a creation timestamp yet
        return this.createdAt == null;
    }

    // equals and hashCode based on id
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Device device = (Device) o;
        return id != null && id.equals(device.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
