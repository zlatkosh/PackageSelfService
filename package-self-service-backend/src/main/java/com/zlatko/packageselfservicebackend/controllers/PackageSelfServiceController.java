package com.zlatko.packageselfservicebackend.controllers;

import com.zlatko.packageselfservicebackend.model.dtos.Package;
import com.zlatko.packageselfservicebackend.model.dtos.PackageDetails;
import com.zlatko.packageselfservicebackend.services.PackageSelfServiceService;
import com.zlatko.packageselfservicebackend.utils.GlobalConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/package-self-service")
@Slf4j
public class PackageSelfServiceController {
    private final PackageSelfServiceService service;

    @PostMapping
    public ResponseEntity<Void> submitPackage(@Valid @RequestBody Package packageDTO, HttpServletRequest request) {

        UUID submittedPackageId = service.submitPackage(packageDTO);
        log.trace("Submitted package with ID: {}", submittedPackageId);

        URI location = getOrderUri(request.getRequestURL().toString(), submittedPackageId);

        return ResponseEntity.created(location) // Sets the 201 Created status and Location header
                .build(); // Returning an empty body with just the headers
    }

    @GetMapping("/{packageId}")
    public ResponseEntity<PackageDetails> getPackageDetails(
            @Pattern(regexp = GlobalConstants.UUID_REGEX_PATTERN, message = "Invalid senderId format.")
            @NotBlank(message = "Sender ID is required.") String senderId,
            @Pattern(regexp = GlobalConstants.UUID_REGEX_PATTERN, message = "Invalid packageId format.")
            @PathVariable String packageId
    ) {
        PackageDetails packageDetails = service.getPackageDetails(packageId, senderId);
        log.trace("Sender '{}', Successfully retrieved package details: {}", senderId, packageDetails);
        return ResponseEntity.ok(packageDetails);
    }

    private URI getOrderUri(String requestUrl, UUID createdOrderId) {
        // Construct the absolute URI for the created resource
        URI location = URI.create("%s/%s".formatted(requestUrl, createdOrderId)); // Append the new order ID

        log.trace("Setting Location header to: {}", location);
        return location;
    }
}