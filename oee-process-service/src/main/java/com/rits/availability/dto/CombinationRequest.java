package com.rits.availability.dto;

public class CombinationRequest {

    private String resourceId;
    private String site;
    private String shiftRef;

    public CombinationRequest(String resourceId, String site, String shiftRef) {
        this.resourceId = resourceId;
        this.site = site;
        this.shiftRef = shiftRef;
    }

    // Getters and setters
}
