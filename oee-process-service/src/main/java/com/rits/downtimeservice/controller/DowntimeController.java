package com.rits.downtimeservice.controller;

import com.rits.availability.dto.AvailabilityRequestForDowntime;
import com.rits.common.dto.OeeFilterRequest;
import com.rits.downtimeservice.dto.*;
import com.rits.downtimeservice.exception.DowntimeException;
import com.rits.downtimeservice.model.Downtime;
import com.rits.downtimeservice.service.DowntimeService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/downtime-service")
public class DowntimeController {
    private final DowntimeService downtimeService;

    //    @PostMapping("/triggerCalculation")
//    @ResponseStatus(HttpStatus.CREATED)
//    public ResponseEntity<?> calculateTrigger(@RequestBody DowntimeRequest downtimeRequest) throws Exception {
//        DowntimeMessageModel messageModel;
//        if(downtimeRequest.getSite()!=null && !downtimeRequest.getSite().isEmpty()){
//            try {
//                messageModel = oeeProcessService.calculateTrigger(downtimeRequest);
//                return ResponseEntity.ok(messageModel);
//            }catch(DowntimeException oeeProcessException){
//                throw oeeProcessException;
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }else
//        throw new DowntimeException(1001, downtimeRequest.getSite());
//    }
//
//    @GetMapping("/calculateLive")
//    @ResponseStatus(HttpStatus.OK)
//    public DowntimeMessageModel calculateLive(@RequestBody DowntimeRequest downtimeRequest) throws Exception {
//        DowntimeMessageModel messageModel;
//        if(downtimeRequest.getSite()!=null && !downtimeRequest.getSite().isEmpty()){
//            try {
//                return messageModel = oeeProcessService.calculateLive(downtimeRequest);
//            }catch(DowntimeException oeeProcessException){
//                throw oeeProcessException;
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }else
//            throw new DowntimeException(1001, downtimeRequest.getSite());
//    }
    @PutMapping("/update/{id}")
    public ResponseEntity<DowntimeResponse> updateDowntime(@PathVariable("id") Long id, @RequestBody DowntimeRequest downtimeRequest) {
        DowntimeResponse response = downtimeService.updateDowntime(id, downtimeRequest);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/log")
    @ResponseStatus(HttpStatus.OK)
    public Boolean logDowntime(@RequestBody DowntimeRequest downtimeRequest) {
        if (downtimeRequest.getSite() != null && !downtimeRequest.getSite().isEmpty()) {
            try {
                Boolean response = downtimeService.logDowntime(downtimeRequest);
                return response;
            } catch (DowntimeException downtimeException) {
                throw downtimeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new DowntimeException(1001);
    }

    @PutMapping("/close-active")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<DowntimeCloseResponse> closeActiveDowntime(@RequestBody DowntimeCloseRequest downtimeCloseRequest) {
        if (downtimeCloseRequest.getSite() != null && !downtimeCloseRequest.getSite().isEmpty()) {
            try {
                DowntimeCloseResponse response = downtimeService.closeActiveDowntime(downtimeCloseRequest.getResourceId(), downtimeCloseRequest.getWorkcenterId());
                return ResponseEntity.ok(response);
            } catch (DowntimeException downtimeException) {
                throw downtimeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new DowntimeException(1001);
    }

    @PostMapping("/bulk-log")
    public ResponseEntity<DowntimeBulkResponse> bulkLogDowntime(@RequestBody List<DowntimeRequest> downtimeRequests) {
        DowntimeBulkResponse response = downtimeService.bulkLogDowntime(downtimeRequests);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/history/resourceId")
    public ResponseEntity<List<DowntimeResponseList>> getDowntimeHistoryByResource(@RequestBody DowntimeRequest downtimeRequest) {
        List<DowntimeResponseList> retrieveByResource;
        if (downtimeRequest.getSite() != null && !downtimeRequest.getSite().isEmpty()) {
            try {
                retrieveByResource = downtimeService.getDowntimeHistoryByResource(downtimeRequest.getResourceId());
                return ResponseEntity.ok(retrieveByResource);
            } catch (DowntimeException downtimeException) {
                throw downtimeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new DowntimeException(1001);
    }


    @GetMapping("/history/workcenterId")
    public ResponseEntity<List<DowntimeResponseList>> getDowntimeHistoryByWorkcenter(@RequestBody DowntimeRequest downtimeRequest) {
        List<DowntimeResponseList> retrieveByWorkcenter;
        if (downtimeRequest.getSite() != null && !downtimeRequest.getSite().isEmpty()) {
            try {
                retrieveByWorkcenter = downtimeService.getDowntimeHistoryByWorkcenter(downtimeRequest.getWorkcenterId());
                return ResponseEntity.ok(retrieveByWorkcenter);
            } catch (DowntimeException downtimeException) {
                throw downtimeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new DowntimeException(1001);
    }

    @PutMapping("/reopen/{id}")
    public ResponseEntity<DowntimeResponse> reopenDowntime(@PathVariable("id") Long id) {
        DowntimeResponse response = downtimeService.reopenDowntime(id);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }


    @GetMapping("/history/root-cause")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<DowntimeResponseList>> getDowntimeHistoryByRootCause(@RequestBody DowntimeRequest downtimeRequest) {
        if (downtimeRequest.getSite() != null && !downtimeRequest.getSite().isEmpty()) {
            try {
                List<DowntimeResponseList> response = downtimeService.getDowntimeHistoryByRootCause(downtimeRequest.getRootCause());
                return ResponseEntity.ok(response);
            } catch (DowntimeException downtimeException) {
                throw downtimeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new DowntimeException(1001);
    }

    @GetMapping("/history/shiftId")
    public ResponseEntity<List<DowntimeResponseList>> getDowntimeHistoryByShift(@RequestBody DowntimeRequest downtimeRequest) {
        List<DowntimeResponseList> retrieveByShift;
        if (downtimeRequest.getSite() != null && !downtimeRequest.getSite().isEmpty()) {
            try {
                retrieveByShift = downtimeService.getDowntimeHistoryByShift(downtimeRequest.getShiftId());
                return ResponseEntity.ok(retrieveByShift);
            } catch (DowntimeException downtimeException) {
                throw downtimeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new DowntimeException(1001);
    }


    @GetMapping("/history/reason")
    public ResponseEntity<List<DowntimeResponseList>> getDowntimeHistoryByReason(@RequestBody DowntimeRequest downtimeRequest) {
        List<DowntimeResponseList> retrieveByReason;
        if (downtimeRequest.getSite() != null && !downtimeRequest.getSite().isEmpty()) {
            try {
                retrieveByReason = downtimeService.getDowntimeHistoryByReason(downtimeRequest.getReason());
                return ResponseEntity.ok(retrieveByReason);
            } catch (DowntimeException downtimeException) {
                throw downtimeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new DowntimeException(1001);
    }


    @GetMapping("/history/resource/date-range")
    public ResponseEntity<List<DowntimeResponseList>> getDowntimeHistoryByResourceAndDateRange(@RequestBody Map<String, Object> requestBody) {
        if (!requestBody.containsKey("resourceId") || !requestBody.containsKey("startDate") || !requestBody.containsKey("endDate")) {
            throw new DowntimeException(1105);
        }
        String resourceId = (String) requestBody.get("resourceId");
        LocalDateTime startDate = LocalDateTime.parse((String) requestBody.get("startDate"));
        LocalDateTime endDate = LocalDateTime.parse((String) requestBody.get("endDate"));
        List<DowntimeResponseList> response = downtimeService.getDowntimeHistoryByResourceAndDateRange(resourceId, startDate, endDate);

        if (response.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(response);
        }
    }


    @GetMapping("/history/date-range")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<DowntimeResponseList>> getDowntimeHistoryByDateRange(@RequestBody Map<String, Object> requestBody) {

        if (requestBody.containsKey("startDate") && requestBody.containsKey("endDate")) {
            try {
                LocalDateTime startDate = LocalDateTime.parse((String) requestBody.get("startDate"));
                LocalDateTime endDate = LocalDateTime.parse((String) requestBody.get("endDate"));

                List<DowntimeResponseList> response = downtimeService.getDowntimeHistoryByDateRange(startDate, endDate);
                return ResponseEntity.ok(response);
            } catch (DowntimeException downtimeException) {
                throw downtimeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new DowntimeException(1104);
    }

    @PostMapping("/totalDowntime")
    public Double getTotalDowntime(@RequestBody AvailabilityRequestForDowntime request) {
        if (request.getSite() != null || !request.getSite().isEmpty()) {
            try {
                return downtimeService.getTotalDowntime(request);
            } catch (DowntimeException downtimeException) {
                throw downtimeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new DowntimeException(1001);
    }

    @PostMapping("/totalDownTimeList")
    public List<Downtime> getTotalDowntimeList(@RequestBody AvailabilityRequestForDowntime request) {
        if (request.getSite() != null || !request.getSite().isEmpty()) {
            try {
                return downtimeService.getTotalDowntimeList(request);
            } catch (DowntimeException downtimeException) {
                throw downtimeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new DowntimeException(1001);
    }


    @PostMapping("/dynamicBreakDuration")
    public Long getDynamicBreakDuration(@RequestBody AvailabilityRequestForDowntime request) {
        try {
            return downtimeService.getDynamicBreakDuration(request.getDynamicBreak());
        } catch (DowntimeException downtimeException) {
            throw downtimeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getDowntimeSummary")
    public List<Downtime> getDowntimeSummary(@RequestBody DowntimeRequest request) {
        try {
            return downtimeService.getDowntimeSummary(request.getSite(), request.getResourceList(), request.getStartDateTime(), request.getEndDateTime(), request.getWorkcenterId(), request.getShiftId());
        } catch (DowntimeException downtimeException) {
            throw downtimeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("/overallDowntime")
    public OverallDowntimeResponse getOverallDowntime(@RequestBody OeeFilterRequest request) {
        return downtimeService.getOverallDowntime(request);
    }

    @PostMapping("/downtimeOverTime")
    public DowntimeRetResponse getDowntimeOverTime(@RequestBody OeeFilterRequest request) {
        return downtimeService.getDowntimeOverTime(request);
    }

    @PostMapping("/downtimeByReason")
    public DowntimeRetResponse getDowntimeByReason(@RequestBody OeeFilterRequest request) {
        return downtimeService.getDowntimeByReason(request);
    }

    @PostMapping("/downtimeByMachine")
    public DowntimeRetResponse getDowntimeByMachine(@RequestBody OeeFilterRequest request) {
        return downtimeService.getDowntimeByMachine(request);
    }

    @PostMapping("/cumulativeDowntime")
    public DowntimeRetResponse getCumulativeDowntime(@RequestBody OeeFilterRequest request) {
        return downtimeService.getCumulativeDowntime(request);
    }

    @PostMapping("/downtimeVsProductionOutput")
    public DowntimeRetResponse getDowntimeVsProductionOutput(@RequestBody OeeFilterRequest request) {
        return downtimeService.getDowntimeVsProductionOutput(request);
    }

    @PostMapping("/downtimeAnalysis")
    public DowntimeRetResponse getDowntimeAnalysis(@RequestBody OeeFilterRequest request) {
        return downtimeService.getDowntimeAnalysis(request);
    }

    @PostMapping("/downtimeImpact")
    public DowntimeRetResponse getDowntimeImpact(@RequestBody OeeFilterRequest request) {
        return downtimeService.getDowntimeImpact(request);
    }

    @PostMapping("/downtimeDurationDistribution")
    public DowntimeRetResponse getDowntimeDurationDistribution(@RequestBody OeeFilterRequest request) {
        return downtimeService.getDowntimeDurationDistribution(request);
    }

    @PostMapping("/downtimeByReasonAndShift")
    public DowntimeRetResponse getDowntimeByReasonAndShift(@RequestBody OeeFilterRequest request) {
        return downtimeService.getDowntimeByReasonAndShift(request);
    }

    @PostMapping("/downtimeByWorkCenter")
    public DowntimeRetResponse getDowntimeByWorkCenter(@RequestBody OeeFilterRequest request) {
        return downtimeService.getDowntimeByReasonAndShift(request);
    }
    @PostMapping("/getDowntimeByWorkcenter")
    public ResponseEntity<List<DowntTimeByWorkcenter>> getDowntimeByWorkcenter(@RequestBody DowntimeRequest request) {
        // Validate that site is provided
        if (request.getSite() == null || request.getSite().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = request.getIntervalStart();
        LocalDateTime end = request.getIntervalEnd();

        if (start == null || end == null) {
            end = now;
            start = now.minusHours(24);
        }

        List<DowntTimeByWorkcenter> summary = downtimeService.getDowntimeByWorkcenter(
                request.getSite(),
                request.getWorkcenterList(),
                start,
                end);

        return ResponseEntity.ok(summary);
    }
    @PostMapping("/getDowntimeByResource")
    public ResponseEntity<List<DowntimeByResource>> getDowntimeByResource(@RequestBody DowntimeRequest request) {
        if (request.getSite() == null || request.getSite().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<DowntimeByResource> summary = downtimeService.getDowntimeSummaryByResource(
                request.getSite(),
                request.getWorkcenterList(),
                request.getResourceIds(),
                request.getIntervalStart(),
                request.getIntervalEnd());
        return ResponseEntity.ok(summary);
    }
    @PostMapping("/getDowntimeByResourceAndInterval")
    public ResponseEntity<List<DowntimeForResource>> getDowntimeEvents(@RequestBody DowntimeRequest request) {
        if (request.getSite() == null || request.getSite().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<DowntimeForResource> events = downtimeService.getDowntimeWithResource(request);
        return ResponseEntity.ok(events);
    }
    @PostMapping("/getDowntimeByReason")
    public ResponseEntity<List<DowntimeReasonSummaryDTO>> getDowntimeByReason(@RequestBody DowntimeRequest request) {
        if (request.getSite() == null || request.getSite().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<DowntimeReasonSummaryDTO> summary = downtimeService.getDowntimeDurationByReason(
                request.getSite(),
                request.getResourceIds(), // List<String> of resource IDs
                request.getIntervalStart(),
                request.getIntervalEnd());
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/getReasonForMachineDown")
    public Downtime getReasonForMachineDown(@RequestBody DowntimeRequest request) {
        if (request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return downtimeService.getReasonForMachineDown(request);
            } catch (DowntimeException downtimeException) {
                throw downtimeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new DowntimeException(1001);
    }

    @PostMapping("/getDowntime")
    public List<Downtime> getDowntime(@RequestBody DowntimeRequest request) {
        try {
            return downtimeService.getDowntime(request.getSite(), request.getResourceList(), request.getDowntimeStart(), request.getDowntimeEnd());
        } catch (DowntimeException downtimeException) {
            throw downtimeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}





