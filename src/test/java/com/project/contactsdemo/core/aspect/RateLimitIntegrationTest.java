package com.project.contactsdemo.core.aspect;

import com.project.contactsdemo.core.config.RateLimiterConfig;
import com.project.contactsdemo.support.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

/** Checks that @RateLimited endpoints are limited end to end (aspect, service, exception handler). */
class RateLimitIntegrationTest extends IntegrationTest {

    @Autowired
    private RateLimiterConfig rateLimiterConfig;

    @BeforeEach
    void enableRateLimiting() {
        rateLimiterConfig.setEnabled(true); //disabled for other tests in application-test.yaml
    }

    @AfterEach
    void disableRateLimiting() {
        rateLimiterConfig.setEnabled(false);
    }

    @Test
    void getAllContactAllowsFiveRequestsPerMinuteThenReturns429() {
        for (int i = 0; i < 5; i++) {
            assertThat(get("/api/getAllContact").getStatusCode()).isNotEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        }
        assertThat(get("/api/getAllContact").getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }
}
