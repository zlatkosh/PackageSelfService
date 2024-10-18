package com.zlatko.packageshippingservice.model.entity;

import com.zlatko.packageshippingservice.model.enums.OrderStatus;
import com.zlatko.packageshippingservice.model.enums.PackageSize;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "shipping_orders")
@Getter
@Setter
@NoArgsConstructor // Default constructor required by JPA
@AllArgsConstructor // Constructor for all fields
@Builder // Builder pattern
public class ShippingOrderEntity {
    @Id
    @GeneratedValue
    @UuidGenerator // Use Hibernate's built-in UUID generator
    private UUID id;

    @Column(nullable = false)
    private String packageName;

    @Column(nullable = false)
    private String postalCode;

    @Column(nullable = false)
    private String streetName;

    @Column(nullable = false)
    private String receiverName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PackageSize packageSize;

    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private LocalDate expectedDeliveryDate;

    private LocalDateTime actualDeliveryDateTime;
}