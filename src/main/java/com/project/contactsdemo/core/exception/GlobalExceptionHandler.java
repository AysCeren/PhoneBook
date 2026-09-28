package com.project.contactsdemo.core.exception;

import com.project.contactsdemo.core.dto.GenericDTO;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * Turns exceptions into {@link GenericDTO} error responses with a matching HTTP status.
 * <p>
 * Only messages written for users (our own exceptions, validation messages) are sent to the client.
 * Anything unexpected is logged in full on the server and answered with a generic message, so internal
 * details such as hostnames, SQL or class names never reach the caller.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String UNEXPECTED_ERROR_MESSAGE = "An unexpected error occurred.";

    @ExceptionHandler({NoDataFoundException.class})
    public ResponseEntity<GenericDTO<Void>> handleDataNotFoundException(NoDataFoundException exception) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    // The circuit breaker is open: a dependency is down, not the client's fault. 503 tells clients to retry later.
    @ExceptionHandler({CallNotPermittedException.class})
    public ResponseEntity<GenericDTO<Void>> handleCallNotPermittedException(CallNotPermittedException exception) {
        logger.warn("Call rejected by circuit breaker: {}", exception.getMessage());
        return error(HttpStatus.SERVICE_UNAVAILABLE, "Servise şuan erişilemiyor.");
    }

    // An external service could not be reached or timed out (connection refused, unknown host, read timeout).
    @ExceptionHandler({ResourceAccessException.class})
    public ResponseEntity<GenericDTO<Void>> handleUnreachableDependency(ResourceAccessException exception) {
        logger.warn("External service unreachable: {}", exception.getMessage());
        return error(HttpStatus.SERVICE_UNAVAILABLE, "A required external service is currently unavailable.");
    }

    // An external service answered, but with an error or an unusable response: 502 Bad Gateway.
    @ExceptionHandler({RestClientException.class})
    public ResponseEntity<GenericDTO<Void>> handleFailingDependency(RestClientException exception) {
        logger.warn("External service call failed", exception);
        return error(HttpStatus.BAD_GATEWAY, "A required external service returned an error.");
    }

    // Standard constraints (e.g. @Email) that fail by returning false end up here.
    // The response is built directly: an exception thrown inside an @ExceptionHandler is not passed
    // to another handler; Spring logs it and falls back to its default error response instead.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericDTO<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return error(HttpStatus.BAD_REQUEST, message);
    }

    // Our custom validators throw this directly with a user-facing message.
    @ExceptionHandler({ValidationControlException.class})
    public ResponseEntity<GenericDTO<Void>> handleValidationErrors(ValidationControlException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Body is missing or is not valid JSON.
    @ExceptionHandler({HttpMessageNotReadableException.class})
    public ResponseEntity<GenericDTO<Void>> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return error(HttpStatus.BAD_REQUEST, "Request body is missing or malformed.");
    }

    // A path or query parameter has the wrong type, e.g. /getAllContactsOfPerson/abc for a Long id.
    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    public ResponseEntity<GenericDTO<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return error(HttpStatus.BAD_REQUEST, "Invalid value for parameter '" + ex.getName() + "'.");
    }

    @ExceptionHandler({RateLimitException.class})
    public ResponseEntity<GenericDTO<Void>> handleRateLimitException(RateLimitException exception) {
        return error(HttpStatus.TOO_MANY_REQUESTS, exception.getMessage());
    }

    @ExceptionHandler({RuntimeException.class})
    public ResponseEntity<GenericDTO<Void>> handleRuntimeException(RuntimeException exception) {
        // Spring exceptions that already carry a status (e.g. ResponseStatusException) keep it.
        HttpStatusCode status = exception instanceof ErrorResponse errorResponse
                ? errorResponse.getStatusCode()
                : HttpStatus.INTERNAL_SERVER_ERROR;
        if (status.is5xxServerError()) {
            logger.error("Unhandled exception", exception); //full details stay in the server log
            return error(status, UNEXPECTED_ERROR_MESSAGE);
        }
        // A 4xx from a Spring ErrorResponse (e.g. ResponseStatusException): its detail is written for the client.
        String detail = ((ErrorResponse) exception).getBody().getDetail();
        return error(status, detail != null ? detail : status.toString());
    }

    private static ResponseEntity<GenericDTO<Void>> error(HttpStatusCode status, String message) {
        GenericDTO<Void> errorGenericDTO = new GenericDTO<>(null, 1);
        errorGenericDTO.setErrorMessage(message);
        return ResponseEntity.status(status).body(errorGenericDTO);
    }
}
