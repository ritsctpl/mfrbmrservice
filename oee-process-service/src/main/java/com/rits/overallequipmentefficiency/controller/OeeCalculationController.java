package com.rits.overallequipmentefficiency.controller;




import com.rits.overallequipmentefficiency.dto.*;
import com.rits.overallequipmentefficiency.model.AggregatedTimePeriod;
import com.rits.overallequipmentefficiency.model.AvailabilityEntity;
import com.rits.overallequipmentefficiency.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.rits.overallequipmentefficiency.dto.AggregatedOeeRequestDTO;
import com.rits.overallequipmentefficiency.dto.AggregatedOeeResponseDTO;
import com.rits.overallequipmentefficiency.model.AggregatedOee;
import com.rits.overallequipmentefficiency.service.AggregationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/oee-calculation-service")
public class OeeCalculationController {
    private final AvailabilityService availabilityService;
    private final PerformanceService performanceService;
    private final QualityOeeService qualityOeeService;
    private final DowntimeService downtimeService;

    private final AggregatedAvailabilityService aggregatedAvailabilityService;
    @Autowired
    private AggregationService aggregationService;
    @Autowired
    private MachineLogService machineLogService;

    @PostMapping("/availability/logAvailabilityBetweenHours")
    public List<AvailabilityEntity> logAvailabilityBetweenHours(@RequestBody AvailabilityRequest request) {
              return availabilityService.logAvailabilityBetweenHours(request);
    }

    @PostMapping("/availability/getAvailabilityBetweenHours")
    public List<AvailabilityEntity> getAvailabilityBetweenHours(@RequestBody AvailabilityRequest request) {
        return  availabilityService.getAvailabilityBetweenHours(request);
    }

    @PostMapping("/availability/logAvailabiltyAndPublish")
    public List<PerformanceInput> logAvailabiltyAndPublish(@RequestBody AvailabilityRequest request) {
        return availabilityService.logAvailabiltyAndPublish(request);
    }

    @PostMapping("/availability/splitAndPublishLineAvailabilty")
    public ResponseEntity<List<AvailabilityRequest>> splitAndPublishLineAvailability(@RequestBody AvailabilityRequest request) {
        List<AvailabilityRequest> splitRequests = availabilityService.splitAndPublishLineAvailability(request);
        return ResponseEntity.ok(splitRequests);
    }

    @PostMapping("/availability/logLineAvailabilityAndPublish")
    public List<PerformanceInput> logLineAvailabilityAndPublish(@RequestBody AvailabilityRequest request) {
        return availabilityService.logLineAvailability(request);
    }

    @PostMapping("/publishLineAvailabilitybyTrackOeeWc")
    public ResponseEntity<String> publishLineAvailabilitybyTrackOeeWc(@RequestBody AvailabilityRequest request) {
        try {
            availabilityService.publishLineAvailabilityRequests(request);
            return ResponseEntity.ok("Line availability requests published successfully for site: " + request.getSite());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to publish line availability requests for site: " + request.getSite());
        }
    }

    @PostMapping("/performance/logPerformance")
    public List<PerformanceOutput> logPerformance(@RequestBody PerformanceInput inputs) {
        return performanceService.calculatePerformance(inputs);
    }

    @PostMapping("/qualityandoee/logQualityAndOee")
    public OeeOutput calculateQualityAndOee(@RequestBody PerformanceOutput performanceOutput) {
        OeeOutput oeeOutput = qualityOeeService.calculateQualityAndOee(performanceOutput);
        return oeeOutput;
    }


    @PostMapping("/downtime/logdowntime")
    public ResponseEntity<String> logDownTime(@RequestBody DowntimeRequest request) {
        downtimeService.logDownTime(request);
        return ResponseEntity.ok("Downtime successfully logged");
    }

    @PostMapping("/downtime/updatedowntimelog")
    public ResponseEntity<String> updateUpdatedDateTime(@RequestBody DowntimeRequest request) {
        downtimeService.updateUpdatedDateTime(request.getSite(), request.getResourceId(), request.getStartDateTime(), request.getEndDateTime());
        return ResponseEntity.ok("Downtime updated successfully");
    }

