package com.project.contactsdemo.core.loggingfilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.UUID;

@Component
public class LoggingFilter extends OncePerRequestFilter {
    //Logger instance for logging
    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString();
        request.setAttribute("RequestID", requestId);
//         Log request details before processing the request.
        logger.info("Request: Method={}, URI={}, Headers={}, Request ID: {}, Request IP  ={}",
                request.getMethod(),
                request.getRequestURI(),
                getRequestHeaders(request),
                requestId,
                request.getRemoteAddr()); //kendi localimden localime ulaştığım için :0:0:0:...

        long startTime = System.currentTimeMillis();  // Capture the start time to measure processing duration.

        try {
            filterChain.doFilter(request, response);  // Continue with the next filter in the chain.
        } finally {
            long duration = System.currentTimeMillis() - startTime;  // Calculate how long the request took.

            //note: buradaki duration benim application'ım ne kadar sürede response verdiği!
            // Log response details after request processing.
            logger.info("Response: Status={}, URI={}, Duration (Response Time)={}ms, Headers={}, Payload = {}",
                    response.getStatus(),
                    request.getRequestURI(),
                    duration,
                    response.getHeaderNames(),
                    response);
            // Log any exceptions that occurred during processing.
            if (request.getAttribute("javax.servlet.error.exception") != null) {
                logger.error("Exception during request processing",
                        (Exception) request.getAttribute("javax.servlet.error.exception"));
            }
        }
    }
    // Utility method to extract request headers for logging.
    private String getRequestHeaders(HttpServletRequest request) {
        StringBuilder headers = new StringBuilder();
        request.getHeaderNames().asIterator()
                .forEachRemaining(header -> headers.append(header).append("=").append(request.getHeader(header)).append("; "));
        return headers.toString();
    }
}
