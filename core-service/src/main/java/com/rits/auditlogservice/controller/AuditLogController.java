package com.rits.auditlogservice.controller;

import com.rits.auditlogservice.dto.AuditLogRequest;
import com.rits.auditlogservice.exception.AuditLogException;
import com.rits.auditlogservice.model.AuditLog;
import com.rits.auditlogservice.service.AuditLogServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/auditlog-service")

@Configuration
public class AuditLogController {
    private final AuditLogServiceImpl auditLogService;
    private static final Logger logger = LoggerFactory.getLogger(AuditLogController.class);
    @Value("${DOCKER_KAFKA_HOST:localhost}")
    private String dockerKafkaHost;

    @Value("${DOCKER_KAFKA_PORT:9092}")
    private String dockerKafkaPort;
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, dockerKafkaHost+":"+dockerKafkaPort);
        logger.info("Using Docker Kafka host IP: {}", dockerKafkaHost+":"+dockerKafkaPort);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "log-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
    @PostMapping("retrieve")
    public List<AuditLog> getauditLogByUser(@RequestBody AuditLogRequest auditLogRequest) {
        try {
            List<AuditLog> auditLogs = auditLogService.getAuditLogs(auditLogRequest);
            return auditLogs;
        } catch (AuditLogException auditLogException) {
            throw auditLogException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("producer")
    public void producer(@RequestBody AuditLogRequest auditLogRequest) throws Exception{
        auditLogService.producer(auditLogRequest);
    }
}
