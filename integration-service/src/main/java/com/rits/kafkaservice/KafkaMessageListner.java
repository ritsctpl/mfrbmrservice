package com.rits.kafkaservice;
import com.fasterxml.jackson.core.type.TypeReference;
import com.rits.configuration.NotificationHandler;
import com.rits.kafkaevent.BaseEvent;
import com.rits.kafkaevent.Message;
import com.rits.kafkaevent.ProductionLogEvent;
import com.rits.kafkapojo.ProductionLogPlacedEvent;
import com.rits.kafkapojo.ProductionLogRequest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

@Service
public class KafkaMessageListner {
    private final ObjectMapper objectMapper;
    private final NotificationHandler notificationHandler;

    public KafkaMessageListner(ObjectMapper objectMapper, NotificationHandler notificationHandler) {
        this.objectMapper = objectMapper;
        this.notificationHandler = notificationHandler;
    }

    /*@KafkaListener(topics = "OEE", groupId = "notificationId", containerFactory = "kafkaListenerContainerFactory")
    public void consumeMessage(Message<?> message) {
        notificationHandler.handle(message);
    }*/

    @KafkaListener(topics = "OEE", groupId = "notificationId", containerFactory = "kafkaListenerContainerFactory")
    public void consumeJsonMessage(String jsonMessage) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Message<?> message = objectMapper.readValue(jsonMessage, new TypeReference<Message<?>>() {});
            notificationHandler.handle(message);
        } catch (IOException e) {
            throw new RuntimeException("Error while processing JSON message", e);
        }
    }

    @KafkaListener(topics = "notificationTopic", groupId = "notificationId", containerFactory = "kafkaListenerContainerFactory")
    public void consumeEvent(String message) {
        try {
            ProductionLogEvent event = objectMapper.readValue(message, ProductionLogEvent.class);
            //BaseEvent event = objectMapper.readValue(message, BaseEvent.class);
            String eventType = event.getEventType();

            if ("ProductionLogPlacedEvent".equals(eventType)) {
                ProductionLogPlacedEvent productionLogEvent = objectMapper.readValue(message, ProductionLogPlacedEvent.class);
                notificationHandler.handle(productionLogEvent);
            } else if (("PCU_START".equals(eventType)) || ("PCU_SIGNOFF".equals(eventType))) {
                ProductionLogRequest productionLogRequest = objectMapper.readValue(message, ProductionLogRequest.class);
                notificationHandler.handle(productionLogRequest);
               // OEEEvent oeeEvent = objectMapper.readValue(message, OEEEvent.class);
                // Handle OEEEvent
            } else  {
                //AuditLogEvent auditLogEvent = objectMapper.readValue(message, AuditLogEvent.class);
                // Handle AuditLogEvent
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while processing Kafka message", e);
        }
    }
}
