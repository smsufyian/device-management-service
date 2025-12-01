package com.devices.repository;

import com.devices.domain.Device;
import com.devices.domain.DeviceStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.domain.Specification;

public class DeviceSpecification {
    private DeviceSpecification() { }

    public static Specification<@NonNull Device> hasBrand(String brand) {
        return (root, query, criteriaBuilder) ->
                brand == null ? criteriaBuilder.conjunction()
                        : criteriaBuilder.equal(root.get("brand"), brand);
    }

    public static Specification<@NonNull Device> nameContains(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%"
            );
        };
    }

    public static Specification<@NonNull Device> hasState(DeviceStatus state) {
        return (root, query, criteriaBuilder) ->
                state == null ? criteriaBuilder.conjunction()
                        : criteriaBuilder.equal(root.get("state"), state);
    }
}
