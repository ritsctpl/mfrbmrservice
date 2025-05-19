package com.rits.kafkaservice;

import com.rits.kafkapojo.MessageWrapper;
import com.rits.kafkapojo.ProcessedMessage;
import com.rits.kafkapojo.TopicConfiguration;
import com.rits.repository.ProcessedMessageRepository;
import com.rits.repository.TopicConfigurationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class GenericKafkaListener {

    @Autowired
    private TopicConfigurationRepository topicConfigurationRepository;

    @Autowired
    private ProcessedMessageRepository processedMessageRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    // Inject the sync topic property.
    @Value("${sync.topic}")
    private String syncTopic;

    // Listener for the sync topic using the dedicated container factory
    @KafkaListener(
            topics = "#{'${sync.topic}'.split(',')}",
            groupId = "generic-group",
            containerFactory = "syncListenerContainerFactory"
    )
    public void onSyncTopicMessage(@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   @Header(KafkaHeaders.OFFSET) long offset,
                                   @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                                   Acknowledgment acknowledgment,
                                   String rawMessage) {
        processMessage(topic, offset, partition, rawMessage, true);
        acknowledgment.acknowledge();
    }

    // Listener for all other topics using your default manual ack container factory
    @KafkaListener(
            topics = "#{'${kafka.topics}'.split(',')}",
            groupId = "generic-group",
            containerFactory = "manualAckListenerContainerFactory"
    )
    public void onOtherMessages(@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                @Header(KafkaHeaders.OFFSET) long offset,
                                @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                                Acknowledgment acknowledgment,
                                String rawMessage) {
        // Only process messages if they are not equal to the sync topic.
        if (syncTopic.equals(topic)) {
            // Optionally, you can skip here because sync messages are handled by onSyncTopicMessage.
            acknowledgment.acknowledge();
            return;
        }
        processMessage(topic, offset, partition, rawMessage, false);
        acknowledgment.acknowledge();
    }

    // A common method that does deduplication and branching based on whether processing should be sync or async.
    private void processMessage(String topic, long offset, int partition, String rawMessage, boolean isSync) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Deserialize the rawMessage into a MessageWrapper object.
            MessageWrapper<?> messageWrapper = objectMapper.readValue(rawMessage, new TypeReference<MessageWrapper<?>>() {});
            String messageId = messageWrapper.getMessageId();

            log.info("Processing message: topic={}, partition={}, offset={}, messageId={}",
                    topic, partition, offset, messageId);

            // Deduplication.
            if (processedMessageRepository.existsByMessageId(messageId)) {
                log.info("Duplicate message detected. Skipping processing for messageId: {}", messageId);
                return;
            }

            // Save status as PENDING.
            processedMessageRepository.save(ProcessedMessage.builder()
                    .messageId(messageId)
                    .message(rawMessage)
                    .topicName(topic)
                    .status("PENDING")
                    .processedAt(LocalDateTime.now())
                    .build());

            // Branch processing based on whether we want sync or async.
            if (isSync) {
                processMessageSync(topic, messageWrapper, rawMessage);
            } else {
                processMessageAsync(topic, messageWrapper, rawMessage);
            }
        } catch (Exception e) {
            log.error("Failed to deserialize/process message: {}", rawMessage, e);
        }
    }

/**
     * Synchronous processing for the sync topic.
     */

    private void processMessageSync(String topic, MessageWrapper<?> messageWrapper, String rawMessage) {
        String messageId = messageWrapper.getMessageId();
        Object payload = messageWrapper.getPayload();

        topicConfigurationRepository.findByTopicNameAndActive(topic, true).ifPresentOrElse(config -> {
            log.info("Processing sync message for active topic config: {}", config);
            try {
                // Synchronously call the API using WebClient.
                String response = webClientBuilder.build()
                        .post()
                        .uri(config.getApiUrl())
                        .bodyValue(payload)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                log.info("Successfully sent sync message to API: {}", response);

                processedMessageRepository.save(ProcessedMessage.builder()
                        .messageId(messageId)
                        .message(rawMessage)
                        .topicName(topic)
                        .status("PROCESSED")
                        .processedAt(LocalDateTime.now())
                        .build());
            } catch (Exception e) {
                log.error("Error in sync API call", e);
                updateFailedMessage(messageId, e.getMessage());
            }
        }, () -> {
            log.warn("No active configuration found for sync topic: {}", topic);
            updateFailedMessage(messageId, "No active configuration for topic: " + topic);
        });
    }

