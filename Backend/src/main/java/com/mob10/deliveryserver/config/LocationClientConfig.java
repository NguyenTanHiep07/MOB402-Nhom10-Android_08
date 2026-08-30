package com.mob10.deliveryserver.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class LocationClientConfig {
    @Bean
    @Qualifier("photonRestClient")
    RestClient photonRestClient(
            @Value("${app.location.photon-base-url}") String baseUrl,
            @Value("${app.location.request-timeout-ms}") long timeoutMs,
            @Value("${app.location.user-agent}") String userAgent) {
        return createClient(baseUrl, timeoutMs, userAgent);
    }

    @Bean
    @Qualifier("osrmRestClient")
    RestClient osrmRestClient(
            @Value("${app.location.osrm-base-url}") String baseUrl,
            @Value("${app.location.request-timeout-ms}") long timeoutMs,
            @Value("${app.location.user-agent}") String userAgent) {
        return createClient(baseUrl, timeoutMs, userAgent);
    }

    private RestClient createClient(String baseUrl, long timeoutMs, String userAgent) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        Duration timeout = Duration.ofMillis(timeoutMs);
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("User-Agent", userAgent)
                .build();
    }
}
