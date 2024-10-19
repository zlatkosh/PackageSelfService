package com.zlatko.packageselfservicebackend.services;

import com.zlatko.packageselfservicebackend.clients.PackageShippingServiceClient;
import com.zlatko.packageselfservicebackend.clients.dtos.ShippingOrder;
import com.zlatko.packageselfservicebackend.clients.dtos.enums.PackageSize;
import com.zlatko.packageselfservicebackend.model.dtos.Package;
import com.zlatko.packageselfservicebackend.model.entities.EmployeeEntity;
import com.zlatko.packageselfservicebackend.model.entities.PackageEntity;
import com.zlatko.packageselfservicebackend.model.exceptions.RecipientNotFoundException;
import com.zlatko.packageselfservicebackend.model.exceptions.SenderNotFoundException;
import com.zlatko.packageselfservicebackend.repositories.EmployeeRepository;
import com.zlatko.packageselfservicebackend.repositories.PackageRepository;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PackageSelfServiceService {

    private final PackageShippingServiceClient packageShippingServiceClient;
    private final EmployeeRepository employeeRepository;
    private final PackageRepository packageRepository;

    @Transactional
    public UUID submitPackage(@Valid Package packageDTO) {
        EmployeeEntity sender = getSender(packageDTO.senderId());
        EmployeeEntity recipient = getRecipient(packageDTO.recipientId());
        ShippingOrder shippingOrder = new ShippingOrder(
                packageDTO.packageName(),
                recipient.getPostalCode(),
                recipient.getStreet(),
                recipient.getName(),
                mapPackageSize(packageDTO.weightInGrams()).toString()
        );

        URI locationURI = packageShippingServiceClient.createShippingOrder(shippingOrder);

        return persistPackage(packageDTO, sender, recipient, locationURI);
    }

    private UUID persistPackage(@Valid Package packageDTO, EmployeeEntity sender, EmployeeEntity recipient, URI locationURI) {

        PackageEntity packageEntity = PackageEntity.builder()
                .id(UUID.randomUUID())
                .packageName(packageDTO.packageName())
                .weightInGrams(packageDTO.weightInGrams())
                .sender(sender)
                .receiver(recipient)
                .downstreamOrderUrl(locationURI.toString())
                .dateOfRegistration(Instant.now())
                .build();
        log.trace("Persisting package: {}", packageEntity);
        return packageRepository.save(packageEntity).getId();
    }

    /**
     * Maps package size in grams to package size.
     *
     * @param sizeInGrams package size in grams
     * @return package size
     */
    public PackageSize mapPackageSize(Integer sizeInGrams) {
        return switch (sizeInGrams) {
            case Integer size when size < 200 -> PackageSize.S;     // Less than 0.2kg
            case Integer size when size < 1000 -> PackageSize.M;    // 0.2kg to <1kg
            case Integer size when size < 10000 -> PackageSize.L;   // 1kg to <10kg
            default -> PackageSize.XL;                              // 10kg or more
        };
    }

    /**
     * Validates that the sender ID exists and returns the sender entity.
     *
     * @param senderId sender ID
     * @return sender entity
     */
    private EmployeeEntity getSender(String senderId) {
        var employeeEntityOptional = employeeRepository.findById(UUID.fromString(senderId));
        if (employeeEntityOptional.isEmpty()) {
            throw new SenderNotFoundException(senderId);
        }
        log.trace("Sender found: {}", employeeEntityOptional.get());
        return employeeEntityOptional.get();
    }

    /**
     * Validates that the recipient ID exists and returns the recipient entity.
     *
     * @param recipientId recipient ID
     * @return recipient entity
     */
    private EmployeeEntity getRecipient(String recipientId) {
        var employeeEntityOptional = employeeRepository.findById(UUID.fromString(recipientId));
        if (employeeEntityOptional.isEmpty()) {
            throw new RecipientNotFoundException(recipientId);
        }
        log.trace("Recipient found: {}", employeeEntityOptional.get());
        return employeeEntityOptional.get();
    }
}
