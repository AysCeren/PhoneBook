package com.project.contactsdemo.core.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * External city lookup service, bound from {@code app.city-service.*}.
 *
 * @param baseUrl endpoint that returns a city by its code, e.g. {@code http://host:port/.../get-with-ilkodu}
 */
@Validated
@ConfigurationProperties(prefix = "app.city-service")
public record CityServiceProperties(@NotBlank String baseUrl) {
}
