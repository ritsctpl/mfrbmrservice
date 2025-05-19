package com.rits.productionlogservice.listner;

import com.rits.pcurouterheaderservice.model.PcuRouterHeader;
import com.rits.productionlogservice.event.KafkaProductionLogRequest;
import com.rits.productionlogservice.event.Message;
import com.rits.productionlogservice.event.ProductionLogPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductionLogPlacedEventListner {

    private final WebClient.Builder webClientBuilder;
    @Value("${integration-service.uri}/logkafkaproductionevent")
    private String integrationServiceProductionLog;
    @Value("${integration-service.uri}/logkafkaproductionlog")
    private String integrationSrvcProductionLog;

    @Value("${integration-service.uri}/logkafkafrommessage")
    private String integrationServiceFromMessage;

    @EventListener
    public void handleProductionLogPlacedEvent(ProductionLogPlacedEvent event) {
        if (event.getProductionLogType() != null) {
            boolean output = webClientBuilder.build()
                    .post()
                    .uri(integrationServiceProductionLog)
                    .bodyValue(new KafkaProductionLogRequest("ProductionLogEvent", event.getProductionLogType()))
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();

        }else{
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("topicName", "notificationTopic");
            requestBody.put("productionLogRequest", event.getProductionLogRequest());

            boolean output = webClientBuilder.build()
                    .post()
                    .uri(integrationSrvcProductionLog)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
        }
    }

    @EventListener
    public void handleProductionLogPlacedEventWithMessage(Message<?> message) {
        String topicName = "OEE";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("topicName", topicName);
        requestBody.put("message", message);

        boolean output = webClientBuilder.build()
                .post()
                .uri(integrationServiceFromMessage)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }

}
