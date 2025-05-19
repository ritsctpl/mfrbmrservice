package com.rits.shiftservice.service;

import com.rits.operationservice.dto.AuditLogRequest;
import com.rits.shiftservice.dto.*;
import com.rits.shiftservice.model.Break;
import com.rits.shiftservice.model.Shift;
import com.rits.shiftservice.model.ShiftMessageModel;
import com.rits.shiftservice.model.ShiftMongo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ShiftService {
    public ShiftMessageModel createShift(ShiftRequest shiftRequest) throws Exception;
    public ShiftMessageModel updateShift(ShiftRequest shiftRequest) throws Exception;
    public ShiftMessageModel deleteShift(ShiftRequest shiftRequest);
    public ShiftResponse retrieveShift(ShiftRequest shiftRequest);
    public ShiftResponseList retrieveTop50(String site);
    public ShiftResponseList retrieveAll(ShiftRequest shiftRequest);

    public String callExtension(Extension extension) throws Exception;
    AuditLogRequest createAuditLog(ShiftRequest shiftRequest);

    AuditLogRequest updateAuditLog(ShiftRequest shiftRequest);

    AuditLogRequest deleteAuditLog(ShiftRequest shiftRequest);
    public MinutesList getPlannedtime(String site);
    public MinutesList getPlannedtimeTillNow(String site);
    public BreakMinutesList getBreakHours(String site);
    public BreakMinutesList getBreakHoursTillNow(String site);
    public BreakMinutes getBreakHoursTillNowByType(String site, String shiftType, String resourceId, String workCenterId, LocalDateTime localDateTime);
    public PlannedMinutes getPlannedTimeTillNowByType(String site, String shiftType, String resourceId, String workCenterId);

    public List<ShiftIntervalWithDate> getShiftsWithDatesInRange(String site, String shiftType, String resourceId, String workCenterId, LocalDateTime dateStart, LocalDateTime dateEnd);

    List<ShiftIntervalWithDate> getShiftsWithDatesInRanges(String site, String shiftType, String resourceId, String workCenterId, LocalDateTime dateStart, LocalDateTime dateEnd);

    List<Shift> retrieveShiftByVersion(String site, String shiftId, String version);


    ShiftMongo getShiftByDates(LocalDateTime datetime);

    Long getShiftPlannedOperatingTime(String shiftId, String site);

    Long getNonProduction(LocalDateTime date);

    Long getBreakDuration(LocalDateTime downtimeEnd, LocalDateTime downtimeStart, List<String> shiftId,String site);
    List<Break> getBreakDurationList(LocalDateTime downtimeEnd, LocalDateTime downtimeStart, List<String> shiftIds, String site);

    List<AvailabilityPlannedOperatingTimeResponse> getAvailabilityPlannedOperatingTime(String site, List<String> shiftId, LocalDateTime startDateTime, LocalDateTime endDateTime, int dynamicBreak);

    ShiftDetailsDTO getCurrentShiftAndBreak(String site);

    BreakMinutes getShiftDetailsByShiftType(String site, String resourceId, String workcenterId);

    Long getShiftTypeBreakDuration(LocalDateTime downtimeEnd, LocalDateTime downtimeStart, String shiftId, String site);
    List<Shift> findShiftWithIntervalsAndBreaks(String shiftRef, String site, LocalDateTime startDate, LocalDateTime endDate);
    ShiftDataResponse calculateShiftData(String shiftRef, String site, LocalDateTime startDateTime, LocalDateTime endDateTime);
    List<ShiftOutput> getShifts(ShiftInput input);
    public Map<String, Long> getPlannedProductionTimes(List<String> shiftHandles);


}
