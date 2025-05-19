package com.rits.qualityservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.performanceservice.dto.PerformanceRequestList;
import com.rits.qualityservice.dto.QualityRequestList;
import com.rits.qualityservice.exception.QualityException;
import com.rits.qualityservice.service.QualityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/quality-service")
public class QualityServiceController {
 private final QualityService qualityService;
 private final ObjectMapper objectMapper;
    @PostMapping("calculateQuality")
    public QualityRequestList retrieveByPcuAndOperationAndShopOrderAndEventType(@RequestBody QualityRequestList qualityRequestList)
    {
        try {
            return qualityService.calculateQuality(qualityRequestList);
        } catch (QualityException qualityException) {
            throw qualityException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping(value = "/calculateQualityByJson", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object calculateQualityByJson(@RequestBody String jsonPayload) {


        try {
            JsonNode rootNode = objectMapper.readTree(jsonPayload);
            PerformanceRequestList performanceRequestList = objectMapper.treeToValue(rootNode, PerformanceRequestList.class);
            QualityRequestList performanceToQualityJson= qualityService.getPerformanceToQualityRequest(performanceRequestList);
            QualityRequestList qualityResponse=qualityService.calculateQuality(performanceToQualityJson);
            System.out.print("Quality method ends here");
            return qualityResponse;

        } catch (QualityException qualityException) {
            throw qualityException;
        }catch (Exception e) {
            e.printStackTrace();
            return null; // Consider a better error handling strategy
        }
    }
}
