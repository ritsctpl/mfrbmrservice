package com.rits.quality.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(basePackages = "com.rits.quality.exception")
public class QualityGlobalExceptionHandler {

    @ExceptionHandler(QualityServiceException.class)
    public ResponseEntity<String> handleQualityServiceException(QualityServiceException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
