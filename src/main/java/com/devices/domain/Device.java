package com.devices.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.jspecify.annotations.NonNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "devices")
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Device implements Persistable<@NonNull UUID> {

    @Id
    @Column(name = "device_id", nullable = false, updatable = false, columnDefinition = "uuid")
    @EqualsAndHashCode.Include
    private UUID id;

    @Setter
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Setter
    @Column(name = "brand", nullable = false, length = 50)
    private String brand;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    private DeviceStatus state;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    private Instant createdAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public Device(UUID id, String name, String brand, DeviceStatus state) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.state = state;
        this.createdAt = null;
    }

    public void updateDetails(String newName, String newBrand, DeviceStatus newState) {
        if (this.state == DeviceStatus.IN_USE) {
            if (!this.name.equals(newName)) {
                throw new DeviceFieldLockedException("name", this.state);
            }
            if (!this.brand.equals(newBrand)) {
                throw new DeviceFieldLockedException("brand", this.state);
            }
        }
        this.name = newName;
        this.brand = newBrand;
        this.state = newState;
    }

    public void validatePartialUpdate(String nameCandidate, String brandCandidate) {
        if (this.state == DeviceStatus.IN_USE) {
            if (nameCandidate != null && !this.name.equals(nameCandidate)) {
                throw new DeviceFieldLockedException(
                        "name",
                        this.state
                );
            }
            if (brandCandidate != null && !this.brand.equals(brandCandidate)) {
                throw new DeviceFieldLockedException(
                        "brand",
                        this.state
                );
            }
        }
    }

    @Override
    @Transient
    public boolean isNew() {
        return this.createdAt == null;
    }
}
