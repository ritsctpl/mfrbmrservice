package com.rits.exception;

import com.rits.downtimeservice.exception.DownTimeException;
import com.rits.managementservice.exception.ManageException;
import com.rits.oeeservice.exception.OeeException;
import com.rits.performanceservice.exception.PerformanceException;
import com.rits.qualityservice.exception.QualityException;
import com.rits.queryBuilder.exception.QueryBuilderException;
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

    @ExceptionHandler(DownTimeException.class)
    public ResponseEntity<ErrorDetails> handleDownTimeException(DownTimeException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(QualityException.class)
    public ResponseEntity<ErrorDetails> handleQualityException(QualityException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(PerformanceException.class)
    public ResponseEntity<ErrorDetails> handlePerformanceException(PerformanceException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(OeeException.class)
    public ResponseEntity<ErrorDetails> handleOeeException(OeeException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(QueryBuilderException.class)
    public ResponseEntity<ErrorDetails> handleQueryBuilderException(QueryBuilderException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(ManageException.class)
    public ResponseEntity<ErrorDetails> handleManagementException(ManageException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleAllExceptions(Exception ex, WebRequest request) {
        String errorMessage = messageSource.getMessage("internal_server_error", null, Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), "internal_server_error");
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

}
