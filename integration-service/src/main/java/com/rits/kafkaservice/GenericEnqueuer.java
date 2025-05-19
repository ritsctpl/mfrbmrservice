package com.rits.kafkaservice;
import com.rits.kafkapojo.MessageWrapper;
import io.micrometer.observation.Observation;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.kafka.support.SendResult;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import java.util.UUID;
@RequiredArgsConstructor
@Slf4j
@Service
public class GenericEnqueuer {

  //  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final KafkaTemplate<String, MessageWrapper<?>> kafkaMessageWrapperTemplate;
 private final ObservationRegistry observationRegistry;

    public void enqueueMessage(String topicName, Object message) {
        String messageId = UUID.randomUUID().toString(); // Generate unique message ID
        log.info("Enqueuing message to topic: {}, messageId: {}, payload: {}", topicName, messageId, message);

        // Wrap the message with `messageId`
        var messageWithId = new MessageWrapper(messageId, message);

        kafkaMessageWrapperTemplate.send(topicName, messageWithId);
    }


    public <T> boolean logGenericMessage(String topicName, T message) {
        log.info("Logging generic message to topic: {}, message: {}", topicName, message);

        String messageId = UUID.randomUUID().toString(); // Generate a unique message ID
        MessageWrapper<T> messageWithId = new MessageWrapper<>(messageId, message); // Wrap the message

        try {
            Observation.createNotStarted(topicName, this.observationRegistry).observeChecked(() -> {
                // Send the wrapped message to Kafka
                CompletableFuture<SendResult<String, MessageWrapper<?>>> completableFuture =
                        FutureConverter.toCompletableFuture(kafkaMessageWrapperTemplate.send(topicName, messageWithId));

                return completableFuture.handle((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Error while sending message to Kafka topic: {}, message: {}, error: {}", topicName, message, throwable.getMessage());
                        throw new RuntimeException("Kafka message send failed", throwable);
                    }

                    log.info("Kafka message successfully sent to topic: {}, messageId: {}", topicName, messageId);
                    return CompletableFuture.completedFuture(result);
                });
            }).get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Error during Kafka message publishing: {}, topic: {}", message, topicName, e);
            throw new RuntimeException("Error while publishing Kafka message", e);
        }
    }


/*


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MessageWrapper {
        private String messageId;
        private Object payload;
    }*/
}
