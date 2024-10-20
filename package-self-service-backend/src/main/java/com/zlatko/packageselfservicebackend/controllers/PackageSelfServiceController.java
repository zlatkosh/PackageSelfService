package com.zlatko.packageselfservicebackend.controllers;

import com.zlatko.packageselfservicebackend.model.dtos.Package;
import com.zlatko.packageselfservicebackend.model.dtos.PackageDetails;
import com.zlatko.packageselfservicebackend.model.dtos.enums.PackageStatus;
import com.zlatko.packageselfservicebackend.model.dtos.errors.Error;
import com.zlatko.packageselfservicebackend.services.PackageSelfServiceService;
import com.zlatko.packageselfservicebackend.utils.GlobalConstants;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/package-self-service")
@Slf4j
public class PackageSelfServiceController {
    private final PackageSelfServiceService service;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Package successfully submitted."),
            @ApiResponse(responseCode = "400", description = "Bad request!",
                content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "409", description = "Conflict! Package name already exists. Please provide a unique name.",
                content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "500", description = "An unexpected error occurred",
                    content = @Content(schema = @Schema(implementation = Error.class))),
    })
    @PostMapping
    public ResponseEntity<Void> submitPackage(@Valid @RequestBody Package packageDTO, HttpServletRequest request) {

        UUID submittedPackageId = service.submitPackage(packageDTO);
        log.trace("Submitted package with ID: {}", submittedPackageId);

        URI location = getPackageUri(request.getRequestURL().toString(), submittedPackageId);

        return ResponseEntity.created(location) // Sets the 201 Created status and Location header
                .build(); // Returning an empty body with just the headers
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved package details",
                    content = @Content(schema = @Schema(implementation = PackageDetails.class))),
            @ApiResponse(responseCode = "400", description = "Bad request!",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "500", description = "An unexpected error occurred",
                    content = @Content(schema = @Schema(implementation = Error.class))),
    })
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

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved package details list",
                    content = @Content(schema = @Schema(implementation = PackageDetails.class))),
            @ApiResponse(responseCode = "400", description = "Bad request!",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "500", description = "An unexpected error occurred",
                    content = @Content(schema = @Schema(implementation = Error.class))),
    })
    @GetMapping
    public ResponseEntity<List<PackageDetails>> listPackageDetails(
            @Pattern(regexp = GlobalConstants.UUID_REGEX_PATTERN, message = "Invalid senderId format.")
            @NotBlank(message = "Sender ID is required.") String senderId,
            @RequestParam(required = false) Optional<PackageStatus> status
    ) {
        List<PackageDetails> packageDetails = service.listPackageDetails(senderId, status);
        log.trace("Sender '{}', status '{}', Retrieved package detail list: {}", senderId, status, packageDetails);
        return ResponseEntity.ok(packageDetails);
    }

    /**
     * Construct the URI for the created package. <br>
     *
     * @param requestUrl The URL of the request
     * @param createdPackageId The ID of the created package
     * @return The URI of the created package
     */
    private URI getPackageUri(String requestUrl, UUID createdPackageId) {
        // Construct the absolute URI for the created resource
        URI location = URI.create("%s/%s".formatted(requestUrl, createdPackageId)); // Append the new order ID

        log.trace("Setting Location header to: {}", location);
        return location;
    }
}
