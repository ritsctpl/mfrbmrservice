package com.rits.overallequipmentefficiency.service;

import com.rits.overallequipmentefficiency.dto.AvailabilityRequest;
import com.rits.overallequipmentefficiency.dto.PerformanceInput;
import com.rits.overallequipmentefficiency.model.AvailabilityEntity;

import java.util.List;

public interface AvailabilityService {
    List<AvailabilityEntity>  logAvailabilityBetweenHours(AvailabilityRequest request) ;
    List<AvailabilityEntity> getAvailabilityBetweenHours(AvailabilityRequest request);

    public List<PerformanceInput> logAvailabiltyAndPublish(AvailabilityRequest request);

    public List<PerformanceInput> logLineAvailability(AvailabilityRequest request);

    List<AvailabilityRequest> splitAndPublishLineAvailability(AvailabilityRequest request);

    void publishLineAvailabilityRequests(AvailabilityRequest request);
}
