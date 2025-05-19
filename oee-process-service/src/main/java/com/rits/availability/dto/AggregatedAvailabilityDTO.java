package com.rits.availability.dto;

public class AggregatedAvailabilityDTO {

    private String resourceId;
    private String workcenterId;
    private String site;
    private String shiftRef;
    private Double totalRuntime;
    private Double avgAvailabilityPercentage;
    private Double totalActualAvailableTime;

    public AggregatedAvailabilityDTO(String resourceId, String workcenterId, String site, String shiftRef,
                                     Double totalRuntime, Double avgAvailabilityPercentage, Double totalActualAvailableTime) {
        this.resourceId = resourceId;
        this.workcenterId = workcenterId;
        this.site = site;
        this.shiftRef = shiftRef;
        this.totalRuntime = totalRuntime;
        this.avgAvailabilityPercentage = avgAvailabilityPercentage;
        this.totalActualAvailableTime = totalActualAvailableTime;
    }

    // Getters and setters
}