/**
     * Asynchronous processing for other topics.*/


    private void processMessageAsync(String topic, MessageWrapper<?> messageWrapper, String rawMessage) {
        String messageId = messageWrapper.getMessageId();
        Object payload = messageWrapper.getPayload();

        CompletableFuture.runAsync(() -> {
            topicConfigurationRepository.findByTopicNameAndActive(topic, true)
                    .ifPresentOrElse(config -> {
                        log.info("Processing async message for active topic config: {}", config);
                        try {
                            webClientBuilder.build()
                                    .post()
                                    .uri(config.getApiUrl())
                                    .bodyValue(payload)
                                    .retrieve()
                                    .bodyToMono(String.class)
                                    .doOnSuccess(response -> {
                                        log.info("Successfully sent async message to API: {}", response);
                                        processedMessageRepository.save(ProcessedMessage.builder()
                                                .messageId(messageId)
                                                .message(rawMessage)
                                                .topicName(topic)
                                                .status("PROCESSED")
                                                .processedAt(LocalDateTime.now())
                                                .build());
                                    })
                                    .doOnError(error -> log.error("Error in async API call", error))
                                    .subscribe();
                        } catch (Exception e) {
                            log.error("Error processing async message for topic: {}", topic, e);
                            updateFailedMessage(messageId, e.getMessage());
                        }
                    }, () -> {
                        log.warn("No active configuration found for async topic: {}", topic);
                        updateFailedMessage(messageId, "No active configuration for topic: " + topic);
                    });
        }).exceptionally(ex -> {
            log.error("Error processing async message for topic: {}, error: {}", topic, ex.getMessage());
            updateFailedMessage(messageId, ex.getMessage());
            return null;
        });
    }

    private void updateFailedMessage(String messageId, String errorMessage) {
        log.error("Marking message {} as FAILED. Error: {}", messageId, errorMessage);
        // Implement your FAILED status update here.
    }
}