    @PostMapping("/downtime/logmachinelog")
    public ResponseEntity<String> logMachineLog(@RequestBody MachineLogRequest request) {
        machineLogService.logMachineLog(request);
        return ResponseEntity.ok("Machine log successfully created");
    }

    @PostMapping("/downtime/getBreakHoursBetweenTime")
    public ResponseEntity<Long> getBreakHoursBetweenTime(@RequestBody DowntimeRequest request) {
        long totalBreakHours = downtimeService.getBreakHoursBetweenTime(request);
        return ResponseEntity.ok(totalBreakHours);
    }
    @PostMapping("/downtime/getPlannedBreakHoursBetweenTime")
    public ResponseEntity<Long> getPlannedBreakHoursBetweenTime(@RequestBody DowntimeRequest request) {
        long totalBreakHours = downtimeService.getPlannedBreakHoursBetweenTime(request);
        return ResponseEntity.ok(totalBreakHours);
    }

    @PostMapping("/downtime/getBreakHoursBetweenTimeWithDetails")
    public ResponseEntity<DowntimeSummary> getBreakHoursBetweenTimeWithDetails(@RequestBody DowntimeRequest request) {
        DowntimeSummary downtimeSummary = downtimeService.getBreakHoursBetweenTimeWithDetails(request);
        return ResponseEntity.ok(downtimeSummary);
    }

  /*  @PostMapping("/aggregate")
    public ResponseEntity<Void> aggregateAvailability(@RequestBody AggregatedAvailabilityRequest request) {
        aggregatedAvailabilityService.aggregateAvailability(
                request.getSite(),
                request.getShiftId(),
                request.getShiftDate(),
                request.getResourceId(),
                request.getWorkcenterId()
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/getAggregatedAvailability")
    public ResponseEntity<AggregatedAvailabilityResponse> getAggregatedAvailability(@RequestBody AggregatedAvailabilityRequest request) {
        AggregatedAvailabilityResponse response = aggregatedAvailabilityService.getAggregatedAvailability(
                request.getSite(),
                request.getShiftId(),
                request.getResourceId(),
                request.getWorkcenterId()
        );
        return response != null ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }*/


   /* @PostMapping("/oee/aggregateOee")
    public ResponseEntity<AggregatedOeeResponseDTO> aggregateOee(@RequestBody AggregatedOeeRequestDTO request) {
        AggregatedOeeResponseDTO response = aggregationService.processAggregation(request.getOeeData());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/aggregate/byDate")
    public ResponseEntity<List<AggregatedOee>> getAggregatedOeeByDate(@RequestParam("date") String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        List<AggregatedOee> result = aggregationService.getAggregatedOeeByDate(date);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/aggregate/byShift")
    public ResponseEntity<List<AggregatedOee>> getAggregatedOeeByShift(@RequestParam("shiftId") String shiftId) {
        List<AggregatedOee> result = aggregationService.getAggregatedOeeByShift(shiftId);
        return ResponseEntity.ok(result);
    }*/

    /**
     * Receives an OeeOutput payload, aggregates the OEE values, and returns the AggregatedOee.
     *
     * @param oeeOutput the input payload containing availability, performance, quality, and OEE data.
     * @return the aggregated OEE data.
     */
    @PostMapping("/oee/aggregateOee")
    public ResponseEntity<AggregatedOee> aggregateOee(@RequestBody AggregatedOeeRequestDTO oeeOutput) {
        AggregatedOee aggregatedOee = aggregationService.aggregateOee(oeeOutput.getOeeData());
        return ResponseEntity.ok(aggregatedOee);
    }
    @PostMapping("/downtime/getCurruntStatus")
    public List<Map<String, Object>> getCurruntStatus(@RequestBody DowntimeRequest request) {
        // Return the list of resource statuses for the provided site
        return downtimeService.getResourceStatusList(request.getSite());
    }

    @PostMapping("/oee/aggregatedTimePeriod")
    public ResponseEntity<AggregatedTimePeriod> aggregatedTimePeriod(@RequestBody AggregatedTimePeriodInput input){
        AggregatedTimePeriod aggregatedTimePeriod = aggregationService.aggregatedTimePeriod(input);
        return ResponseEntity.ok(aggregatedTimePeriod);
    }
}
