package com.rits.oeeservice.controller;

import com.rits.availability.dto.AvailabilityByDownTimeResponse;
import com.rits.common.dto.OeeFilterRequest;
import com.rits.oeeservice.dto.*;
import com.rits.oeeservice.exception.OeeException;
import com.rits.oeeservice.model.Oee;
import com.rits.oeeservice.service.OeeService;
import com.rits.overallequipmentefficiency.dto.OeeProductionLogRequest;
import com.rits.performance.dto.PerformanceByDowntimeResponse;
import com.rits.performance.dto.PerformanceComparisonResponse;
import com.rits.quality.dto.ScrapAndReworkTrendResponse;
import com.rits.quality.exception.QualityException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/app/v1/oee-service")
public class OeeController {

    @Autowired
    private OeeService oeeService;

    // -------------------- Live OEE --------------------

    @PostMapping("/liveoee")
    public ResponseEntity<OeeCalculationResponse> calculateOee(@RequestBody OeeRequestList requestList) {
        OeeCalculationResponse response = oeeService.calculateOee(requestList);
        if (response.getOeeResponses().isEmpty()) {
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // -------------------- Basic OEE Endpoints --------------------

    @PostMapping("/oee")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<Oee>> getOee() throws Exception {
        try {
            List<Oee> oee = oeeService.executeQuery();
            return ResponseEntity.ok(oee);
        } catch (OeeException oeeProcessException) {
            throw oeeProcessException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/calculateOEE")
    @ResponseStatus(HttpStatus.OK)
    public Boolean getOee(@RequestBody OeeFilterRequest request) throws Exception {
        try {
            return oeeService.calculateOEE(request);
        } catch (OeeException oeeProcessException) {
            throw oeeProcessException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // -------------------- OEE by Various Criteria --------------------

    /* Uncomment if needed
    @PostMapping("/getOverallOEE")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<OverallOeeResponse> getOverallOee(@RequestBody OeeFilterRequest request) {
        if (request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                OverallOeeResponse oeeResponse = oeeService.getOverallOee(request);
                return ResponseEntity.ok(oeeResponse);
            } catch (OeeException oeeException) {
                throw oeeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new OeeException(1001);
    }

    @PostMapping("/getByTime")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<OeeByTimeResponse> getByTime(@RequestBody OeeFilterRequest request) {
        if (request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                OeeByTimeResponse oeeResponse = oeeService.getByTime(request);
                return ResponseEntity.ok(oeeResponse);
            } catch (OeeException oeeException) {
                throw oeeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new OeeException(1001);
    }

    @PostMapping("/getOEEByMachine")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<OeeByMachineResponse> getOeeByMachine(@RequestBody OeeFilterRequest request) {
        if (request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                OeeByMachineResponse oeeResponse = oeeService.getOeeByMachine(request);
                return ResponseEntity.ok(oeeResponse);
            } catch (OeeException oeeException) {
                throw oeeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new OeeException(1001);
    }
    */

    @PostMapping("/getOEEByShift")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<OeeByShiftResponse> getOeeByShift(@RequestBody OeeFilterRequest request) {
        if (request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                OeeByShiftResponse oeeResponse = oeeService.getOeeByShift(request);
                return ResponseEntity.ok(oeeResponse);
            } catch (OeeException oeeException) {
                throw oeeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new OeeException(1001);
    }

    @PostMapping("/getOEEBreakdown")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<OeeByBreakdownResponse> getOeeByBreakdown(@RequestBody OeeFilterRequest request) {
        if (request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                OeeByBreakdownResponse oeeResponse = oeeService.getOeeByBreakdown(request);
                return ResponseEntity.ok(oeeResponse);
            } catch (OeeException oeeException) {
                throw oeeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new OeeException(1001);
    }

    // @PostMapping("/getOEEByProductionLine")
    // @ResponseStatus(HttpStatus.OK)
    // public ResponseEntity<OeeByProductionLineResponse> getOeeByProductionLine(@RequestBody OeeFilterRequest request) {
    //     if (request.getSite() != null && !request.getSite().isEmpty()) {
    //         try {
    //             OeeByProductionLineResponse oeeResponse = oeeService.getOeeByProductionLine(request);
    //             return ResponseEntity.ok(oeeResponse);
    //         } catch (OeeException oeeException) {
    //             throw oeeException;
    //         } catch (Exception e) {
    //             throw new RuntimeException(e);
    //         }
    //     }
    //     throw new OeeException(1001);
    // }

    @PostMapping("/getOEEByComponent")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<OeeByComponentResponse> getOeeByComponent(@RequestBody OeeFilterRequest request) {
        if (request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                OeeByComponentResponse oeeResponse = oeeService.getOeeByComponent(request);
                return ResponseEntity.ok(oeeResponse);
            } catch (OeeException oeeException) {
                throw oeeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new OeeException(1001);
    }

    @PostMapping("/getOEEByOrder")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<OeeByOrderResponse> getOeeByOrder(@RequestBody OeeFilterRequest request) {
        if (request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                OeeByOrderResponse oeeResponse = oeeService.getOeeByOrder(request);
                return ResponseEntity.ok(oeeResponse);
            } catch (OeeException oeeException) {
                throw oeeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new OeeException(1001);
    }

    @PostMapping("/calculate")
    @ResponseStatus(HttpStatus.OK)
    public Boolean calculate(@RequestBody OeeFilterRequest request) {
        if (request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return oeeService.calculate(request);
            } catch (OeeException oeeException) {
                throw oeeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new OeeException(1001);
    }

    // @PostMapping("/getOEEByProduct")
    // @ResponseStatus(HttpStatus.OK)
    // public ResponseEntity<OeeByProductResponse> getOeeByProduct(@RequestBody OeeFilterRequest request) {
    //     if (request.getSite() != null && !request.getSite().isEmpty()) {
    //         try {
    //             OeeByProductResponse oeeResponse = oeeService.getOeeByProduct(request);
    //             return ResponseEntity.ok(oeeResponse);
    //         } catch (OeeException oeeException) {
    //             throw oeeException;
    //         } catch (Exception e) {
    //             throw new RuntimeException(e);
    //         }
    //     }
    //     throw new OeeException(1001);
    // }

    @PostMapping("/retrieveByFilter")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<OeeByProductResponse> retrieveByFilter(@RequestBody OeeFilterRequest request) {
        if (StringUtils.isBlank(request.getSite())) {
            throw new OeeException(1001);
        }
        try {
            OeeByProductResponse oeeResponse = oeeService.retrieveByFilter(request);
            return ResponseEntity.ok(oeeResponse);
        } catch (OeeException oeeException) {
            throw oeeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // -------------------- Detailed OEE Endpoints --------------------

    @PostMapping("/getOeeDetailByShift")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ShiftDetails>> getOeeDetailByShift(@RequestBody OeeRequest request) {
        // Set default times if not provided (last 24 hours)
        if (request.getStartTime() == null || request.getEndTime() == null) {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(24);
            request.setStartTime(String.valueOf(startTime));
            request.setEndTime(String.valueOf(endTime));
        }
        if (StringUtils.isBlank(request.getSite())) {
            throw new OeeException(1001);
        }
        try {
            List<ShiftDetails> oeeDetailByShift = oeeService.getOeeDetailsByShiftId(request);
            return ResponseEntity.ok(oeeDetailByShift);
        } catch (OeeException oeeException) {
            throw oeeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getOeeDetailsByWorkCenterId")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<WorkcenterDetails>> getOeeDetailsByWorkCenterId(@RequestBody OeeRequest request) {
        // Set default times if not provided (last 24 hours)
        if (request.getStartTime() == null || request.getEndTime() == null) {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(24);
            request.setStartTime(String.valueOf(startTime));
            request.setEndTime(String.valueOf(endTime));
        }
        if (StringUtils.isBlank(request.getSite())) {
            throw new OeeException(1001);
        }
        try {
            List<WorkcenterDetails> workcenterDetails = oeeService.getOeeDetailsByWorkCenterId(request);
            return ResponseEntity.ok(workcenterDetails);
        } catch (OeeException oeeException) {
            throw oeeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getOeeDetailsByResourceId")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ResourceDetails>> getOeeDetailsByResourceId(@RequestBody OeeRequest request) {
        // Set default times if not provided (last 24 hours)
        if (request.getStartTime() == null || request.getEndTime() == null) {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(24);
            request.setStartTime(String.valueOf(startTime));
            request.setEndTime(String.valueOf(endTime));
            System.out.println("Start Time: " + startTime);
            System.out.println("End Time: " + endTime);

        }
        if (StringUtils.isBlank(request.getSite())) {
            throw new OeeException(1001);
        }
        try {
            List<ResourceDetails> resourceDetails = oeeService.getOeeDetailsByResourceId(request);
            return ResponseEntity.ok(resourceDetails);
        } catch (OeeException oeeException) {
            throw oeeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getOeeDetailsByMachineDataResourceId")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ResourceDetails>> getOeeDetailsByMachineDataResourceId(@RequestBody OeeRequest request) {
        // Set default times if not provided (last 24 hours)
        if (request.getStartTime() == null || request.getEndTime() == null) {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(24);
            request.setStartTime(String.valueOf(startTime));
            request.setEndTime(String.valueOf(endTime));
            System.out.println("Start Time: " + startTime);
            System.out.println("End Time: " + endTime);

        }
        if (StringUtils.isBlank(request.getSite())) {
            throw new OeeException(1001);
        }
        try {
            List<ResourceDetails> resourceDetails = oeeService.getOeeDetailsByMachineDataResourceId(request);
            return ResponseEntity.ok(resourceDetails);
        } catch (OeeException oeeException) {
            throw oeeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getOeeDetailsByOperation")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<OperationDetails>> getOeeDetailsByOperation(@RequestBody OeeRequest request) {
        if (request.getStartTime() == null || request.getEndTime() == null) {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(24);
            request.setStartTime(String.valueOf(startTime));
            request.setEndTime(String.valueOf(endTime));
            System.out.println("Start Time: " + startTime);
            System.out.println("End Time: " + endTime);

        }
        if (StringUtils.isBlank(request.getSite())) {
            throw new OeeException(1001);
        }
        try {
            List<OperationDetails> operationDetails = oeeService.getOeeDetailsByOperation(request);
            return ResponseEntity.ok(operationDetails);
        } catch (OeeException oeeException) {
            throw oeeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getOeeDetailsByBatchNo")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<BatchDetails>> getOeeDetailsByBatchNo(@RequestBody OeeRequest request) {
        // Set default times if not provided (last 24 hours)
        if (request.getStartTime() == null || request.getEndTime() == null) {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(24);
            request.setStartTime(String.valueOf(startTime));
            request.setEndTime(String.valueOf(endTime));
        }
        if (StringUtils.isBlank(request.getSite())) {
            throw new OeeException(1001);
        }
        try {
            List<BatchDetails> batchDetails = oeeService.getOeeDetailsByBatchNo(request);
            return ResponseEntity.ok(batchDetails);
        } catch (OeeException oeeException) {
            throw oeeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getOeeDetailsByPlant")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<SiteDetails>> getOeeDetailsByPlant(@RequestBody OeeRequest request) {
        // Set default times if not provided (last 24 hours)
        if (request.getStartTime() == null || request.getEndTime() == null) {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(24);
            request.setStartTime(String.valueOf(startTime));
            request.setEndTime(String.valueOf(endTime));
        }
        if (StringUtils.isBlank(request.getSite())) {
            throw new OeeException(1001);
        }
        try {
            List<SiteDetails> siteDetailsList = new ArrayList<>();
            if (request.getSite() == null) {
                siteDetailsList = oeeService.getAllOeeDetails(request);
            } else {
                SiteDetails siteDetails = oeeService.getOeeDetailBySite(request);
                siteDetailsList.add(siteDetails);
            }
            return ResponseEntity.ok(siteDetailsList);
        } catch (OeeException oeeException) {
            throw oeeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/OEEdetails")
    public Object getOeeDetails(@RequestBody OeeRequest request) {
        // Set default times if not provided (last 24 hours)
        if (request.getStartTime() == null || request.getEndTime() == null) {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(24);
            request.setStartTime(String.valueOf(startTime));
            request.setEndTime(String.valueOf(endTime));
        }
        System.out.println("Start Time: " + request.getStartTime());
        System.out.println("End Time: " + request.getEndTime());

        if (request.getSite() != null) {
            return oeeService.getOeeDetailsBySite(request);
        } else if (request.getShiftId() != null) {
            return oeeService.getOeeDetailsByShift(request);
        } else if (request.getWorkcenterId() != null) {
            return oeeService.getOeeDetailsByWorkcenter(request);
        } else if (request.getResourceId() != null) {
            return oeeService.getOeeDetailsByResource(request);
        } else if (request.getBatchno() != null) {
            return oeeService.getOeeDetailsByBatch(request);
        } else {
            return oeeService.getAllOeeDetails(request);
        }
    }

    /*@PostMapping("/OEEdetailsByFilter")
    public Object getOeeDetailsByFilter(@RequestBody OeeRequest request) {
        try {
            if (request.getStartTime() == null || request.getEndTime() == null) {
                LocalDateTime endTime = LocalDateTime.now();
                LocalDateTime startTime = endTime.minusHours(24);
                request.setStartTime(String.valueOf(startTime));
                request.setEndTime(String.valueOf(endTime));
            }
            System.out.println("Start Time: " + request.getStartTime());
            System.out.println("End Time: " + request.getEndTime());

            if (request.getSite() != null && request.getShiftId() == null && request.getWorkcenterId() == null
                    && request.getResourceId() == null && request.getBatchno() == null) {
                return oeeService.getOeeDetailBySite(request);
            } else if (request.getSite() != null && request.getShiftId() != null
                    && request.getWorkcenterId() == null && request.getResourceId() == null && request.getBatchno() == null) {
                return oeeService.getOeeDetailsByShiftAndSite(request);
            } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null
                    && request.getResourceId() == null && request.getBatchno() == null) {
                return oeeService.getOeeDetailsByWorkcenterAndShiftAndSite(request);
            } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null
                    && request.getResourceId() != null && request.getBatchno() == null) {
                return oeeService.getOeeDetailsByResourceAndWorkcenterAndShiftAndSite(request);
            } else if (request.getSite() != null && request.getShiftId() != null && request.getWorkcenterId() != null
                    && request.getResourceId() != null && request.getBatchno() != null) {
                return oeeService.getOeeDetailsByBatchAndResourceAndWorkcenterAndShiftAndSite(request);
            } else {
                return oeeService.getAllOeeDetails(request);
            }
        } catch (OeeException oeeException) {
            throw oeeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/

    @PostMapping("/calculateOeeByEventType")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ResourceDetails>> calculateOeeByEventType(@RequestBody OeeRequest request) {
        if (StringUtils.isBlank(request.getSite())) {
            throw new OeeException(1001);
        }
        try {
            List<ResourceDetails> resourceDetails = oeeService.calculateOeeByEvent(request);
            return ResponseEntity.ok(resourceDetails);
        } catch (OeeException oeeException) {
            throw oeeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getOeeByShiftByType")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<Map<String, Object>>> getOeeByShiftByType(@RequestBody OeeFilterRequest request) {
        if (StringUtils.isBlank(request.getSite())) {
            throw new OeeException(1001);
        }
        try {
            List<Map<String, Object>> resourceDetails = oeeService.getOeeByShiftByType(request);
            return ResponseEntity.ok(resourceDetails);
        } catch (OeeException oeeException) {
            throw oeeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // -------------------- Endpoints by Thiru --------------------

    @PostMapping("/getOverall")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<OverallOeeResponse> getOverall(@RequestBody OeeFilterRequest request) {
        if (request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                OverallOeeResponse oeeResponse = oeeService.getOverall(request);
                return ResponseEntity.ok(oeeResponse);
            } catch (OeeException oeeException) {
                throw oeeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new OeeException(1001);
    }

    @PostMapping("/getByTime")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<OeeByTimeResponse> getByTime(@RequestBody OeeFilterRequest request) {
        if (request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                OeeByTimeResponse oeeResponse = oeeService.getByTime(request);
                return ResponseEntity.ok(oeeResponse);
            } catch (OeeException oeeException) {
                throw oeeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new OeeException(1001);
    }

    @PostMapping("/getByMachine")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<OeeByMachineResponse> getByMachine(@RequestBody OeeFilterRequest request) {
        if (request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                OeeByMachineResponse oeeResponse = oeeService.getByMachine(request);
                return ResponseEntity.ok(oeeResponse);
            } catch (OeeException oeeException) {
                throw oeeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new OeeException(1001);
    }

    @PostMapping("/getByProductionLine")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<OeeByProductionLineResponse> getByProductionLine(@RequestBody OeeFilterRequest request) {
        if (request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                OeeByProductionLineResponse oeeResponse = oeeService.getByProductionLine(request);
                return ResponseEntity.ok(oeeResponse);
            } catch (OeeException oeeException) {
                throw oeeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new OeeException(1001);
    }

    // -------- Endpoints by Kartheek Nasina --------

    @PostMapping("/getByProduct")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<OeeByProductResponse> getByProduct(@RequestBody OeeFilterRequest request) {
        if (request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                OeeByProductResponse oeeResponse = oeeService.getByProduct(request);
                return ResponseEntity.ok(oeeResponse);
            } catch (OeeException oeeException) {
                throw oeeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new OeeException(1001);
    }

    @PostMapping("/performanceComparison")
    public ResponseEntity<PerformanceComparisonResponse> getPerformanceComparison(@RequestBody OeeFilterRequest request) {
        if (request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                PerformanceComparisonResponse oeeResponse = oeeService.getPerformanceComparison(request);
                return ResponseEntity.ok(oeeResponse);
            } catch (OeeException oeeException) {
                throw oeeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new OeeException(1001);
    }

    @PostMapping("/getPerformanceDowntime")
    public ResponseEntity<PerformanceByDowntimeResponse> getPerformanceDowntime(@RequestBody OeeFilterRequest request) {
        if (request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                PerformanceByDowntimeResponse oeeResponse = oeeService.getPerformanceDowntime(request);
                return ResponseEntity.ok(oeeResponse);
            } catch (OeeException oeeException) {
                throw oeeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new OeeException(1001);
    }

    @PostMapping("/getAvailabilityDowntime")
    public ResponseEntity<AvailabilityByDownTimeResponse> getAvailabilityDowntime(@RequestBody OeeFilterRequest request) {
        if (request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                AvailabilityByDownTimeResponse oeeResponse = oeeService.getAvailabilityDowntime(request);
                return ResponseEntity.ok(oeeResponse);
            } catch (OeeException oeeException) {
                throw oeeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new OeeException(1001);
    }

    @PostMapping("/getScrapAndReworkTrend")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ScrapAndReworkTrendResponse> getScrapAndReworkTrend(@RequestBody OeeFilterRequest request) {

        if(StringUtils.isEmpty(request.getSite()))
            throw new QualityException(1001);

        try{
            return ResponseEntity.ok(oeeService.getScrapAndReworkTrend(request));
        }catch (QualityException qualityException){
            throw qualityException;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    // -------- Endpoints by Kartheek Nasina --------
    @PostMapping("/getSpeedLossByResource")
    public ResponseEntity<List<SpeedLossSummaryDTO>> getSpeedLossSummaryByResource(
            @RequestBody OeeFilterRequest request) {
        List<SpeedLossSummaryDTO> summary = oeeService.getSpeedLossByResource(request);
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/getSpeedLossByWorkcenter")
    public ResponseEntity<List<SpeedLossSummaryDTO>> getSpeedLossSummaryByWorkcenter(
            @RequestBody OeeFilterRequest request) {
        List<SpeedLossSummaryDTO> summary = oeeService.getSpeedLossByWorkcenter(request);
        return ResponseEntity.ok(summary);
    }


    @PostMapping("/getOverallHistory")
    public ResponseEntity<MetricsResponse> getOverallHistory(@RequestBody OeeRequest request){
        if(request.getSite() == null || request.getSite().isEmpty()){
            throw new OeeException(1001);
        }
        try{
            return ResponseEntity.ok(oeeService.getOverallHistory(request));
        }catch (OeeException oeeException){
            throw oeeException;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getOverallResourceHistory")
    public ResponseEntity<List<Map<String, Object>>> getOverallResourceHistory(@RequestBody OeeRequest request){
        if(request.getSite() == null || request.getSite().isEmpty()){
            throw new OeeException(1001);
        }
        try{
            return ResponseEntity.ok(oeeService.getOverallResourceHistoryByType(request));
        }catch (OeeException oeeException){
            throw oeeException;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getShiftByResource")
    public ResponseEntity<List<Map<String, Object>>> getShiftByResource(@RequestBody OeeRequest request){
        if(request.getSite() == null || request.getSite().isEmpty()){
            throw new OeeException(1001);
        }
        try{
            return ResponseEntity.ok(oeeService.getShiftByResource(request));
        }catch (OeeException oeeException){
            throw oeeException;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    @PostMapping("/getByOperation")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<OeeByOperationResponse> getByOperation(@RequestBody OeeFilterRequest request) {
        if (request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                OeeByOperationResponse oeeResponse = oeeService.getByOperation(request);
                return ResponseEntity.ok(oeeResponse);
            } catch (OeeException oeeException) {
                throw oeeException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new OeeException(1001);
    }

    @PostMapping("/getByresourceTimeAndInterval")
    public ResponseEntity<List<Map<String, Object>>> getByresourceTimeAndInterval(@RequestBody OeeRequest request){
        if(request.getSite() == null || request.getSite().isEmpty()){
            throw new OeeException(1001);
        }
        try{
            return ResponseEntity.ok(oeeService.getByresourceTimeAndInterval(request));
        }catch (OeeException oeeException){
            throw oeeException;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/operatorReport")
    @ResponseStatus(HttpStatus.OK)
    public CompletableFuture<ResponseEntity<OperatorReportResponse>> generateReport(@RequestBody OperatorReportRequest request) {
        if (request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return oeeService.generateReport(request)
                        .thenApply(ResponseEntity::ok)
                        .exceptionally(ex -> {
                            if (ex.getCause() instanceof OeeException) {
                                throw (OeeException) ex.getCause();
                            }
                            throw new RuntimeException(ex);
                        });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new OeeException(1001);
    }

    @PostMapping("/getOeeDetailsByPlantV1")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<SiteDetails>> getOeeDetailsByPlantV1(@RequestBody OeeRequest request) {
        if (request.getStartTime() == null || request.getEndTime() == null) {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(24);
            request.setStartTime(String.valueOf(startTime));
            request.setEndTime(String.valueOf(endTime));
        }
        if (StringUtils.isBlank(request.getSite())) {
            throw new OeeException(1001);
        }
        try {
            List<SiteDetails> siteDetailsList = new ArrayList<>();
            siteDetailsList = oeeService.getAllOeeDetailsV1(request);
            return ResponseEntity.ok(siteDetailsList);
        } catch (OeeException oeeException) {
            throw oeeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getOeeDetailsByWorkCenterIdV1")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<WorkcenterDetails>> getOeeDetailsByWorkCenterIdV1(@RequestBody OeeRequest request) {
        // Set default times if not provided (last 24 hours)
        if (request.getStartTime() == null || request.getEndTime() == null) {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(24);
            request.setStartTime(String.valueOf(startTime));
            request.setEndTime(String.valueOf(endTime));
        }
        if (StringUtils.isBlank(request.getSite())) {
            throw new OeeException(1001);
        }
        try {
            List<WorkcenterDetails> workcenterDetails = oeeService.getOeeDetailsByWorkCenterIdV1(request);
            return ResponseEntity.ok(workcenterDetails);
        } catch (OeeException oeeException) {
            throw oeeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getOverallResourceHistoryV1")
    public ResponseEntity<List<Map<String, Object>>> getOverallResourceHistoryV1(@RequestBody OeeRequest request){
        if(request.getSite() == null || request.getSite().isEmpty()){
            throw new OeeException(1001);
        }
        try{
            return ResponseEntity.ok(oeeService.getOverallResourceHistoryV1(request));
        }catch (OeeException oeeException){
            throw oeeException;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getShiftByResourceV1")
    public ResponseEntity<List<Map<String, Object>>> getShiftByResourceV1(@RequestBody OeeRequest request){
        if(request.getSite() == null || request.getSite().isEmpty()){
            throw new OeeException(1001);
        }
        try{
            return ResponseEntity.ok(oeeService.getShiftByResourceV1(request));
        }catch (OeeException oeeException){
            throw oeeException;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getOverallHistoryV1")
    public ResponseEntity<List<Map<String, Object>>> getOverallHistoryV1(@RequestBody OeeRequest request){
        if(request.getSite() == null || request.getSite().isEmpty()){
            throw new OeeException(1001);
        }
        try{
            return ResponseEntity.ok(oeeService.getOverallHistoryV1(request));
        }catch (OeeException oeeException){
            throw oeeException;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getAndLogMachinedataRecords")
    public boolean getAndLogMachinedataRecords(@RequestBody OeeRequest request){
        if(request.getSite() == null || request.getSite().isEmpty()){
            throw new OeeException(1001);
        }
        try{
            return oeeService.getAndLogMachinedataRecords(request);
        }catch (OeeException oeeException){
            throw oeeException;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getAndLogMachinedataRecordsBetween")
    public boolean getAndLogMachinedataRecordsBetween(@RequestBody OeeProductionLogRequest request) {
        if (request.getSite() == null || request.getIntervalStartDateTime() == null || request.getIntervalEndDateTime() == null) {
            throw new OeeException(1001);
        }
        try {
            return oeeService.getAndLogMachinedataRecordsBetween(request.getSite(), request.getIntervalStartDateTime(), request.getIntervalEndDateTime());
        } catch (OeeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getOverallOeeReport")
    public ResponseEntity<OverallOeeReportResponse> getOverallOeeReport(@RequestBody OeeRequest request) {
        if (request.getSite() == null || request.getSite().isEmpty()) {
            throw new OeeException(1001);
        }
        try {
            OverallOeeReportResponse response = oeeService.getOverallOeeReport(request.getSite());
            return ResponseEntity.ok(response);
        } catch (OeeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
