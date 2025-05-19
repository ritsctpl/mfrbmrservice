package com.rits.oeeservice.service;


import com.rits.availability.dto.AvailabilityByDownTimeResponse;
import com.rits.common.dto.OeeFilterRequest;
import com.rits.oeeservice.dto.*;
import com.rits.oeeservice.model.Oee;
import com.rits.performance.dto.PerformanceByDowntimeResponse;
import com.rits.performance.dto.PerformanceComparisonResponse;
import com.rits.quality.dto.ScrapAndReworkTrendResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public interface OeeService {
    public OeeCalculationResponse calculateOee(OeeRequestList requestList);

    List<Oee> executeQuery() throws Exception;

    /*OverallOeeResponse getOverallOee(OeeFilterRequest request) throws Exception;
    OeeByTimeResponse getByTime(OeeFilterRequest request) throws Exception;
    OeeByMachineResponse getOeeByMachine(OeeFilterRequest request) throws Exception;*/

    OeeByOrderResponse getOeeByOrder(OeeFilterRequest request) throws Exception;
   OeeByShiftResponse getOeeByShift(OeeFilterRequest request) throws Exception;
//    OeeLossByReasonResponse getOeeLossByReason(OeeFilterRequest request);
//    OeeByProductionLineResponse getOeeByProductionLine(OeeFilterRequest request) throws Exception;
//    OeeByOperatorResponse getOeeByOperator(OeeFilterRequest request);
//    OeeByProductResponse getOeeByProduct(OeeFilterRequest request);
    OeeByProductResponse retrieveByFilter(OeeFilterRequest request);
 Boolean calculateOEE(OeeFilterRequest request);
    Boolean calculate(OeeFilterRequest request);
    List<ShiftDetails> getOeeDetailsByShift(OeeRequest request);
    List<ResourceDetails> getOeeDetailsByResource(OeeRequest request);
    List<SiteDetails> getOeeDetailsBySite(OeeRequest request);
    List<WorkcenterDetails> getOeeDetailsByWorkcenter(OeeRequest request);

    List<BatchDetails> getOeeDetailsByBatch(OeeRequest request);


    // Method to get OEE details filtered by site and shift
    List<ShiftDetails> getOeeDetailsByShiftAndSite(OeeRequest request);

    // Method to get OEE details filtered by site, shift, and workcenter
    List<WorkcenterDetails> getOeeDetailsByWorkcenterAndShiftAndSite(OeeRequest request);

    // Method to get OEE details filtered by site, shift, workcenter, and resource
    List<ResourceDetails> getOeeDetailsByResourceAndWorkcenterAndShiftAndSite(OeeRequest request);

    // Method to get OEE details filtered by site, shift, workcenter, resource, and batch number
    List<BatchDetails> getOeeDetailsByBatchAndResourceAndWorkcenterAndShiftAndSite(OeeRequest request);

    List<SiteDetails> getAllOeeDetails(OeeRequest request);
    List<ShiftDetails> getOeeDetailsByShiftId(OeeRequest oeeRequest);

    List<WorkcenterDetails> getOeeDetailsByWorkCenterId(OeeRequest oeeRequest);

    List<ResourceDetails> getOeeDetailsByResourceId(OeeRequest oeeRequest);

    List<OperationDetails> getOeeDetailsByOperation(OeeRequest oeeRequest);

    List<BatchDetails> getOeeDetailsByBatchNo(OeeRequest oeeRequest);
    SiteDetails getOeeDetailBySite(OeeRequest request);
    List<ResourceDetails> calculateOeeByEvent(OeeRequest oeeRequest);
    List<Map<String, Object>> getOeeByShiftByType(OeeFilterRequest oeeRequest);

    // Method to get all OEE details, no filters applied


    //done by thiru - start

    OverallOeeResponse getOverall(OeeFilterRequest request);
    OeeByTimeResponse getByTime(OeeFilterRequest request) throws Exception;
    OeeByMachineResponse getByMachine(OeeFilterRequest request) throws Exception;
    OeeByProductionLineResponse getByProductionLine(OeeFilterRequest request) throws Exception;
    OeeByProductResponse getByProduct(OeeFilterRequest request) throws Exception;
   PerformanceComparisonResponse getPerformanceComparison(OeeFilterRequest request);

    LocalDateTime getEarliestValidShiftStartDateTime(ShiftRequest shiftReq);

    PerformanceByDowntimeResponse getPerformanceDowntime(OeeFilterRequest request);
    AvailabilityByDownTimeResponse getAvailabilityDowntime(OeeFilterRequest request);
    OeeByBreakdownResponse getOeeByBreakdown(OeeFilterRequest request) throws Exception;
    OeeByComponentResponse getOeeByComponent(OeeFilterRequest request) throws Exception;
    ScrapAndReworkTrendResponse getScrapAndReworkTrend(OeeFilterRequest request);
    List<SpeedLossSummaryDTO> getSpeedLossByResource(OeeFilterRequest request);
    List<SpeedLossSummaryDTO> getSpeedLossByWorkcenter(OeeFilterRequest request);

    MetricsResponse getOverallHistory(OeeRequest request) throws Exception;
    List<Map<String, Object>> getOverallResourceHistoryByType(OeeRequest request) throws Exception;
    List<Map<String, Object>> getShiftByResource(OeeRequest request) throws Exception;
    OeeByOperationResponse getByOperation(OeeFilterRequest request) throws Exception;

    List<Map<String, Object>> getByresourceTimeAndInterval(OeeRequest request) throws Exception;

    CompletableFuture<OperatorReportResponse> generateReport(OperatorReportRequest request);

    List<SiteDetails> getAllOeeDetailsV1(OeeRequest request);

    List<WorkcenterDetails> getOeeDetailsByWorkCenterIdV1(OeeRequest oeeRequest);
    List<Map<String, Object>> getOverallResourceHistoryV1(OeeRequest request) throws Exception;

    List<Map<String, Object>> getShiftByResourceV1(OeeRequest request) throws Exception;

    List<Map<String, Object>> getOverallHistoryV1(OeeRequest request) throws Exception;

    List<ResourceDetails> getOeeDetailsByMachineDataResourceId(OeeRequest request);

    boolean getAndLogMachinedataRecords(OeeRequest request);

    boolean getAndLogMachinedataRecordsBetween(String site, LocalDateTime startTime, LocalDateTime endTime);

    OverallOeeReportResponse getOverallOeeReport(String site);
    //end
}
