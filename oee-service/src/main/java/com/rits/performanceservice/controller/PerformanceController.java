package com.rits.performanceservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.downtimeservice.dto.ProductionLogRequest;
import com.rits.downtimeservice.model.DownTimeAvailability;
import com.rits.downtimeservice.model.DownTimeMessageModel;
import com.rits.performanceservice.dto.DownTime;
import com.rits.performanceservice.dto.PerformanceRequestList;
import com.rits.performanceservice.exception.PerformanceException;
import com.rits.performanceservice.service.PerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/performance-service")
public class PerformanceController {
    private final PerformanceService  performanceService;
    private final ObjectMapper objectMapper;

    @PostMapping("calculatePerformance")
    public ResponseEntity<PerformanceRequestList> calculatePerformance(@RequestBody PerformanceRequestList performanceRequestList) {

        PerformanceRequestList calculatePerformance;

            try {
                calculatePerformance = performanceService.calculatePerformance(performanceRequestList);
                return ResponseEntity.ok(calculatePerformance);
            } catch (PerformanceException performanceException) {
                throw performanceException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    @PostMapping("getUnProcessedRecord")
    public ResponseEntity<PerformanceRequestList> getUnProcessedRecord(@RequestBody PerformanceRequestList performanceRequestList) {

        PerformanceRequestList getUnProcessedRecord;

        try {
            getUnProcessedRecord = performanceService.getUnProcessedRecord(performanceRequestList);
            return ResponseEntity.ok(getUnProcessedRecord);
        } catch (PerformanceException performanceException) {
            throw performanceException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping(value = "/calculatePerformanceByJson", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object logDownTimebyJson(@RequestBody String jsonPayload) {
        PerformanceRequestList calculatePerformance;

        try {
            JsonNode rootNode = objectMapper.readTree(jsonPayload);
            DownTimeMessageModel downTimeMessageModel = objectMapper.treeToValue(rootNode, DownTimeMessageModel.class);

            // Assuming downTimeMessageModel contains necessary information to create a DownTimeAvailability object
            DownTimeAvailability downTimeAvailability = downTimeMessageModel.getResponse();
            DownTime downtime= performanceService.downTimeAvailabilityToDownTimeBuilder(downTimeAvailability);
            // Set properties of downTimeAvailability

            PerformanceRequestList performanceRequestListObject = new PerformanceRequestList();
            List<DownTime> performanceRequestList = new ArrayList<>();
            performanceRequestList.add(downtime);
            performanceRequestListObject.setPerformanceRequestList(performanceRequestList);


            // Assuming performanceResponseList needs to be initialized
            calculatePerformance = performanceService.calculatePerformance(performanceRequestListObject);


            System.out.print(" calculatePerformance method ends here");
            return calculatePerformance;

        } catch (Exception e) {
            e.printStackTrace();
            return null; // Consider a better error handling strategy
        }
    }


}
