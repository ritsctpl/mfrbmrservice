package com.rits.availability.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(basePackages = "com.rits.availability.exception")
public class AvailabilityGlobalExceptionHandler {

    @ExceptionHandler(AvailabilityException.class)
    public ResponseEntity<String> handleAvailabilityException(AvailabilityException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // Other exception handlers
}
