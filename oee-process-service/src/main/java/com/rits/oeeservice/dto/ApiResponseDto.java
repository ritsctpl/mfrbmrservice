package com.rits.oeeservice.dto;

public class ApiResponseDto {
    private String status;
    private Object data;
    private String message;

    public ApiResponseDto() {}

    public ApiResponseDto(String status, Object data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public Object getData() {
        return data;
    }
    public void setData(Object data) {
        this.data = data;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
