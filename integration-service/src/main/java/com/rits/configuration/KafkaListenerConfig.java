package com.rits.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.core.ConsumerFactory;


@Configuration
public class KafkaListenerConfig {

    @Bean
    @Primary
    public ConcurrentKafkaListenerContainerFactory<String, String> manualAckListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.getContainerProperties().setMissingTopicsFatal(false); // Optional: prevents startup failures for missing topics
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> syncListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        // Set the ack mode to manual if you want to control acknowledgment manually
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        // Set concurrency to 1 so that only one thread processes sync-topic messages
        factory.setConcurrency(1);
        return factory;
    }
}
