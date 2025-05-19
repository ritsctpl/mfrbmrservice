package com.rits.pco.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.pco.service.KafkaMessageService;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaMessageServiceImpl implements KafkaMessageService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    public KafkaMessageServiceImpl(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void sendMessage(String topic, String key, String message) {
        try {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, message);
            record.headers().add(new RecordHeader("source", "fenta-test".getBytes()));

            kafkaTemplate.send(record);
            System.out.println("✅ Message sent to Kafka topic: " + topic);
        } catch (Exception e) {
            System.err.println("❌ Error sending message to Kafka: " + e.getMessage());
        }
    }


    @Override
    public void sendMessageApi(String topic, String key, Object message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, jsonMessage);
            record.headers().add(new RecordHeader("source", "fenta-test".getBytes()));

            kafkaTemplate.send(record);
            System.out.println("✅ Message sent to Kafka topic: " + topic);
        } catch (Exception e) {
            System.err.println("❌ Error sending message to Kafka: " + e.getMessage());
        }
    }
    /**
     * ✅ Kafka Listener for fenta-response topic
     */
   /* @KafkaListener(topics = "fenta-response", groupId = "rits-group")
    public void listenFentaResponse(String message) {
        System.out.println("📥 Received message from fenta-response: " + message);

        // You can further process the message here
    }*/

    /**
     * ✅ Kafka Listener for fenta-pco-agent-response topic
     */
    @KafkaListener(topics = "fenta-pco-agent-response", groupId = "rits-group")
    public void listenPcoAgentResponse(String message) {
        System.out.println("📥 Received message from fenta-pco-agent-response: " + message);
    }
}
