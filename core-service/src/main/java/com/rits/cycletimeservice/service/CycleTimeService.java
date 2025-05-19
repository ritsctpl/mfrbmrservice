package com.rits.cycletimeservice.service;

import com.rits.cycletimeservice.dto.*;
import com.rits.cycletimeservice.model.AttachmentPriority;
import com.rits.cycletimeservice.model.CycleTime;
import com.rits.cycletimeservice.model.CycleTimeMessageModel;
import com.rits.cycletimeservice.model.CycleTimePostgres;

import java.util.List;

public interface CycleTimeService {
    CycleTimeMessageModel createOrUpdate(CycleTimeRequest cycleTimeRequest) throws Exception;

    CycleTimeMessageModel delete(CycleTimeRequest cycleTimeRequest)throws Exception;

    CycleTimeResponse retrieve(CycleTimeRequest cycleTimeRequest) throws Exception;

    CycleTimeResponseList retrieveAll(String site) throws Exception;


    CycleTimeResponseList retrieveByCriteria(CycleTimeRequest cycleTimeRequest)throws Exception;

    Boolean createPriorityCombinations(AttachmentPriority attachmentPriority)throws Exception;

   // CycleTime retrieveByAttachment(CycleTimeRequest cycleTimeRequest)throws Exception;

    List<String> generateCombinations(List<String> elements);

    void generateCombinationsHelper(List<String> elements, String prefix, int startIndex, List<String> result);

    List<CycleTimeDto> getCalculatedPerformance(PerformanceRequest request);

    Double getPlannedCycleTime(ProductionLogDto request);
    List<CycleTimePostgres> getCycleTimeRecords(CycleTimeRequest cycleTimeRequest);
    List<CycleTime> getActiveCycleTimesBySiteAndResource(String site, String resourceId);
    List<CycleTime> getActiveCycleTimesBySiteAndWorkcenter(String site, String workcenterId);

    public List<CycleTime> getFilteredCycleTimes(CycleTimeReq cycleTimeReq);

    List<CycleTime> getFilteredCycleTimesByWorkCenter(CycleTimeReq cycleTimeReq);

    Double getCycleTimeValue(ProductionLogDto dto);
}
