package com.rits.activityservice.exception;

import java.util.List;

public class HandleNotFoundException extends RuntimeException {
    private String ActivityHookList;
    private String fieldName;
    private List<String> fieldValue;
    public HandleNotFoundException(String ActivityHookList, String fieldName, List<String> fieldValue)
    {
        super(String.format("%s not found with %s: %s",ActivityHookList,fieldName,fieldValue));
        this.ActivityHookList=ActivityHookList;
        this.fieldName=fieldName;
        this.fieldValue=fieldValue;
    }
}
