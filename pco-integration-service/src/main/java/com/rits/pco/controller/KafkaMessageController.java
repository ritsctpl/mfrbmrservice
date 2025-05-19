package com.rits.pco.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.pco.dto.KafkaApiRequest;
import com.rits.pco.dto.KafkaMessageRequest;
import com.rits.pco.service.KafkaMessageService;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/app/v1/pco-integration-service/kafka")
public class KafkaMessageController {

    private final KafkaMessageService kafkaMessageService;
    private final ObjectMapper objectMapper;

    public KafkaMessageController(KafkaMessageService kafkaMessageService, ObjectMapper objectMapper) {
        this.kafkaMessageService = kafkaMessageService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/send")
    public String sendMessage(
            @RequestParam String topic,
            @RequestParam(required = false) String key,
            @RequestBody String message) {

        kafkaMessageService.sendMessage(topic, key, message);
        return "✅ Message sent to Kafka topic: " + topic;
    }

    @PostMapping("/send-json")
    public String sendJsonMessage(@RequestBody KafkaMessageRequest request) {
        try {
            String messageString;

            if (request.getMessage() instanceof JsonNode) {
                // Convert JsonNode to String
                messageString = objectMapper.writeValueAsString(request.getMessage());
            } else {
                messageString = request.getMessage().toString();
            }

            kafkaMessageService.sendMessage(request.getTopic(), request.getKey(), messageString);
            return "✅ JSON Message sent to Kafka topic: " + request.getTopic();
        } catch (Exception e) {
            return "❌ Error sending message: " + e.getMessage();
        }
    }

    // ✅ New method for API request messages
    @PostMapping("/send-api-request")
    public String sendApiRequest(@RequestBody KafkaApiRequest request) {
        String topic = request.getPcoId() + "-" + request.getAgentId();

        kafkaMessageService.sendMessageApi(topic, null, request);
        return "✅ API request sent to Kafka topic: " + topic;
    }

}
