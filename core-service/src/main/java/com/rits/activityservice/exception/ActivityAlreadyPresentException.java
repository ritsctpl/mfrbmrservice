package com.rits.activityservice.exception;

public class ActivityAlreadyPresentException extends RuntimeException {
    private String fieldName;
    private String fieldValue;
    public ActivityAlreadyPresentException(String fieldName, String fieldValue) {
        super(String.format("%s Already Present in the Database : %s",fieldName,fieldValue));
        this.fieldName=fieldName;
        this.fieldValue=fieldValue;
    }
}
