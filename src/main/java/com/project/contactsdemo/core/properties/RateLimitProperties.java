package com.project.contactsdemo.core.properties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RateLimitProperties {
    private int rateLimit;
    private Long timeFrameMinutes;
}
