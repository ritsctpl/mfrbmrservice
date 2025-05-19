package com.rits.performanceservice.service;

import com.rits.downtimeservice.model.DownTimeAvailability;
import com.rits.oeeservice.model.Oee;
import com.rits.performanceservice.dto.DownTime;
import com.rits.performanceservice.dto.PerformanceRequestList;
import com.rits.performanceservice.model.Performance;

import java.util.List;

public interface PerformanceService {
    public PerformanceRequestList  calculatePerformance(PerformanceRequestList  performanceRequestList);

    PerformanceRequestList getUnProcessedRecord(PerformanceRequestList performanceRequestList);
    PerformanceRequestList makeProcessedAsTrue(List<Performance> performanceList);

    DownTime downTimeAvailabilityToDownTimeBuilder(DownTimeAvailability downTimeAvailability);
    List<Oee>  calculatePerformanceForLiveData(PerformanceRequestList  performanceRequestList) throws Exception;

}
