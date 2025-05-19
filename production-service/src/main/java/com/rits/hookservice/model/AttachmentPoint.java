package com.rits.hookservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "R_HOOK_POINT")
public class AttachmentPoint {

    @Id
    private String id;

    // Fully qualified name of the target class (e.g., com.rits.processorderstateservice.service.ProcessOrderStateServiceImpl)
    private String targetClass;

    // Target method name (e.g., startProcess)
    private String targetMethod;

    // Type can be "HOOK" (fire and forget, for logging/validation) or "EXTENSION" (which can modify inputs/outputs)
    private String hookType;

    // When to execute the hook: "BEFORE" or "AFTER"
    private String hookPoint;

    // The Spring bean name of the hook implementation (e.g., "myCustomHook")
    private String hookClass;

    // The hook method name to be invoked (e.g., "beforeStartProcess")
    private String hookMethod;

    // Execution mode: "SYNC" (default) or "ASYNC". Note: EXTENSION hooks always execute synchronously.
    private String executionMode = "SYNC";

    // Flag to enable or disable this attachment point
    private boolean active;

    // Getters and Setters

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getTargetClass() {
        return targetClass;
    }
    public void setTargetClass(String targetClass) {
        this.targetClass = targetClass;
    }
    public String getTargetMethod() {
        return targetMethod;
    }
    public void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod;
    }
    public String getHookType() {
        return hookType;
    }
    public void setHookType(String hookType) {
        this.hookType = hookType;
    }
    public String getHookPoint() {
        return hookPoint;
    }
    public void setHookPoint(String hookPoint) {
        this.hookPoint = hookPoint;
    }
    public String getHookClass() {
        return hookClass;
    }
    public void setHookClass(String hookClass) {
        this.hookClass = hookClass;
    }
    public String getHookMethod() {
        return hookMethod;
    }
    public void setHookMethod(String hookMethod) {
        this.hookMethod = hookMethod;
    }
    public String getExecutionMode() {
        return executionMode;
    }
    public void setExecutionMode(String executionMode) {
        this.executionMode = executionMode;
    }
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
}
