package com.rits.oeeservice.listner;

import com.rits.availability.dto.AvailabilityRequest;
import com.rits.common.dto.OeeFilterRequest;
import com.rits.downtimeservice.dto.DowntimeRequest;
import com.rits.oeeservice.service.OeeService;
import com.rits.performance.dto.PerformanceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class OeeServiceHelper {

    private final WebClient.Builder webClientBuilder;
    private final OeeService oeeService; // Injecting OeeService

    @Value("${availability.url}/getAvailabilityRec")
    private String getAvailabilityUrl;
    @Value("${availability.url}/logAvailability")
    private String logAvailabilityUrl;
    @Value("${performance-service.url}/getPerfromanceByDateRange")
    private String getPerfromanceByDateRangeUrl;
    @Value("${performance-service.url}/calculatePerformance")
    private String calculatePerformanceUrl;

    @Value("${quality-service.url}/getQualityByDateTime")
    private String getQualityByDateTimeUrl;
    @Value("${quality-service.url}/calculateQuality")
    private String calculateQualityUrl;
    @Value("${downtime-service.url}/log")
    private String logDowntimeUrl;

    Boolean calculateDownTime(OeeFilterRequest request) {
        System.out.println("Starting calculateDownTime...");
        DowntimeRequest downtimeRequest = new DowntimeRequest();
        downtimeRequest.setSite(request.getSite());
        downtimeRequest.setDowntimeStart(request.getStartDateTime());
        downtimeRequest.setDowntimeEnd(request.getEndDateTime());
        return webClientBuilder.build()
                .post()
                .uri(logDowntimeUrl)
                .bodyValue(downtimeRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }

    Boolean calculateAvailability(OeeFilterRequest request) {
        System.out.println("Starting calculateAvailability...");
        AvailabilityRequest availabilityRequest = new AvailabilityRequest();
        availabilityRequest.setSite(request.getSite());
        availabilityRequest.setStartDateTime(request.getStartDateTime());
        availabilityRequest.setEndDateTime(request.getEndDateTime());
        return webClientBuilder.build()
                .post()
                .uri(logAvailabilityUrl)
                .bodyValue(availabilityRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }

    Boolean calculatePerformance(OeeFilterRequest request) {
        System.out.println("Starting calculatePerformance...");
        PerformanceRequest performanceRequest = new PerformanceRequest();
        performanceRequest.setSite(request.getSite());
        performanceRequest.setStartDateTime(request.getStartDateTime());
        performanceRequest.setEndDateTime(request.getEndDateTime());
        return webClientBuilder.build()
                .post()
                .uri(calculatePerformanceUrl)
                .bodyValue(performanceRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }

    Boolean calculateQuality(OeeFilterRequest request) {
        System.out.println("Starting calculateQuality...");
        return webClientBuilder.build()
                .post()
                .uri(calculateQualityUrl)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }

    Boolean calculateOee(OeeFilterRequest request) {
        System.out.println("Starting calculateOee...");
        return oeeService.calculateOEE(request); // Delegate to OeeService
    }
}
