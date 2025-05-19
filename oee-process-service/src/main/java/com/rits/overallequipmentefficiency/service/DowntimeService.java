package com.rits.overallequipmentefficiency.service;



import com.rits.overallequipmentefficiency.dto.DowntimeRequest;
import com.rits.overallequipmentefficiency.dto.DowntimeSummary;
import com.rits.overallequipmentefficiency.model.ResourceStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface DowntimeService {

    void logDownTime(DowntimeRequest request);

    void updateUpdatedDateTime(String site, String resource, LocalDateTime startDateTime, LocalDateTime endDateTime);

    Long getBreakHoursBetweenTime(DowntimeRequest request);
    Long getPlannedBreakHoursBetweenTime(DowntimeRequest request);

    DowntimeSummary getBreakHoursBetweenTimeWithDetails(DowntimeRequest request);
    List<Map<String, Object>> getResourceStatusList(String site);

}
