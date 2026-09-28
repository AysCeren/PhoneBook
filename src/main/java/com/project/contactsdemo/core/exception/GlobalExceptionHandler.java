package com.project.contactsdemo.core.exception;

import com.project.contactsdemo.core.dto.GenericDTO;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler{

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler({NoDataFoundException.class})
    public ResponseEntity<?> handleDataNotFoundException(NoDataFoundException exception) {
        GenericDTO<Void> errorGenericDTO =new GenericDTO<>(null,1);
        errorGenericDTO.setErrorMessage(exception.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) //404
                .body(errorGenericDTO);
    }

    @ExceptionHandler({CallNotPermittedException.class})
    public ResponseEntity<?> handleCallNotPermittedException(CallNotPermittedException exception) {
        GenericDTO<Void> errorGenericDTO =new GenericDTO<>(null,1);
        errorGenericDTO.setErrorMessage("Servise şuan erişilemiyor.");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) //404
                .body(errorGenericDTO);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericDTO<?>> handleValidationExceptions(MethodArgumentNotValidException ex) throws ValidationControlException {
        throw new ValidationControlException(ex.getMessage());
    }
    @ExceptionHandler({ValidationControlException.class})
    public ResponseEntity<?> handleValidationErrors(ValidationControlException ex) {
        GenericDTO<Void> errorGenericDTO =new GenericDTO<>(null,1);
        errorGenericDTO.setErrorMessage(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorGenericDTO);
    }
    @ExceptionHandler({RuntimeException.class})
    public ResponseEntity<GenericDTO<?>> handleRuntimeException(RuntimeException exception) {
        GenericDTO<Void> errorGenericDTO =new GenericDTO<>(null,1);
        errorGenericDTO.setErrorMessage(exception.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorGenericDTO);
    }
    @ExceptionHandler({RateLimitException.class})
    public ResponseEntity<?> handleRateLimitException(RateLimitException exception) {
        GenericDTO<Void> errorGenericDTO =new GenericDTO<>(null,1);
        errorGenericDTO.setErrorMessage(exception.getMessage());
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(errorGenericDTO);
    }
}