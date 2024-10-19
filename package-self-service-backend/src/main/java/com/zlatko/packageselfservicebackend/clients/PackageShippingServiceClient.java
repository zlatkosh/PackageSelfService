package com.zlatko.packageselfservicebackend.clients;

import com.zlatko.packageselfservicebackend.clients.dtos.ShippingOrder;
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

@Service
@Slf4j
public class PackageShippingServiceClient {

    public static final String CREATE_SHIPPING_ORDER = "createShippingOrder";
    public static final String CREATE_SHIPPING_ORDER_FALLBACK = "createShippingOrderFallback";
    private final WebClient webClient;

    public PackageShippingServiceClient(@Qualifier("packageShippingServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

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

    protected Function<UriBuilder, URI> buildURI(String... pathSegments) {
        return uriBuilder -> {
            URI uri = uriBuilder.pathSegment(pathSegments)
                    .build();
            log.trace("Created URI: {}", uri);
            return uri;
        };
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
}