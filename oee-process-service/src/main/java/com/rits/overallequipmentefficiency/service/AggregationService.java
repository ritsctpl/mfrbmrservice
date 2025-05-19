package com.rits.overallequipmentefficiency.service;

import com.rits.overallequipmentefficiency.dto.*;
import com.rits.overallequipmentefficiency.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AggregationService {

    // Individual aggregation methods
/*
    AggregatedAvailability aggregateAvailability(OeeOutput oeeData);
    AggregatedPerformance aggregatePerformance(OeeOutput oeeData, AggregatedAvailability aggregatedAvailability);
    AggregatedQuality aggregateQuality(OeeOutput oeeData, AggregatedAvailability aggregatedAvailability,
                                       AggregatedPerformance aggregatedPerformance);
    AggregatedOee aggregateOee(OeeOutput oeeData, AggregatedAvailability aggregatedAvailability,
                               AggregatedPerformance aggregatedPerformance, AggregatedQuality aggregatedQuality);
*/

    // Composite method to process the overall aggregation
    /*AggregatedOeeResponseDTO processAggregation(OeeOutput oeeData);

    // Retrieval methods
    List<AggregatedOee> getAggregatedOeeByDate(LocalDate date);
    List<AggregatedOee> getAggregatedOeeByShift(String shiftId);
    IntervalTimesResponse getIntervalTimes(OeeProductionLogRequest request,
                                           LocalDateTime intervalStartDateTime,
                                           LocalDateTime intervalEndDateTime,
                                           String category);
*/

    AggregatedOee aggregateOee(OeeOutput oeeOutput);

    AggregatedTimePeriod aggregatedTimePeriod(AggregatedTimePeriodInput input);
}
