package com.zlatko.packageselfservicebackend.services;

import com.zlatko.packageselfservicebackend.clients.PackageShippingServiceClient;
import com.zlatko.packageselfservicebackend.clients.dtos.ShippingOrder;
import com.zlatko.packageselfservicebackend.clients.dtos.ShippingOrderDetails;
import com.zlatko.packageselfservicebackend.clients.dtos.enums.PackageSize;
import com.zlatko.packageselfservicebackend.model.dtos.Package;
import com.zlatko.packageselfservicebackend.model.dtos.PackageDetails;
import com.zlatko.packageselfservicebackend.model.dtos.RecipientDetails;
import com.zlatko.packageselfservicebackend.model.dtos.enums.PackageStatus;
import com.zlatko.packageselfservicebackend.model.entities.EmployeeEntity;
import com.zlatko.packageselfservicebackend.model.entities.PackageEntity;
import com.zlatko.packageselfservicebackend.model.exceptions.PackageNotFoundException;
import com.zlatko.packageselfservicebackend.model.exceptions.RecipientNotFoundException;
import com.zlatko.packageselfservicebackend.model.exceptions.SenderNotFoundException;
import com.zlatko.packageselfservicebackend.repositories.EmployeeRepository;
import com.zlatko.packageselfservicebackend.repositories.PackageRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
                .dateOfRegistration(LocalDateTime.now())
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
        var employeeEntity = employeeRepository.findById(UUID.fromString(senderId))
                .orElseThrow(() -> new SenderNotFoundException(senderId));

        log.trace("Sender found: {}", employeeEntity);
        return employeeEntity;
    }

    /**
     * Validates that the recipient ID exists and returns the recipient entity.
     *
     * @param recipientId recipient ID
     * @return recipient entity
     */
    private EmployeeEntity getRecipient(String recipientId) {
        var employeeEntity = employeeRepository.findById(UUID.fromString(recipientId))
                        .orElseThrow(() -> new RecipientNotFoundException(recipientId));
        log.trace("Recipient found: {}", employeeEntity);
        return employeeEntity;
    }

    public PackageDetails getPackageDetails(String packageId, String senderId) {
        PackageEntity packageEntity = packageRepository.findByIdAndSender(UUID.fromString(packageId), getSender(senderId))
                .orElseThrow(() -> new PackageNotFoundException(packageId, senderId));
        // Extract the order ID from the URL
        String orderId = StringUtils.substringAfterLast(packageEntity.getDownstreamOrderUrl(), "/");
        ShippingOrderDetails clientOrderDetails = packageShippingServiceClient.getOrderDetails(orderId);
        return new PackageDetails(
                packageEntity.getId().toString(),
                packageEntity.getPackageName(),
                packageEntity.getDateOfRegistration(),
                PackageStatus.valueOf(clientOrderDetails.orderStatus().toString()), // get/map the status from the downstream service
                clientOrderDetails.expectedDeliveryDate(), // get the estimated delivery date from the downstream service
                clientOrderDetails.actualDeliveryDateTime(), // get the actual delivery date from the downstream service
                new RecipientDetails(
                        packageEntity.getReceiver().getId().toString(),
                        packageEntity.getReceiver().getName(),
                        constructRecipientAddress(packageEntity.getReceiver())
                )

        );
    }

    /**
     * Constructs the recipient address from the input entity by concatenating: <br>
     *  - street name <br>
     *  - postal code <br>
     *  - city <br>
     *  - state <br>
     *  - country <br>
     *
     * @param receiver recipient entity
     * @return formatted recipient address
     */
    private String constructRecipientAddress(@NotNull EmployeeEntity receiver) {
        return "%s, %s, %s  %s - %s".formatted(
                        receiver.getStreet(),
                        receiver.getPostalCode(),
                        receiver.getCity(),
                        receiver.getState(),
                        receiver.getCountry()
        );
    }
}
