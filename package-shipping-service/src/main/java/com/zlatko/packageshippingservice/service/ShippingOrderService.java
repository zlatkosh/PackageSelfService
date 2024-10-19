package com.zlatko.packageshippingservice.service;

import com.zlatko.packageshippingservice.model.dto.ShippingOrder;
import com.zlatko.packageshippingservice.model.dto.ShippingOrderDetails;
import com.zlatko.packageshippingservice.model.entity.ShippingOrderEntity;
import com.zlatko.packageshippingservice.model.enums.OrderStatus;
import com.zlatko.packageshippingservice.model.enums.PackageSize;
import com.zlatko.packageshippingservice.model.exceptions.DuplicatePackageNameException;
import com.zlatko.packageshippingservice.repository.ShippingOrderRepository;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing shipping orders
 */
@Service
public class ShippingOrderService {
    private final ShippingOrderRepository shippingOrderRepository;

    public ShippingOrderService(ShippingOrderRepository shippingOrderRepository) {
        this.shippingOrderRepository = shippingOrderRepository;
    }

    /**
     * Create a new shipping order
     * @param shippingOrder The details of the shipping order to create
     * @return  The ID of the created shipping order
     */
    public UUID createShippingOrder(ShippingOrder shippingOrder) {
        Optional<ShippingOrderEntity> existingOrder = shippingOrderRepository.findByPackageName(shippingOrder.packageName());

        if (existingOrder.isPresent()) {
            throw new DuplicatePackageNameException("The selected packageName was already taken.");
        }

        ShippingOrderEntity entity = new ShippingOrderEntity(
                null,
                shippingOrder.packageName(),
                shippingOrder.postalCode(),
                shippingOrder.streetName(),
                shippingOrder.receiverName(),
                PackageSize.valueOf(shippingOrder.packageSize()),
                OrderStatus.IN_PROGRESS, // Set status to IN_PROGRESS by default. There should be a mechanism to update the status later
                java.time.LocalDate.now().plusWeeks(1), // Set expected delivery date to one week from now
                null

        );
        ShippingOrderEntity createdOrder = shippingOrderRepository.save(entity);
        return createdOrder.getId();
    }

    /**
     * Retrieve a list of all shipping orders
     * @return A list of all shipping orders
     */
    public List<ShippingOrderDetails> listShippingOrders(OrderStatus status, int offset, int limit) {
        Page<ShippingOrderEntity> allByStatus;
        if (status==null) {
            allByStatus = shippingOrderRepository.findAll(PageRequest.of(offset, limit));
        } else {
            allByStatus = shippingOrderRepository.findAllByStatus(status, PageRequest.of(offset, limit));
        }
        return allByStatus.stream()
                .map(entity -> new ShippingOrderDetails(
                        entity.getId().toString(),
                        entity.getPackageName(),
                        entity.getPackageSize(),
                        entity.getPostalCode(),
                        entity.getStreetName(),
                        entity.getReceiverName(),
                        entity.getStatus(),
                        entity.getExpectedDeliveryDate(),
                        entity.getActualDeliveryDateTime()
                ))
                .toList();
    }

    /**
     * Retrieve the details of a shipping order by its ID
     * @param orderId The ID of the order to retrieve
     * @return The details of the order with the given ID, or an empty Optional if no such order exists
     */
    public Optional<ShippingOrderDetails> getOrderDetails(String orderId) {
        UUID uuid = UUID.fromString(orderId);
        return shippingOrderRepository.findById(uuid)
                .map(entity -> new ShippingOrderDetails(
                        entity.getId().toString(),
                        entity.getPackageName(),
                        entity.getPackageSize(),
                        entity.getPostalCode(),
                        entity.getStreetName(),
                        entity.getReceiverName(),
                        entity.getStatus(),
                        entity.getExpectedDeliveryDate(),
                        entity.getActualDeliveryDateTime()
                ));
    }
}
