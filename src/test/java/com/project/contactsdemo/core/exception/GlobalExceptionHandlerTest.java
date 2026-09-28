package com.project.contactsdemo.core.exception;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Runs the handler against a small test controller, without starting the application
 * (no database, Hazelcast or city service needed).
 */
class GlobalExceptionHandlerTest {

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new ThrowingController())
            .setControllerAdvice(new GlobalExceptionHandler())
            // Jackson's XML module is on the classpath (via JasperReports), so ask for JSON explicitly
            .defaultRequest(get("/").accept(MediaType.APPLICATION_JSON))
            .build();

    @Test
    void noDataFoundReturns404() throws Exception {
        mockMvc.perform(get("/no-data"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorStatus").value(1))
                .andExpect(jsonPath("$.errorMessage").value("There is no person found"));
    }

    @Test
    void openCircuitBreakerReturns503() throws Exception {
        mockMvc.perform(get("/circuit-open"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void unreachableExternalServiceReturns503WithoutInternalDetails() throws Exception {
        mockMvc.perform(get("/unreachable"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string(not(containsString("internal-host"))));
    }

    @Test
    void standardConstraintViolationReturns400WithFieldMessage() throws Exception {
        mockMvc.perform(post("/validate").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorStatus").value(1))
                .andExpect(jsonPath("$.errorMessage").value(containsString("email: ")));
    }

    @Test
    void customValidatorExceptionReturns400WithItsMessage() throws Exception {
        mockMvc.perform(get("/custom-validation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Birthdate cannot be empty"));
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        mockMvc.perform(post("/validate").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void wrongPathVariableTypeReturns400() throws Exception {
        mockMvc.perform(get("/typed/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Invalid value for parameter 'id'."));
    }

    @Test
    void rateLimitReturns429() throws Exception {
        mockMvc.perform(get("/rate-limited"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void unexpectedExceptionReturns500WithGenericMessage() throws Exception {
        mockMvc.perform(get("/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorMessage").value("An unexpected error occurred."))
                .andExpect(content().string(not(containsString("secret-table"))));
    }

    @Test
    void springExceptionKeepsItsOwnStatus() throws Exception {
        mockMvc.perform(get("/response-status"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorMessage").value("Already exists"));
    }

    @RestController
    static class ThrowingController {

        record EmailRequest(@Email String email) {
        }

        @GetMapping("/no-data")
        void noData() {
            throw new NoDataFoundException("There is no person found");
        }

        @GetMapping("/circuit-open")
        void circuitOpen() {
            CircuitBreaker breaker = CircuitBreaker.ofDefaults("test");
            breaker.transitionToOpenState();
            throw CallNotPermittedException.createCallNotPermittedException(breaker);
        }

        @GetMapping("/unreachable")
        void unreachable() {
            throw new ResourceAccessException("I/O error on GET request for \"http://internal-host:1234/x\"");
        }

        @PostMapping("/validate")
        void validate(@Valid @RequestBody EmailRequest request) {
        }

        @GetMapping("/custom-validation")
        void customValidation() {
            throw new ValidationControlException("Birthdate cannot be empty");
        }

        @GetMapping("/typed/{id}")
        void typed(@PathVariable Long id) {
        }

        @GetMapping("/rate-limited")
        void rateLimited() {
            throw new RateLimitException("Rate limit has been exceeded", "1.2.3.4", 5);
        }

        @GetMapping("/unexpected")
        void unexpected() {
            throw new IllegalStateException("constraint violation on secret-table");
        }

        @GetMapping("/response-status")
        void responseStatus() {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already exists");
        }
    }
}
