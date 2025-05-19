package com.rits.processlotservice.exception;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.Locale;

@ControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler(ProcessLotException.class)
    public ResponseEntity<ErrorDetails> handleRoutingException(ProcessLotException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleAllExceptions(Exception ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), ex.getMessage(), request.getDescription(false), "internal_server_error");
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }
}
