package com.rits.oeeservice.dto;

public class ParameterMetaDto {
    private String name;
    private String type;
    private boolean required;
    // New field for configuring the array element type (e.g., "integer", "text", etc.)
    private String elementType;

    // Getters and Setters
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public boolean isRequired() {
        return required;
    }
    public void setRequired(boolean required) {
        this.required = required;
    }
    public String getElementType() {
        return elementType;
    }
    public void setElementType(String elementType) {
        this.elementType = elementType;
    }
}

/*
package com.rits.oeeservice.dto;

public class ParameterMetaDto {
    private String name;
    private String type;
    private boolean required;

    // Getters and Setters
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public boolean isRequired() {
        return required;
    }
    public void setRequired(boolean required) {
        this.required = required;
    }
}
*/
