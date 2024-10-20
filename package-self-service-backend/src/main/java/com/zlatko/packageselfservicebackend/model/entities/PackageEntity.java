package com.zlatko.packageselfservicebackend.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "packages")
@NoArgsConstructor // Default constructor required by JPA
@AllArgsConstructor // Constructor for all fields
@Builder // Builder pattern
public class PackageEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 255)
    @NotNull
    @Column(name = "package_name", nullable = false)
    private String packageName;

    @NotNull
    @Column(name = "weight_in_grams", nullable = false)
    private Integer weightInGrams;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private EmployeeEntity sender;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "receiver_id", nullable = false)
    private EmployeeEntity receiver;

    @Column(name = "downstream_order_url")
    @NotNull
    private String downstreamOrderUrl;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "date_of_registration")
    @NotNull
    private LocalDateTime dateOfRegistration;

}