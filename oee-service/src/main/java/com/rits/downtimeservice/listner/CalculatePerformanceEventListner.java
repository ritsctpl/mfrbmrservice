package com.rits.downtimeservice.listner;

import com.rits.downtimeservice.dto.Message;
import com.rits.downtimeservice.model.DownTimeMessageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CalculatePerformanceEventListner {
    private final WebClient.Builder webClientBuilder;
    @Value("${integration-service.uri}/logkafkafrommessage")
    private String integrationServiceFromMessage;
    @EventListener
    public void handlePerformanceEvent(DownTimeMessageModel downTimeMessageModel){
        Message<DownTimeMessageModel> message = new Message<>("OeePerformance", downTimeMessageModel);
        String topicName = "OEE";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("topicName", topicName);
        requestBody.put("message", message);

       webClientBuilder.build()
                .post()
                .uri(integrationServiceFromMessage)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> {
                      return clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                        System.err.println("Error response body: " + errorBody);
                        return Mono.error(new RuntimeException("API call failed with error: " + errorBody));
                    });
                })
                .bodyToMono(Boolean.class)
                .subscribe(
                        result -> {
                            // Handle successful response here if needed
                            System.out.println("Success: " + result);
                        },
                        error -> {
                            // Handle error
                            System.err.println("Error: " + error.getMessage());
                        },
                        () -> {
                            // Handle completion signal if needed (optional)
                            System.out.println("Completed");
                        }
                );

    }
}
