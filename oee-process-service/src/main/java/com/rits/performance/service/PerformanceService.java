package com.rits.performance.service;

import com.rits.common.dto.OeeFilterRequest;
import com.rits.performance.dto.*;
import com.rits.performance.model.OeePerformanceEntity;

import java.util.List;

public interface PerformanceService {

    // Method to fetch overall performance data
    OverallPerformanceResponse getOverallPerformance(OeeFilterRequest request) throws Exception;

    // Method to fetch performance by time
    PerformanceByTimeResponse getPerformanceByTime(OeeFilterRequest request) throws Exception;

    // Method to fetch performance by shift
    PerformanceByShiftResponse getPerformanceByShift(OeeFilterRequest request) throws Exception;

    // Method to fetch performance by machine
    PerformanceByMachineResponse getPerformanceByMachine(OeeFilterRequest request) throws Exception;

    // Method to fetch performance by production line
    PerformanceByProductionLineResponse getPerformanceByProductionLine(OeeFilterRequest request) throws Exception;

    // Method to fetch performance by reason
    PerformanceByReasonResponse getPerformanceByReason(OeeFilterRequest request);

    // Method to fetch performance heatmap data
    PerformanceHeatMapResponse getPerformanceHeatMap(OeeFilterRequest request);

    // Method to fetch performance by downtime analysis
    PerformanceByDowntimeResponse getPerformanceByDowntime(OeeFilterRequest request);

    // Method to fetch performance by operator
    PerformanceByOperatorResponse getPerformanceByOperator(OeeFilterRequest request);

    // Method to compare performance across machines
    /*PerformanceComparisonResponse getPerformanceComparison(OeeFilterRequest request);*/

    Boolean calculatePerformance(PerformanceRequest performanceRequest);
    List<PerformanceResponseDto> performanceUniqueComb(OeeFilterRequest request) throws Exception;
    public List<OeePerformanceEntity> performanceByDateRange(OeeFilterRequest request);


    //filters
    PerformanceByEfficiencyOfProduct getPerformanceByEfficiencyOfProduct(OeeFilterRequest request) throws Exception;
    PerformanceLossReasonsResponse getPerformanceLossReasons(OeeFilterRequest request) throws Exception;
}
