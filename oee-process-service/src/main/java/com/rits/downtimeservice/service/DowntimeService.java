package com.rits.downtimeservice.service;

import com.rits.availability.dto.AvailabilityRequestForDowntime;
import com.rits.common.dto.OeeFilterRequest;
import com.rits.downtimeservice.dto.*;
import com.rits.downtimeservice.model.Downtime;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public interface DowntimeService {
//    DowntimeMessageModel calculateTrigger(DowntimeRequest downtimeRequest)throws Exception;
//
//    DowntimeMessageModel calculateLive(DowntimeRequest downtimeRequest) throws Exception;

    Boolean logDowntime(DowntimeRequest downtimeRequest);

    DowntimeResponse updateDowntime(Long id, DowntimeRequest downtimeRequest);
    DowntimeBulkResponse bulkLogDowntime(List<DowntimeRequest> downtimeRequests);

    List<DowntimeResponseList> getDowntimeHistoryByResource(String resourceId);

    List<DowntimeResponseList> getDowntimeHistoryByWorkcenter(String workcenterId);

    List<DowntimeResponseList> getDowntimeHistoryByShift(String shiftId);

    DowntimeCloseResponse closeActiveDowntime(String resourceId, String workcenterId);
    List<DowntimeResponseList> getDowntimeHistoryByReason(String reason);
    List<DowntimeResponseList> getDowntimeHistoryByResourceAndDateRange(String resourceId, LocalDateTime startDate, LocalDateTime endDate);

    DowntimeResponse reopenDowntime(Long id);

    List<DowntimeResponseList> getDowntimeHistoryByRootCause(String rootCause);

    List<DowntimeResponseList> getDowntimeHistoryByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    Double getTotalDowntime(AvailabilityRequestForDowntime request);
    List<Downtime> getTotalDowntimeList(AvailabilityRequestForDowntime request);

    Long getDynamicBreakDuration(int dynamicBreak);
    List<Downtime> getDowntimeSummary(String site, List<String> resourceList,
                                      LocalDateTime startDateTime, LocalDateTime endDateTime,
                                      String workcenter, String shift);
    List<Downtime> getDowntime(String site, List<String> resourceList,
                                      LocalDateTime downtimeStart, LocalDateTime downtimeEnd);


    OverallDowntimeResponse getOverallDowntime(OeeFilterRequest request);
    DowntimeRetResponse getDowntimeOverTime(OeeFilterRequest request);
    DowntimeRetResponse getDowntimeByMachine(OeeFilterRequest request);
    DowntimeRetResponse getCumulativeDowntime(OeeFilterRequest request);


    // filters
    DowntimeRetResponse getDowntimeDurationDistribution(OeeFilterRequest request);
    DowntimeRetResponse getDowntimeAnalysis(OeeFilterRequest request);
    DowntimeRetResponse getDowntimeByReasonAndShift(OeeFilterRequest request);
    DowntimeRetResponse getDowntimeByReason(OeeFilterRequest request);
    DowntimeRetResponse getDowntimeVsProductionOutput(OeeFilterRequest request);
    DowntimeRetResponse getDowntimeImpact(OeeFilterRequest request);
     List<DowntTimeByWorkcenter> getDowntimeByWorkcenter(String site, List<String> workcenterList, LocalDateTime start, LocalDateTime end) ;

    List<DowntimeByResource> getDowntimeSummaryByResource(String site, List<String> workcenterList, List<String> resourceIds,
                                                          LocalDateTime intervalStart, LocalDateTime intervalEnd);
    List<DowntimeEventDTO> getDowntimeEvents(String site, List<String> resourceIds,
                                             LocalDateTime intervalStart, LocalDateTime intervalEnd);
    List<DowntimeReasonSummaryDTO> getDowntimeDurationByReason(String site,List<String> resourceIds,LocalDateTime intervalStart,LocalDateTime intervalEnd);

    Downtime getReasonForMachineDown(DowntimeRequest request);
    List<DowntimeForResource> getDowntimeWithResource(DowntimeRequest request);

}


