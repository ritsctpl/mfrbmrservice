package com.rits.kafkaservice;

import com.rits.operationservice.dto.AuditLogRequest;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ProducerEventListener {

    @Value("${DOCKER_KAFKA_HOST:localhost}")
    private String dockerKafkaHost;
    @EventListener
    public void handleProducerSuccess(ProducerEvent producerEvent) {
        AuditLogRequest message = producerEvent.getSendResult();
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, dockerKafkaHost+":9092");
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "com.rits.kafkaservice.PojoSerializer"); // Use your custom serializer

        try {
            KafkaProducer<String, AuditLogRequest> producer = new KafkaProducer<>(producerProps);
            ProducerRecord<String, AuditLogRequest> record = new ProducerRecord<>(message.getTopic(), message);
            producer.send(record);
            producer.close();
        } catch (Exception e) {
            // Handle any exceptions that may occur
            e.printStackTrace(); // You can replace this with proper error handling
        }
    }













































































































































}


