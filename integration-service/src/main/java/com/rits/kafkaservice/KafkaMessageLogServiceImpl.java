package com.rits.kafkaservice;

import com.rits.kafkaevent.Message;
import com.rits.kafkapojo.ProductionLogPlacedEvent;
import com.rits.kafkapojo.ProductionLogRequest;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaMessageLogServiceImpl implements KafkaMessageLogService {

    private final KafkaTemplate<String, ProductionLogPlacedEvent> kafkaTemplate;
    private final KafkaTemplate<String, ProductionLogRequest> kafkaProdLogTemplate;
    private final KafkaTemplate<String, Message<?>> kafkaTemplateMessage;
    private final ObservationRegistry observationRegistry;

    @Override
    public boolean logKafkaProductionLog(String topicName, ProductionLogPlacedEvent productionLogPlacedEvent) {
        handleProductionLogPlacedEvent(topicName,productionLogPlacedEvent);
        return true;
    }

    @Override
    public boolean logKafkaProductionLog(String topicName, ProductionLogRequest productionLogPlacedRequest) {
        handleProductionLogPlaced(topicName,productionLogPlacedRequest);
        return true;
    }

    @Override
    public boolean logKafkaProductionLog(String topicName, Message<?> message) {
        handleProductionLogPlacedMsg(topicName, message);
        return true;
    }

    private void handleProductionLogPlacedMsg(String topicName, Message<?> message) {
        log.info("Production Log Placed Event Received, Sending Production Log Event to notificationTopic: {}", message.getEventType());

        try {
            CompletableFuture<SendResult<String, Message<?>>> completableFuture =
                    CompletableFuture.supplyAsync(() -> {
                        ListenableFuture<SendResult<String, Message<?>>> listenableFuture = kafkaTemplateMessage.send(topicName, message);
                        try {
                            return listenableFuture.get();
                        } catch (InterruptedException | ExecutionException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Error while sending message to Kafka", e);
                        }
                    });

            completableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error while sending message to Kafka", e);
        }
    }

    /*
        private void handleProductionLogPlacedMsg(String topicName, Message<?> message) {
        log.info("Production Log Placed Event Received, Sending to Kafka Topic: {}", topicName);

        // Validate message payload
        if (message.getEventType() == null || message.getObject() == null) {
            throw new IllegalArgumentException("Invalid message payload: " + message);
        }

        CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Sending message to Kafka topic: {} with eventType: {} and payload: {}", topicName, message.getEventType(), message.getObject());
                ListenableFuture<SendResult<String, Message<?>>> listenableFuture = kafkaTemplateMessage.send(topicName, message);
                return listenableFuture.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted while sending message to Kafka", e);
            } catch (ExecutionException e) {
                throw new RuntimeException("Kafka message send failed", e.getCause());
            }
        }).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send message to Kafka topic: {}", topicName, ex);
            } else {
                log.info("Message sent successfully to Kafka topic: {} with metadata: {}", topicName, result.getRecordMetadata());
            }
        });
    }
*/

    private void handleProductionLogPlaced(String topicName, ProductionLogRequest productionLogPlacedRequest) {
        log.info("Production Log Placed Event Received, Sending Production Log Event to notificationTopic: {}", productionLogPlacedRequest.getEventType());

        // Create Observation for Kafka Template
        try {
            CompletableFuture<SendResult<String, ProductionLogRequest>> completableFuture =
                    CompletableFuture.supplyAsync(() -> {
                        ListenableFuture<SendResult<String, ProductionLogRequest>> listenableFuture = kafkaProdLogTemplate.send("notificationTopic",
                                productionLogPlacedRequest);
                        try {
                            return listenableFuture.get();
                        } catch (InterruptedException | ExecutionException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Error while sending message to Kafka", e);
                        }
                    });

            completableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error while sending message to Kafka", e);
        }
    }


   /* private void handleProductionLogPlacedEvent(String topic, ProductionLogPlacedEvent event) {
        log.info("Production Log Placed Event Received, Sending Production Log Event to notificationTopic: {}", event.getProductionLogType());

        // Create Observation for Kafka Template
        try {
            Observation.createNotStarted("notification-topic", this.observationRegistry).observeChecked(() -> {
                CompletableFuture<SendResult<String, ProductionLogPlacedEvent>> future = kafkaTemplate.send("notificationTopic",
                        new ProductionLogPlacedEvent(event.getProductionLogType()));
                return future.handle((result, throwable) -> CompletableFuture.completedFuture(result));
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error while sending message to Kafka", e);
        }
    }*/

    private void handleProductionLogPlacedEvent(String topic, ProductionLogPlacedEvent event) {
        log.info("Production Log Placed Event Received, Sending Production Log Event to notificationTopic: {}", event.getProductionLogType());

        // Create Observation for Kafka Template
        try {
            CompletableFuture<SendResult<String, ProductionLogPlacedEvent>> completableFuture =
                    CompletableFuture.supplyAsync(() -> {
                        ListenableFuture<SendResult<String, ProductionLogPlacedEvent>> listenableFuture = kafkaTemplate.send("notificationTopic",
                                new ProductionLogPlacedEvent(event.getProductionLogType()));
                        try {
                            return listenableFuture.get();
                        } catch (InterruptedException | ExecutionException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Error while sending message to Kafka", e);
                        }
                    });

            completableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error while sending message to Kafka", e);
        }
    }




}
