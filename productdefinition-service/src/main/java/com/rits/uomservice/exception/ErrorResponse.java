package com.rits.uomservice.exception;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class ErrorResponse {

        private String message;
        private int code;
    private String details;


    }