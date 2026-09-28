package com.project.contactsdemo.core.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * External city lookup service, bound from {@code app.city-service.*}.
 *
 * @param baseUrl        endpoint that returns a city by its code, e.g. {@code http://host:port/.../get-with-ilkodu}
 * @param connectTimeout maximum time to establish the TCP connection
 * @param readTimeout    maximum time to wait for response data once connected
 */
@Validated
@ConfigurationProperties(prefix = "app.city-service")
public record CityServiceProperties(
        @NotBlank String baseUrl,
        @NotNull @DefaultValue("2s") Duration connectTimeout,
        @NotNull @DefaultValue("3s") Duration readTimeout) {
}
