package com.project.contactsdemo.core.exception;

public class RateLimitException extends RuntimeException {
    public RateLimitException(String message, String identifier, int limit) {
        super(message + " for " + identifier + " (limit: " + limit + ")");
    }
}

