package com.zlatko.packageselfservicebackend.clients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
public class ClientConfig {

    @Bean
    public WebClient packageShippingServiceWebClient(@Value("${app.client.packageShippingService.baseurl}") String baseUrl, WebClient.Builder webClientBuilder) {
        log.debug("Initializing packageShippingServiceWebClient, with base URL: {}", baseUrl);
        return webClientBuilder
                .baseUrl(baseUrl)
                .build();
    }
}
