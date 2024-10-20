package com.zlatko.packageselfservicebackend.model.dtos;

import com.zlatko.packageselfservicebackend.model.dtos.enums.PackageStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PackageDetails(
        String packageId,
        String packageName,
        LocalDateTime dateOfRegistration,
        PackageStatus status,
        LocalDate expectedDeliveryDate,
        LocalDateTime actualDeliveryDateTime,
        RecipientDetails recipient
) {
}
