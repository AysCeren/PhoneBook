package com.project.contactsdemo.core.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Hazelcast client connection, bound from {@code app.hazelcast.*}.
 *
 * @param clusterName cluster the client joins
 * @param address     member address as {@code host:port}
 */
@Validated
@ConfigurationProperties(prefix = "app.hazelcast")
public record HazelcastProperties(@NotBlank String clusterName, @NotBlank String address) {
}
