package com.rits;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.configuration.NotificationHandler;
import com.rits.kafkaevent.BaseEvent;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import com.rits.kafkapojo.ProductionLogPlacedEvent;

@SpringBootApplication
@Slf4j
public class IntegrationServiceApplication {



 //   @Autowired
    private NotificationHandler notificationHandler;

    public static void main(String[] args) {
        SpringApplication.run(IntegrationServiceApplication.class, args);
    }

    //@KafkaListener(topics = "notificationTopic")
    public void consumeEvent(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            BaseEvent event = objectMapper.readValue(message, BaseEvent.class);
            String eventType = event.getEventType();

            if ("ProductionLogPlacedEvent".equals(eventType)) {
                ProductionLogPlacedEvent productionLogEvent = objectMapper.readValue(message, ProductionLogPlacedEvent.class);
                notificationHandler.handle(productionLogEvent);
            } else if ("OEEEvent".equals(eventType)) {
                // Handle OEEEvent
            } else if ("AuditLogEvent".equals(eventType)) {
                // Handle AuditLogEvent
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public ObservationRegistry observationRegistry() {
        return ObservationRegistry.create();
    }

/*
    private final ObservationRegistry observationRegistry;
    private final Tracer tracer;

    public static void main(String[] args) {
        SpringApplication.run(IntegrationServiceApplication.class, args);
    }


    @KafkaListener(topics = "notificationTopic")
    public void handleNotification(ProductionLogPlacedEvent productionLogPlacedEvent) {
        Observation.createNotStarted("on-message", this.observationRegistry).observe(() -> {
            log.info("Got message <{}>", productionLogPlacedEvent);
            log.info("TraceId- {}, Received Notification for Order - {}", this.tracer.currentSpan().context().traceId(),
                    productionLogPlacedEvent.getProductionLogType());
        });

    }


    @KafkaListener(topics = "notificationTopic")
    public void consumeEvent(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            // Deserialize the JSON message into a YourBaseEvent object
            BaseEvent event = objectMapper.readValue(message, BaseEvent.class);

            // Determine the event type from the eventType field in the JSON message
            String eventType = event.getEventType();

            // Route to the appropriate handler based on the event type
            if ("ProductionLogPlacedEvent".equals(eventType)) {
                ProductionLogPlacedEvent productionLogEvent = objectMapper.readValue(message, ProductionLogPlacedEvent.class);
                handleNotification(productionLogEvent);
            } else if ("OEEEvent".equals(eventType)) {

            } else if ("AuditLogEvent".equals(eventType)) {

            }
            // Add more handlers for other event types as needed
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public ObservationRegistry observationRegistry() {
        return ObservationRegistry.create();
    }
*/

}