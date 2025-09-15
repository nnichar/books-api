package com.example.books_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation errors thrown when @Valid fails on request bodies.
     * Example: empty "title" or "author", or invalid JSON field format.
     * Returns: 400 with detailed list of field-level validation messages.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> handleValidation(MethodArgumentNotValidException ex,
                                                               HttpServletRequest request) {
        List<Map<String, String>> errors = new ArrayList<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            Map<String, String> m = new HashMap<>();
            m.put("field", fe.getField());
            m.put("message", fe.getDefaultMessage());
            errors.add(m);
        }
        Map<String,Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("errors", errors);
        body.put("path", request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Handles IllegalArgumentException thrown inside service/controller.
     * Example: invalid date (year <= 1000, year > current year),
     * or missing/blank author parameter (if manually checked).
     * Returns: 400 with a simple error message.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,Object>> handleIllegalArgument(IllegalArgumentException ex,
                                                                    HttpServletRequest request) {
        Map<String,Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("errors", List.of(Map.of("message", ex.getMessage())));
        body.put("path", request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Handles missing required query parameters.
     * Example: GET /books without providing "author" query parameter.
     * Returns: 400 with a message indicating which parameter is missing.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String,Object>> handleMissingParam(MissingServletRequestParameterException ex,
                                                                 HttpServletRequest request) {
        Map<String,Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("errors", List.of(Map.of("message", ex.getParameterName() + " is required")));
        body.put("path", request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }
}
