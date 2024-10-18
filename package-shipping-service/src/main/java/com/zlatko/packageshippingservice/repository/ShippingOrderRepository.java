package com.zlatko.packageshippingservice.repository;

import com.zlatko.packageshippingservice.model.entity.ShippingOrderEntity;
import com.zlatko.packageshippingservice.model.enums.OrderStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShippingOrderRepository extends JpaRepository<ShippingOrderEntity, UUID> {
    Optional<ShippingOrderEntity> findByPackageName(String packageName);
    Page<ShippingOrderEntity> findAllByStatus(OrderStatus status, Pageable pageable);
}
