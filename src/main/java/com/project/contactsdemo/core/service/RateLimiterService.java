package com.project.contactsdemo.core.service;

import com.project.contactsdemo.core.config.RateLimiterConfig;
import com.project.contactsdemo.core.exception.RateLimitException;
import com.project.contactsdemo.core.properties.RateLimitProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fixed-window rate limiter: each (client, service) pair may make {@code rateLimit} requests
 * per window of {@code timeFrameMinutes}. A window starts with the first request and is
 * replaced by a new one once it has ended.
 * <p>
 * State is kept in memory, so each application instance counts separately.
 */
@Service
public class RateLimiterService {

    /** Requests seen in the current window (including rejected ones), and when the window ends. */
    private record Window(int count, Instant resetAt) {
    }

    private final RateLimiterConfig config;
    private final Clock clock;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    @Autowired
    public RateLimiterService(RateLimiterConfig config) {
        this(config, Clock.systemUTC());
    }

    // for tests: lets them control time instead of waiting for a real window to pass
    RateLimiterService(RateLimiterConfig config, Clock clock) {
        this.config = config;
        this.clock = clock;
    }

    public void registerRequest(String identifier, String service) {
        RateLimitProperties limitConfig = config.getLimits().get(service);
        if (limitConfig == null) {
            return; //no limit configured for this service
        }
        Instant now = clock.instant();
        Duration timeFrame = Duration.ofMinutes(limitConfig.getTimeFrameMinutes());

        // compute() runs atomically per key: check and increment happen as one step, so two
        // concurrent requests can't both see the same count. Other keys are not blocked.
        Window window = windows.compute(identifier + ":" + service, (key, current) ->
                current == null || !now.isBefore(current.resetAt())
                        ? new Window(1, now.plus(timeFrame))                   //first request, or the old window ended
                        : new Window(current.count() + 1, current.resetAt())); //same window: count this request

        if (window.count() > limitConfig.getRateLimit()) {
            throw new RateLimitException("Rate limit has been exceeded", identifier, limitConfig.getRateLimit());
        }
    }

    /** Removes windows that have ended, so clients that stop sending requests don't stay in memory. */
    @Scheduled(fixedRate = 60_000)
    public void removeExpiredWindows() {
        Instant now = clock.instant();
        windows.values().removeIf(window -> !now.isBefore(window.resetAt()));
    }
}
