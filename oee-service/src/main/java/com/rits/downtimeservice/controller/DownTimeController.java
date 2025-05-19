package com.rits.downtimeservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.rits.downtimeservice.dto.*;
import com.rits.downtimeservice.exception.DownTimeException;
import com.rits.downtimeservice.model.DownTimeAvailability;
import com.rits.downtimeservice.model.DownTimeMessageModel;
import com.rits.downtimeservice.service.DowntimeService;
import com.rits.oeeservice.model.Oee;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/downtime-service")
public class DownTimeController {
    private final DowntimeService downtimeService;
    private final ObjectMapper objectMapper;
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public DownTimeMessageModel logAvailability(@RequestBody DownTimeRequest downTimeRequest) throws Exception {
        DownTimeMessageModel responce=new DownTimeMessageModel();

             responce = downtimeService.logDownTimeandAvailability(downTimeRequest);

    return responce;
    }

    @PostMapping("/schedulerCall")
    @ResponseStatus(HttpStatus.CREATED)
    public DownTimeMessageModel logAvailability() throws Exception {
        DownTimeMessageModel responce=new DownTimeMessageModel();

            responce = downtimeService.logDownTimeandAvailability();

        return responce;
    }

    @PostMapping(value ="/logDowntime", consumes = MediaType.APPLICATION_JSON_VALUE)
    public DownTimeMessageModel logDownTimeAndAvailabilityByProductionLog(@RequestBody String productionLog) {
        try {
            ProductionLogRequest actualObject = objectMapper.readValue(productionLog, ProductionLogRequest.class);
           // Map<String, Object> productionLogMap = objectMapper.readValue(productionLog, new TypeReference<Map<String, Object>>() {});


            return downtimeService.logDownTimeandAvailabilitybyProductionLog(actualObject);
        } catch (Exception e) {
            // Handle exception appropriately
            e.printStackTrace();
            return null; // Or return an error message model
        }
    }

    @PostMapping(value = "/logDowntimeByJson", consumes = MediaType.APPLICATION_JSON_VALUE)
    public DownTimeMessageModel logDownTimebyJson(@RequestBody String jsonPayload) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonPayload);
            Object targetObject;

            if (rootNode.has("eventType") && "MC_DOWN".equals(rootNode.get("eventType").asText())) {
                targetObject = objectMapper.treeToValue(rootNode, ProductionLogRequest.class);
            } else {

                targetObject = objectMapper.treeToValue(rootNode, ProductionLogRequest.class);
            }

            return downtimeService.logDownTimeandAvailabilitybyProductionLog(targetObject);

        } catch (Exception e) {
            e.printStackTrace();
            return null; // Consider a better error handling strategy
        }
    }

    @PostMapping("/getUnproccessedRec")
    @ResponseStatus(HttpStatus.CREATED)
    public List<DownTimeAvailability> getUnProccessedRec() throws Exception {
        return downtimeService.getUnproccessedRec();

    }
    @PostMapping("/update")
    @ResponseStatus(HttpStatus.CREATED)
    public DownTimeMessageModel updateRec(@RequestBody DownTimeRequest downTimeRequest) throws Exception {
        return downtimeService.updateRec(downTimeRequest);

    }
    @PostMapping("/getAvailabilityForLive")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Oee> getAvailabilityForLive(@RequestBody DownTimeRequest downTimeRequest) throws Exception {
        try {
            return downtimeService.getAvailabilityForLiveData(downTimeRequest.getSite(),downTimeRequest.getResourceId());
        }catch(DownTimeException e){
            throw e;
        }catch(Exception e){
            throw e;
        }
    }
    @PostMapping("/getAvailabilityForScheduler")
    @ResponseStatus(HttpStatus.CREATED)
    public DownTimeMessageModel getAvailabilityForScheduler(@RequestBody DownTimeRequest downTimeRequest) throws Exception {
        try {
            return downtimeService.getAvailabilityForScheduler(downTimeRequest.getSite(),downTimeRequest.getResourceList(),downTimeRequest.getCombinations());
        }catch(DownTimeException e){
            throw e;
        }catch(Exception e){
            throw e;
        }
    }

    @PostMapping("/getTotalDownTimeByReasonCodeInShifts")
    @ResponseStatus(HttpStatus.OK)
    public List<DowntimeResponse> getTotalDownTimeByReasonCodeInEachShifts(@RequestBody DownTimeRequest downTimeRequest) throws Exception {
        try {
            return downtimeService.getTotalDownTimeByReasonCodeInEachShift(downTimeRequest.getSite(),downTimeRequest.getResource(),downTimeRequest.getStartDate(),downTimeRequest.getEndDate());
        }catch(DownTimeException e){
            throw e;
        }catch(Exception e){
            throw e;
        }
    }

    @PostMapping("/getTotalDownTimeForCurrentShift")
    @ResponseStatus(HttpStatus.OK)
    public DownTimeByShift getTotalDownTimeForCurrentShifts(@RequestBody DownTimeRequest downTimeRequest) throws Exception {
        try {
            return downtimeService.getTotalDownTimeForCurrentShift(downTimeRequest.getSite(),downTimeRequest.getResource());
        }catch(DownTimeException e){
            throw e;
        }catch(Exception e){
            throw e;
        }
    }

    @PostMapping("/getDownTimeBetweenDates")
    @ResponseStatus(HttpStatus.OK)
    public List<DownTimeAvailability> getDownTimeBetweenDates(@RequestBody DownTimeRequest downTimeRequest) throws Exception {
        try {
            return downtimeService.getAllRecordsBetweenDateTime(downTimeRequest.getSite(),downTimeRequest.getResource());
        }catch(DownTimeException e){
            throw e;
        }catch(Exception e){
            throw e;
        }
    }
}
