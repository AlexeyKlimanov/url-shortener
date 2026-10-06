package io.github.AlexeyKlimanov.url_shortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handlerIllegalArgument(IllegalArgumentException ex){
        Map<String, Object> body = Map.of(
           "timestamp", LocalDateTime.now(),
           "status", 409,
           "error", "Conflict",
           "message", ex.getMessage() 
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
}
