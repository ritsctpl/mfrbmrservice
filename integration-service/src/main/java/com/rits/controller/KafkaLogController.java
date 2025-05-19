package com.rits.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.kafkaevent.Message;
import com.rits.kafkapojo.ProductionLogPlacedEvent;
import com.rits.kafkapojo.ProductionLogRequest;
import com.rits.kafkaservice.GenericEnqueuer;
import com.rits.kafkaservice.KafkaMessageLogService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/integration-service")
public class KafkaLogController {
    private final KafkaMessageLogService kafkaMessageLogService;
    private final ObjectMapper objectMapper;
    private final ApplicationContext context;
    private final GenericEnqueuer enqueuer;
    private static final Logger log = LoggerFactory.getLogger(KafkaLogController.class); // Manual logger

    @PostMapping("/shutdown")
    public void shutdown() {
        System.out.println("Shutting down...");
        SpringApplication.exit(context, () -> 1);
    }

    @PostMapping("/test")
    public String shutdowntest() {
        return "Sucess";
    }
    @PostMapping("/logkafkaproductionevent")
    public Boolean create(@RequestBody KafkaProductionLogRequest productionLogRequest)
    {
       return kafkaMessageLogService.logKafkaProductionLog(productionLogRequest.getNotificationTopic(),new ProductionLogPlacedEvent(productionLogRequest.getEventType()));
    }

    @PostMapping("/logkafkaproductionlog")
    public Boolean create(@RequestBody Map<String, Object> requestBody )
    {
        String topicName = (String) requestBody.get("topicName");
        ProductionLogRequest productionLogRequest = objectMapper.convertValue(requestBody.get("productionLogRequest"), ProductionLogRequest.class);
        return kafkaMessageLogService.logKafkaProductionLog(topicName,productionLogRequest);
    }

    /*@PostMapping("/logkafkafrommessage")
    public Boolean createFromMessage(@RequestBody Map<String, Object> requestMap) {
        String topicName = (String) requestMap.get("topicName");
        Message<?> message = (Message<?>) requestMap.get("message");

        return kafkaMessageLogService.logKafkaProductionLog(topicName, message);
    }
*/


    @PostMapping("/logkafkafrommessage")
    public Boolean createFromMessage(@RequestBody Map<String, Object> requestMap) {
        String topicName = (String) requestMap.get("topicName");
        ObjectMapper objectMapper = new ObjectMapper();

        // Use TypeReference to capture the generic type of Message
        TypeReference<Message<?>> messageType = new TypeReference<Message<?>>() {};

        // Deserialize the "message" field as a Message<?> object
        Message<?> message = objectMapper.convertValue(requestMap.get("message"), messageType);

        return kafkaMessageLogService.logKafkaProductionLog(topicName, message);
    }

    /*@PostMapping
    public void enqueueKafkaMessage(@RequestBody Map<String, Object> request) {
        String topicName = (String) request.get("topicName");
        Object payload = request.get("payload");

        if (topicName == null || payload == null) {
            throw new IllegalArgumentException("Both 'topicName' and 'payload' fields are required.");
        }

        log.info("Enqueuing message to topic: {}, payload: {}", topicName, payload);
        enqueuer.enqueueMessage(topicName, payload);
    }*/
    @PostMapping("/logGenericMessage")
    public Boolean logGenericMessage(@RequestBody Map<String, Object> requestMap) {
        // Validate topicName
        String topicName = (String) requestMap.get("topicName");
        if (topicName == null || topicName.isEmpty()) {
            throw new IllegalArgumentException("topicName is required");
        }

        // Validate message
        Object message = requestMap.get("payload");
        if (message == null) {
            throw new IllegalArgumentException("message is required");
        }

        log.info("Received request to log generic message to topic: {}, message: {}", topicName, message);

        // Send message using the generic enqueuer
        return enqueuer.logGenericMessage(topicName, message);
    }


    /*
    @PostMapping("/logkafkafrommessage")
    public Boolean createFromMessage(@RequestBody Map<String, Object> requestMap) {
        String topicName = (String) requestMap.get("topicName");
        Message<?> message;
        ObjectMapper objectMapper = new ObjectMapper();
        String messageJson = null;
        try {
            messageJson = objectMapper.writeValueAsString(requestMap.get("message"));
            message = objectMapper.readValue(messageJson, Message.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


        return kafkaMessageLogService.logKafkaProductionLog(topicName, message);
    }*/
}
