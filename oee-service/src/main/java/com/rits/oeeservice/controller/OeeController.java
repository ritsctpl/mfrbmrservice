package com.rits.oeeservice.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.oeeservice.dto.*;
import com.rits.oeeservice.exception.OeeException;
import com.rits.oeeservice.model.Oee;
import com.rits.oeeservice.service.OeeService;
import com.rits.performanceservice.dto.PerformanceRequestList;
import com.rits.qualityservice.dto.QualityRequestList;
import com.rits.qualityservice.exception.QualityException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/oee-service")
public class OeeController {
    private final OeeService oeeService;
    private final ObjectMapper objectMapper;
    @PostMapping("calculateOee")
    public Boolean calculateOee(@RequestBody OeeRequestList oeeRequestList)
    {
        try {
            return oeeService.calculateOee(oeeRequestList);
        } catch (OeeException oeeException) {
            throw oeeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*@PostMapping("/getAvailabilityByResource")
    public AvailabilityByResourceList getAvailabilityByResource(@RequestBody List<Oee> oeeData)
    {
        try {
            return oeeService.getAvailabilityByResource(oeeData);
        } catch (OeeException oeeException) {
            throw oeeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/
    @PostMapping("/getOEECompleteResult")
    public FinalOEE getOEECompleteResult(@RequestBody FilterObjects filterObjects)
    {
        try {
            return oeeService.getOEECompleteResult(filterObjects);
        } catch (OeeException oeeException) {
            throw oeeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping(value = "/calculateOeeByJson", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object calculateQualityByJson(@RequestBody String jsonPayload) {


        try {
            JsonNode rootNode = objectMapper.readTree(jsonPayload);
            QualityRequestList qualityRequestList = objectMapper.treeToValue(rootNode, QualityRequestList.class);
            OeeRequestList getQualityToOeeRequest= oeeService.getQualityToOeeRequest(qualityRequestList);

            Boolean oee=oeeService.calculateOee(getQualityToOeeRequest);
            System.out.print("oee method ends here");
            return oee;

        } catch (OeeException oeeException) {
            throw oeeException;
        }catch (Exception e) {
            e.printStackTrace();
            return null; // Consider a better error handling strategy
        }
    }
    @PostMapping("/getOEELiveData")
    public FinalOEE getOEECompleteResultForLiveData(@RequestBody FilterObjects filterObjects)
    {
        try {
            return oeeService.getOEECompleteResultForLiveData(filterObjects);
        } catch (OeeException oeeException) {
            throw oeeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/retrieve")
    public FinalOEE retrieveOee(@RequestBody FilterObjects filterObjects)
    {
        try {
            return oeeService.retrieveOee(filterObjects);
        } catch (OeeException oeeException) {
            throw oeeException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
