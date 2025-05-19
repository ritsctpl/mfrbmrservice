package com.rits.exception;

import com.rits.activitygroupservice.exception.ActivityGroupException;
import com.rits.activityservice.exception.ActivityException;
import com.rits.auditlogservice.exception.AuditLogException;
import com.rits.barcodeservice.exception.BarCodeException;
import com.rits.customdataservice.Exception.CustomDataException;
import com.rits.cycletimeservice.exception.CycleTimeException;
import com.rits.dataFieldService.exception.DataFieldException;
import com.rits.datatypeservice.exception.DataTypeException;
import com.rits.mfrscreenconfigurationservice.exception.MFRScreenConfigurationException;
import com.rits.mfrscreenconfigurationservice.model.MFRScreenConfiguration;
import com.rits.mfrservice.Exception.MfrException;
import com.rits.podservice.exception.PodException;
import com.rits.site.exception.SiteException;
import com.rits.usergroupservice.exception.UserGroupException;
import com.rits.userservice.exception.UserException;
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



    @ExceptionHandler(ActivityException.class)
    public ResponseEntity<ErrorDetails> handleActivityException(ActivityException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }
    @ExceptionHandler(BarCodeException.class)
    public ResponseEntity<ErrorDetails> handleBarCodeException(BarCodeException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }
    @ExceptionHandler(ActivityGroupException.class)
    public ResponseEntity<ErrorDetails> handleActivityGroupException(ActivityGroupException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(SiteException.class)
    public ResponseEntity<ErrorDetails> handleSiteException(SiteException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorDetails> handleUserException(UserException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(UserGroupException.class)
    public ResponseEntity<ErrorDetails> handleUserGroupException(UserGroupException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(CustomDataException.class)
    public ResponseEntity<ErrorDetails> handleCustomDataException(CustomDataException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(DataFieldException.class)
    public ResponseEntity<ErrorDetails> handleDataFieldException(DataFieldException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(DataTypeException.class)
    public ResponseEntity<ErrorDetails> handleDataTypeException(DataTypeException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(PodException.class)
    public ResponseEntity<ErrorDetails> handlePodException(PodException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(CycleTimeException.class)
    public ResponseEntity<ErrorDetails> handleCycleTimeException(CycleTimeException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }
    @ExceptionHandler(MfrException.class)
    public ResponseEntity<ErrorDetails> handleMfrException(MfrException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(MFRScreenConfigurationException.class)
    public ResponseEntity<ErrorDetails> handleMfrScreenConfigurationException(MFRScreenConfigurationException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }
    @ExceptionHandler(AuditLogException.class)
    public ResponseEntity<ErrorDetails> handleAuditLogException(AuditLogException ex, WebRequest request) {
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