package com.rits.availability.service;

import com.rits.availability.dto.*;
import com.rits.availability.model.OeeAvailabilityEntity;
import com.rits.common.dto.OeeFilterRequest;

import java.time.LocalDate;
import java.util.List;

public interface AvailabilityService {
    OverallAvailabilityResponse getOverallAvailability(OeeFilterRequest request);
    AvailabilityByTimeResponse getAvailabilityByTime(OeeFilterRequest request);
    AvailabilityByShiftResponse getAvailabilityByShift(OeeFilterRequest request);
    AvailabilityByMachineResponse getAvailabilityByMachine(OeeFilterRequest request);

    AvailabilityByWorkcenterResponse getAvailabilityByWorkcenter(OeeFilterRequest request) throws Exception;

    AvailabilityByMachineAndDateRangeResponse getAvailabilityByResourceAndDateRange(OeeFilterRequest request);

    AvailabilityByDownTimeResponse getAvailabilityByDownTime(OeeFilterRequest request);
    DownTimeHeatMapResponse getDownTimeHeatMap(OeeFilterRequest request);

    AvailabilityByWorkcenterAndDateRangeResponse getAvailabilityByWorkcenterAndDateRange(OeeFilterRequest oeeFilterRequest);

    AvailabilityResponse getLogAvailability(AvailabilityRequest availabilityRequest);

    CalculateAvailabilityResponse calculateAvailability(AvailabilityRequest availabilityRequest);

    AvailabilityResponse deleteAvailability(AvailabilityRequest availabilityRequest);
    Boolean logAvailability(AvailabilityRequest availabilityRequest);
    List<AggregatedAvailabilityDTO> getGroupedAvailability(List<CombinationRequest> combinations);
    List<OverallAvailabilityResponse> findAvailability(AvailabilityRequest request);
}
