package com.rits.activityservice.exception;

public class ActivityNotFoundException extends RuntimeException {
    private String ActivityName;
    private String fieldName;
    private String fieldValue;
    public ActivityNotFoundException(String ActivityName, String fieldName, String fieldValue)
    {
        super(String.format("%s not found with %s: %s",ActivityName,fieldName,fieldValue));
        this.ActivityName=ActivityName;
        this.fieldName=fieldName;
        this.fieldValue=fieldValue;
    }
}
