package com.rits.downtimeservice.service;

import com.rits.downtimeservice.dto.*;
import com.rits.downtimeservice.model.DownTimeAvailability;
import com.rits.downtimeservice.model.DownTimeMessageModel;
import com.rits.oeeservice.model.Oee;
import com.rits.performanceservice.dto.Combinations;

import java.util.List;

public interface DowntimeService {
    public DownTimeMessageModel logDownTimeandAvailability(DownTimeRequest downTimeRequest) throws Exception;

    public DownTimeMessageModel logDownTimeandAvailability() throws Exception;

    public DownTimeMessageModel logDownTimeandAvailabilitybyProductionLog(ProductionLogRequest productionLog) throws Exception;
    public DownTimeMessageModel logDownTimeandAvailabilitybyProductionLog(Object productionLog) throws Exception;
    public List<DownTimeAvailability> getUnproccessedRec();
    public DownTimeMessageModel updateRec(DownTimeRequest downTimeRequest);
    DownTimeMessageModel getAvailabilityForScheduler(String site, List<String> resourceList, List<Combinations> combinations) throws Exception;
    List<Oee> getAvailabilityForLiveData(String site,String resource) throws Exception;

    List<DowntimeResponse> getTotalDownTimeByReasonCodeInEachShift(String site, String resourceBO,String startDate, String endDate) throws Exception;

    DownTimeByShift getTotalDownTimeForCurrentShift(String site, String resource)throws Exception;

    List<DownTimeAvailability> getAllRecordsBetweenDateTime(String site,String resource) throws Exception;
}
