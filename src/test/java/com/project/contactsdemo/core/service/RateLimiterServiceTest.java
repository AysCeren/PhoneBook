package com.project.contactsdemo.core.service;

import com.project.contactsdemo.core.config.RateLimiterConfig;
import com.project.contactsdemo.core.exception.RateLimitException;
import com.project.contactsdemo.core.properties.RateLimitProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RateLimiterServiceTest {

    private static final String SERVICE = "TEST_SERVICE";
    private static final int LIMIT = 5;

    private final MutableClock clock = new MutableClock(Instant.parse("2026-01-01T10:00:00Z"));
    private RateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setRateLimit(LIMIT);
        properties.setTimeFrameMinutes(1L);
        RateLimiterConfig config = new RateLimiterConfig();
        config.setEnabled(true);
        config.setLimits(Map.of(SERVICE, properties));
        rateLimiterService = new RateLimiterService(config, clock);
    }

    @Test
    void allowsRequestsUpToTheLimitThenRejects() {
        for (int i = 0; i < LIMIT; i++) {
            rateLimiterService.registerRequest("1.2.3.4", SERVICE);
        }
        assertThatThrownBy(() -> rateLimiterService.registerRequest("1.2.3.4", SERVICE))
                .isInstanceOf(RateLimitException.class);
    }

    @Test
    void countsEachClientSeparately() {
        for (int i = 0; i < LIMIT; i++) {
            rateLimiterService.registerRequest("1.2.3.4", SERVICE);
        }
        assertThatNoException().isThrownBy(() -> rateLimiterService.registerRequest("5.6.7.8", SERVICE));
    }

    @Test
    void startsANewWindowAfterTheTimeFrame() {
        for (int i = 0; i < LIMIT; i++) {
            rateLimiterService.registerRequest("1.2.3.4", SERVICE);
        }
        clock.advance(Duration.ofMinutes(1));
        assertThatNoException().isThrownBy(() -> rateLimiterService.registerRequest("1.2.3.4", SERVICE));
    }

    @Test
    void ignoresServicesWithoutAConfiguredLimit() {
        for (int i = 0; i < LIMIT * 10; i++) {
            rateLimiterService.registerRequest("1.2.3.4", "UNCONFIGURED");
        }
    }

    @Test
    void neverAllowsMoreThanTheLimitUnderConcurrentRequests() throws InterruptedException {
        int threads = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger allowed = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    start.await(); //release all threads at the same moment
                    rateLimiterService.registerRequest("1.2.3.4", SERVICE);
                    allowed.incrementAndGet();
                } catch (RateLimitException | InterruptedException ignored) {
                    // rejected requests are expected
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        done.await();
        executor.shutdown();

        assertThat(allowed.get()).isEqualTo(LIMIT);
    }

    /** A clock that only moves when the test tells it to. */
    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }
}
