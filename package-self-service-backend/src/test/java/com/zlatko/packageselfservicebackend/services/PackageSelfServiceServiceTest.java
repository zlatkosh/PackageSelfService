package com.zlatko.packageselfservicebackend.services;

import com.zlatko.packageselfservicebackend.clients.PackageShippingServiceClient;
import com.zlatko.packageselfservicebackend.clients.dtos.ShippingOrder;
import com.zlatko.packageselfservicebackend.clients.dtos.ShippingOrderDetails;
import com.zlatko.packageselfservicebackend.clients.dtos.enums.OrderStatus;
import com.zlatko.packageselfservicebackend.clients.dtos.enums.PackageSize;
import com.zlatko.packageselfservicebackend.model.dtos.Package;
import com.zlatko.packageselfservicebackend.model.dtos.PackageDetails;
import com.zlatko.packageselfservicebackend.model.entities.EmployeeEntity;
import com.zlatko.packageselfservicebackend.model.entities.PackageEntity;
import com.zlatko.packageselfservicebackend.model.exceptions.PackageNotFoundException;
import com.zlatko.packageselfservicebackend.model.exceptions.RecipientNotFoundException;
import com.zlatko.packageselfservicebackend.model.exceptions.SenderNotFoundException;
import com.zlatko.packageselfservicebackend.repositories.EmployeeRepository;
import com.zlatko.packageselfservicebackend.repositories.PackageRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PackageSelfServiceServiceTest {

    @InjectMocks
    private PackageSelfServiceService packageSelfServiceService;

    @Mock
    private PackageShippingServiceClient packageShippingServiceClient;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PackageRepository packageRepository;

    private EmployeeEntity sender;
    private EmployeeEntity recipient;
    private Package packageDTO;
    private PackageEntity packageEntity;
    private UUID packageId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mocking sample data
        sender = new EmployeeEntity();
        sender.setId(UUID.randomUUID());
        sender.setName("Sender Name");
        sender.setStreet("Sender Street");
        sender.setPostalCode("12345");
        sender.setCity("Sender City");
        sender.setState("Sender State");
        sender.setCountry("Sender Country");

        recipient = new EmployeeEntity();
        recipient.setId(UUID.randomUUID());
        recipient.setName("Recipient Name");
        recipient.setStreet("Recipient Street");
        recipient.setPostalCode("54321");
        recipient.setCity("Recipient City");
        recipient.setState("Recipient State");
        recipient.setCountry("Recipient Country");

        packageId = UUID.randomUUID();

        // Update the Package record with the new structure
        packageDTO = new Package(
                "Sample Package", // package name
                500, // weight in grams
                recipient.getId().toString(), // recipient ID
                sender.getId().toString() // sender ID
        );

        packageEntity = PackageEntity.builder()
                .id(packageId)
                .packageName("Sample Package")
                .weightInGrams(500)
                .sender(sender)
                .receiver(recipient)
                .downstreamOrderUrl("http://example.com/order/123")
                .dateOfRegistration(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("submitPackage method")
    class SubmitPackageTests {

        @Test
        void should_submit_package_successfully() {
            // Given
            when(employeeRepository.findById(sender.getId())).thenReturn(Optional.of(sender));
            when(employeeRepository.findById(recipient.getId())).thenReturn(Optional.of(recipient));
            when(packageShippingServiceClient.createShippingOrder(any(ShippingOrder.class)))
                    .thenReturn(URI.create("http://example.com/order/123"));
            when(packageRepository.save(any(PackageEntity.class))).thenReturn(packageEntity);

            // When
            UUID submittedPackageId = packageSelfServiceService.submitPackage(packageDTO);

            // Then
            assertNotNull(submittedPackageId);
            assertEquals(packageId, submittedPackageId);
            verify(employeeRepository, times(2)).findById(any(UUID.class)); // Verify sender and recipient lookup
            verify(packageShippingServiceClient, times(1)).createShippingOrder(any(ShippingOrder.class)); // Verify shipping order creation
            verify(packageRepository, times(1)).save(any(PackageEntity.class)); // Verify package persistence
        }

        @Test
        void should_throw_sender_not_found_exception() {
            // Given
            when(employeeRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

            // When
            Executable executable = () -> packageSelfServiceService.submitPackage(packageDTO);

            // Then
            assertThrows(SenderNotFoundException.class, executable);
        }

        @Test
        void should_throw_recipient_not_found_exception() {
            // Given
            when(employeeRepository.findById(sender.getId())).thenReturn(Optional.of(sender));
            when(employeeRepository.findById(recipient.getId())).thenReturn(Optional.empty());

            // When
            Executable executable = () -> packageSelfServiceService.submitPackage(packageDTO);

            // Then
            assertThrows(RecipientNotFoundException.class, executable);
        }
    }

    @Nested
    @DisplayName("getPackageDetails method")
    class GetPackageDetailsTests {

        @Test
        void should_return_package_details_successfully() {
            // Given
            when(packageRepository.findByIdAndSender(any(UUID.class), any(EmployeeEntity.class)))
                    .thenReturn(Optional.of(packageEntity));
            when(employeeRepository.findById(any(UUID.class))).thenReturn(Optional.of(sender));

            // Mock the downstream order details response with the correct structure
            ShippingOrderDetails orderDetails = new ShippingOrderDetails(
                    packageId.toString(), // package ID
                    "Sample Package", // package name
                    PackageSize.M, // package size (mocked as Medium)
                    recipient.getPostalCode(), // postal code
                    recipient.getStreet(), // street name
                    recipient.getName(), // receiver name
                    OrderStatus.SENT, // order status
                    LocalDate.now().plusDays(3), // expected delivery date
                    LocalDateTime.now().plusDays(2) // actual delivery date
            );

            when(packageShippingServiceClient.getOrderDetails(any(String.class))).thenReturn(orderDetails);

            // When
            PackageDetails packageDetails = packageSelfServiceService.getPackageDetails(packageId.toString(), sender.getId().toString());

            // Then
            assertNotNull(packageDetails);
            assertEquals(packageId.toString(), packageDetails.packageId());
            verify(packageRepository, times(1)).findByIdAndSender(any(UUID.class), any(EmployeeEntity.class));
            verify(packageShippingServiceClient, times(1)).getOrderDetails(any(String.class)); // Verify order details fetching
        }

        @Test
        void should_throw_package_not_found_exception() {
            // Given
            when(packageRepository.findByIdAndSender(any(UUID.class), any(EmployeeEntity.class))).thenReturn(Optional.empty());
            when(employeeRepository.findById(any(UUID.class))).thenReturn(Optional.of(sender));

            // When
            Executable executable = () -> packageSelfServiceService.getPackageDetails(packageId.toString(), sender.getId().toString());

            // Then
            assertThrows(PackageNotFoundException.class, executable);
        }
    }

    @Nested
    @DisplayName("listPackageDetails method")
    class ListPackageDetailsTests {

        @Test
        void should_return_list_of_package_details_successfully() {
            // Given
            when(employeeRepository.findById(any(UUID.class))).thenReturn(Optional.of(sender));
            when(packageRepository.findBySender(any(EmployeeEntity.class))).thenReturn(List.of(packageEntity));

            // Mock the downstream order details response for each package
            ShippingOrderDetails orderDetails = new ShippingOrderDetails(
                    packageId.toString(), // package ID
                    "Sample Package", // package name
                    PackageSize.M, // package size
                    recipient.getPostalCode(), // postal code
                    recipient.getStreet(), // street name
                    recipient.getName(), // receiver name
                    OrderStatus.DELIVERED, // order status
                    LocalDate.now().plusDays(1), // expected delivery date
                    LocalDateTime.now() // actual delivery date
            );

            when(packageShippingServiceClient.getOrderDetails(any(String.class))).thenReturn(orderDetails);

            // When
            var packageDetailsList = packageSelfServiceService.listPackageDetails(sender.getId().toString(), Optional.empty());

            // Then
            assertNotNull(packageDetailsList);
            assertFalse(packageDetailsList.isEmpty());
            assertEquals(1, packageDetailsList.size());
            verify(packageRepository, times(1)).findBySender(any(EmployeeEntity.class));
        }

        @Test
        void should_throw_sender_not_found_exception() {
            // Given
            when(employeeRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

            // When
            Executable executable = () -> packageSelfServiceService.listPackageDetails(UUID.randomUUID().toString(), Optional.empty());

            // Then
            assertThrows(SenderNotFoundException.class, executable);
        }
    }
}
