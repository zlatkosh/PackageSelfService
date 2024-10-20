package com.zlatko.packageselfservicebackend.clients;

import com.zlatko.packageselfservicebackend.clients.dtos.ShippingOrder;
import com.zlatko.packageselfservicebackend.clients.dtos.ShippingOrderDetails;
import com.zlatko.packageselfservicebackend.model.exceptions.DuplicatePackageNameException;
import com.zlatko.packageselfservicebackend.utils.GlobalConstants;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.net.URI;
import java.util.Objects;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

/**
 * Client class for the package-shipping-service API.
 */
@Service
@Slf4j
public class PackageShippingServiceClient {

    public static final String CREATE_SHIPPING_ORDER = "createShippingOrder";
    public static final String CREATE_SHIPPING_ORDER_FALLBACK = "createShippingOrderFallback";
    public static final String GET_ORDER_DETAILS_FALLBACK = "getOrderDetailsFallback";
    private final WebClient webClient;

    public PackageShippingServiceClient(@Qualifier("packageShippingServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Creates a shipping order by sending a POST request to the package-shipping-service API.<br>
     * If the package name is already taken (HTTP status 409), a DuplicatePackageNameException is
     * thrown and handled by the global exception handler. <br>
     * An X-Correlation-ID header is added to the request for tracing purposes. <br>
     * The method is annotated with @Retry and @CircuitBreaker annotations for resilience purposes. <br>
     * In case of a circuit breaker open state, the createShippingOrderFallback method is called. <br>
     *
     * @param shippingOrder the shipping order
     * @return the URI of the created shipping order
     */
    @Retry(name = CREATE_SHIPPING_ORDER)
    @CircuitBreaker(name = CREATE_SHIPPING_ORDER, fallbackMethod = CREATE_SHIPPING_ORDER_FALLBACK)
    public URI createShippingOrder(ShippingOrder shippingOrder) {
        log.trace("Creating shipping order: {}", shippingOrder);
        return Objects.requireNonNull(webClient.post()
                        .uri(buildURI("shippingOrders"))
                        .bodyValue(shippingOrder)
                        .header(GlobalConstants.X_CORRELATION_ID, MDC.get(GlobalConstants.X_CORRELATION_ID))
                        .retrieve()
                        .onStatus(status -> status == HttpStatus.CONFLICT, response -> {
                            // Handle 409 Conflict
                            return Mono.error(new DuplicatePackageNameException(shippingOrder.packageName()));
                        })
                        .toBodilessEntity()
                        .block())
                .getHeaders()
                .getLocation();
    }

    /**
     * Circuit breaker fallback method for createShippingOrder.
     * In case of a circuit breaker open state, this method is called.
     * It throws a RuntimeException which is afterward handled by the global exception handler.
     *
     * @param shippingOrder the shipping order
     * @param err the error
     * @return nothing, throws a RuntimeException
     */
    protected URI createShippingOrderFallback(ShippingOrder shippingOrder, Throwable err) {
        String message = "OPEN state circuitbreaker! Shipping order '%s', could not be processed!".formatted(shippingOrder);
        throw new RuntimeException(message, err);
    }

    /**
     * Gets the details of a shipping order by sending a GET request to the package-shipping-service API.<br>
     * An X-Correlation-ID header is added to the request for tracing purposes. <br>
     * The method is annotated with @Retry and @CircuitBreaker annotations for resilience purposes. <br>
     * In case of a circuit breaker open state, the getOrderDetailsFallback method is called. <br>
     *
     * @param orderId the order ID
     * @return the shipping order details
     */
    @Retry(name = CREATE_SHIPPING_ORDER)
    @CircuitBreaker(name = CREATE_SHIPPING_ORDER, fallbackMethod = GET_ORDER_DETAILS_FALLBACK)
    public ShippingOrderDetails getOrderDetails(String orderId) {
        log.trace("Getting order details for order ID: {}", orderId);
        return webClient.get()
                .uri(buildURI("shippingOrders", orderId))
                .retrieve()
                .bodyToMono(ShippingOrderDetails.class)
                .block();
    }

    /**
     * Circuit breaker fallback method for getOrderDetails.
     * In case of a circuit breaker open state, this method is called.
     * It throws a RuntimeException which is afterward handled by the global exception handler.
     *
     * @param orderId the order ID
     * @param err the error
     * @return nothing, throws a RuntimeException
     */
    protected ShippingOrderDetails getOrderDetailsFallback(String orderId, Throwable err) {
        String message = "OPEN state circuitbreaker! Order details for order ID '%s', could not be retrieved!".formatted(orderId);
        throw new RuntimeException(message, err);
    }

    /**
     * Builds a URI with the given path segments by using a UriBuilder. <br>
     * Example: {@code buildURI("shippingOrders", orderId)} -> {@code /shippingOrders/{orderId}} <br>
     *
     * @param pathSegments the path segments
     * @return the URI
     */
    protected Function<UriBuilder, URI> buildURI(String... pathSegments) {
        return uriBuilder -> {
            URI uri = uriBuilder.pathSegment(pathSegments)
                    .build();
            log.trace("Created URI: {}", uri);
            return uri;
        };
    }
}