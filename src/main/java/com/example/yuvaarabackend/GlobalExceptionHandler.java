package com.example.yuvaarabackend;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> handleMissingParams(MissingServletRequestParameterException ex) {
        return ResponseEntity.status(400)
                .body(Map.of("info", "Missing required fields"));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        // ConstraintViolationException contain ConstraintViolation objects
        // .getConstraintViolations give this objects, .stream() give stream this violation objects.
        // .map(ConstraintViolation::getMessage => //just get violation message by object) => ["violation", "violation"]...
        // .collect(Collectors.joining(", ") => Collect all mesages in one string
        String messages = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        return ResponseEntity.status(400)
                .body(Map.of("info", messages));
    }
}