/*

package com.rits.kafkaservice;

import com.rits.kafkapojo.MessageWrapper;
import com.rits.kafkapojo.ProcessedMessage;
import com.rits.kafkapojo.TopicConfiguration;
import com.rits.repository.ProcessedMessageRepository;
import com.rits.repository.TopicConfigurationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.kafka.support.Acknowledgment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class GenericKafkaListener {

    @Autowired
    private TopicConfigurationRepository topicConfigurationRepository;

    @Autowired
    private ProcessedMessageRepository processedMessageRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;


    // Inject the sync topic property
    @Value("${sync.topic}")
    private String syncTopic;


    @KafkaListener(
            topics = "#{'${kafka.topics}'.split(',')}",
            groupId = "generic-group",
            containerFactory = "manualAckListenerContainerFactory"
    )
    public void onMessage(@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                          @Header(KafkaHeaders.OFFSET) long offset,
                          @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                          Acknowledgment acknowledgment,
                          String rawMessage) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Deserialize the rawMessage into a MessageWrapper object
            MessageWrapper<?> messageWrapper = objectMapper.readValue(rawMessage, new TypeReference<MessageWrapper<?>>() {});
            String messageId = messageWrapper.getMessageId();
            Object payload = messageWrapper.getPayload();

            log.info("Processing message: topic={}, partition={}, offset={}, messageId={}, payload={}",
                    topic, partition, offset, messageId, payload);

            // Deduplication: Check if the message has already been processed
            if (processedMessageRepository.existsByMessageId(messageId)) {
                log.info("Duplicate message detected. Skipping processing for messageId: {}", messageId);
                acknowledgment.acknowledge();
                return;
            }

            // Save message status as PENDING
            processedMessageRepository.save(ProcessedMessage.builder()
                    .messageId(messageId)
                    .message(rawMessage)
                    .topicName(topic)
                    .status("PENDING")
                    .processedAt(LocalDateTime.now())
                    .build());

            // Check if the topic should be processed synchronously
            if (syncTopic.equals(topic)) {
                processMessageSync(topic, messageWrapper, rawMessage);
            } else {
                processMessageAsync(topic, messageWrapper, rawMessage);
            }

            // Acknowledge the message
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to deserialize message: {}", rawMessage, e);
        }
    }


*/
/**
     * Synchronous processing for the configured sync topic.
     *//*


    private void processMessageSync(String topic, MessageWrapper<?> messageWrapper, String rawMessage) {
        String messageId = messageWrapper.getMessageId();
        Object payload = messageWrapper.getPayload();

        // Retrieve active topic configuration
        topicConfigurationRepository.findByTopicNameAndActive(topic, true).ifPresentOrElse(config -> {
            log.info("Processing message for active topic configuration (sync): {}", config);
            try {
                // Synchronously call the API using WebClient and block until the response arrives.
                String response = webClientBuilder.build()
                        .post()
                        .uri(config.getApiUrl())
                        .bodyValue(payload)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                log.info("Successfully sent message to API (sync): {}", response);

                // Mark the message as processed.
                processedMessageRepository.save(ProcessedMessage.builder()
                        .messageId(messageId)
                        .message(rawMessage)
                        .topicName(topic)
                        .status("PROCESSED")
                        .processedAt(LocalDateTime.now())
                        .build());
            } catch (Exception e) {
                log.error("Error sending message to API (sync)", e);
                updateFailedMessage(messageId, e.getMessage());
            }
        }, () -> {
            log.warn("No active configuration found for topic: {}", topic);
            updateFailedMessage(messageId, "No active configuration for topic: " + topic);
        });
    }


*/
/**
     * Asynchronous processing for other topics.
     *//*


    private void processMessageAsync(String topic, MessageWrapper<?> messageWrapper, String rawMessage) {
        String messageId = messageWrapper.getMessageId();
        Object payload = messageWrapper.getPayload();

        CompletableFuture.runAsync(() -> {
            topicConfigurationRepository.findByTopicNameAndActive(topic, true)
                    .ifPresentOrElse(config -> {
                        log.info("Processing message for active topic configuration (async): {}", config);
                        try {
                            webClientBuilder.build()
                                    .post()
                                    .uri(config.getApiUrl())
                                    .bodyValue(payload)
                                    .retrieve()
                                    .bodyToMono(String.class)
                                    .doOnSuccess(response -> {
                                        log.info("Successfully sent message to API (async): {}", response);
                                        processedMessageRepository.save(ProcessedMessage.builder()
                                                .messageId(messageId)
                                                .message(rawMessage)
                                                .topicName(topic)
                                                .status("PROCESSED")
                                                .processedAt(LocalDateTime.now())
                                                .build());
                                    })
                                    .doOnError(error -> log.error("Error sending message to API (async)", error))
                                    .subscribe();
                        } catch (Exception e) {
                            log.error("Error occurred while processing message for topic (async): {}", topic, e);
                            updateFailedMessage(messageId, e.getMessage());
                        }
                    }, () -> {
                        log.warn("No active configuration found for topic: {}", topic);
                        updateFailedMessage(messageId, "No active configuration for topic: " + topic);
                    });
        }).exceptionally(ex -> {
            log.error("Error occurred while processing message asynchronously for topic: {}, error: {}", topic, ex.getMessage());
            updateFailedMessage(messageId, ex.getMessage());
            return null;
        });
    }

    // This method updates the status of a failed message.
    private void updateFailedMessage(String messageId, String errorMessage) {
        log.error("Marking message {} as FAILED. Error: {}", messageId, errorMessage);
        // Your implementation to mark the message as FAILED (e.g., updating the repository) goes here.
    }

}

*/
