package com.rits.service;

import com.rits.integration.model.CustomResponseEntity;
import com.rits.integration.model.IntegrationEntity;
import com.rits.integration.repository.IntegrationRepository;
import com.rits.integration.repository.ResponseRepository;
import com.rits.service.MessageRouter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConsumerManagementService {

    @Autowired
    private ConsumerFactory<String, String> consumerFactory;

    @Autowired
    private IntegrationRepository integrationRepository;

    @Autowired
    private MessageRouter messageRouter;

    @Autowired
    private ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory;

    @Autowired
    private ResponseRepository responseRepository;

    private final Map<String, ConcurrentMessageListenerContainer<String, String>> listenerContainers = new HashMap<>();

    /*@PostConstruct
    public void initializeConsumers() {
        List<IntegrationEntity> entities = integrationRepository.findAll();
        List<String> topics = IntegrationEntity.getAllUniqueIdentifiers(entities);

        for (String topic : topics) {
            addConsumerForTopic(topic);
        }
    }*/
    @PostConstruct
    public void initializeConsumers() {
        List<IntegrationEntity> entities = integrationRepository.findAll();

        if (entities == null || entities.isEmpty()) {
            System.err.println("No topics found. Adding default topic.");
            addConsumerForTopic("genericTopic"); // Ensure at least one default consumer is created
            return;  // Skip further processing if the database is empty
        }

        List<String> topics = IntegrationEntity.getAllUniqueIdentifiers(entities);

        if (topics == null || topics.isEmpty()) {
            System.err.println("No topics found. Adding default topic.");
            addConsumerForTopic("defaultProcess"); // Fallback if no topics are found
            return;
        }

        for (String topic : topics) {
            addConsumerForTopic(topic);
        }
    }



    public void addConsumerForTopic(String topic) {
        if (listenerContainers.containsKey(topic)) {
            return; // Consumer already exists for the topic
        }

        ConcurrentMessageListenerContainer<String, String> container = createListenerContainer(topic);

        // Start the container and add it to the listener containers map
        container.start();
        listenerContainers.put(topic, container);
    }

    public void stopConsumer(String topic) {
        ConcurrentMessageListenerContainer<String, String> container = listenerContainers.get(topic);
        if (container != null) {
            container.stop();
            listenerContainers.remove(topic);
        }
    }

    public void restartConsumer(String topic) {
        stopConsumer(topic);
        addConsumerForTopic(topic);
    }

    public void stopAllConsumers() {
        listenerContainers.values().forEach(ConcurrentMessageListenerContainer::stop);
    }

    private ConcurrentMessageListenerContainer<String, String> createListenerContainer(String topic) {
        kafkaListenerContainerFactory.setConsumerFactory(consumerFactory);
        kafkaListenerContainerFactory.getContainerProperties().setAckMode(org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL);

        ConcurrentMessageListenerContainer<String, String> container = kafkaListenerContainerFactory.createContainer(topic);

        // Set a message listener with acknowledgment handling
        container.setupMessageListener((AcknowledgingMessageListener<String, String>) (record, acknowledgment) -> {
            try {
                processMessage(record, acknowledgment);
            } catch (Exception e) {
                System.err.println("Error processing message from topic " + record.topic() + ": " + e.getMessage());
                e.printStackTrace();
            }
        });

        container.setConcurrency(3); // Set concurrency level, adjust as needed
        return container;
    }

    private void processMessage(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            String topic = record.topic();
            String messageBody = record.value();

            // Fetch the IntegrationEntity for the topic
            IntegrationEntity integrationEntity = integrationRepository.findByIdentifier(topic);

            if (integrationEntity != null) {
                // Delegate the message to MessageRouter for further processing
                messageRouter.routeMessage(messageBody, integrationEntity);
            } else {
                CustomResponseEntity customResponseEntity = new CustomResponseEntity();
                customResponseEntity.setSite(integrationEntity.getSite());
                customResponseEntity.setIdentifier(integrationEntity.getIdentifier());
                customResponseEntity.setInput(messageBody);
                customResponseEntity.setStatusMessage("Unsupported message - No Workflow Defined");
                customResponseEntity.setStatus("Fail");
                responseRepository.save(customResponseEntity);
                System.err.println("No IntegrationEntity found for topic: " + topic);
            }
            // Acknowledge the message only after successful processing
            acknowledgment.acknowledge();
        } catch (Exception e) {
            // Handle exceptions if any issue occurs during message processing
            System.err.println("Error processing message from topic " + record.topic() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}


/*
package com.rits.service;


import com.rits.integration.model.IntegrationEntity;
import com.rits.integration.repository.IntegrationRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConsumerManagementService {

    @Autowired
    private ConsumerFactory<String, String> consumerFactory;

    @Autowired
    private IntegrationRepository integrationRepository;

    @Autowired
    private MessageProcessingService messageProcessingService;  // Injecting MessageProcessingService

    @Autowired
    private ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory;

    private final Map<String, ConcurrentMessageListenerContainer<String, String>> listenerContainers = new HashMap<>();

    @PostConstruct
    public void initializeConsumers() {
        List<IntegrationEntity> entities = integrationRepository.findAll();
        List<String> topics = IntegrationEntity.getAllUniqueIdentifiers(entities);

        for (String topic : topics) {
            addConsumerForTopic(topic);
        }
    }

    public void addConsumerForTopic(String topic) {
        if (listenerContainers.containsKey(topic)) {
            return; // Consumer already exists for the topic
        }

        // Define container properties
        ContainerProperties containerProps = new ContainerProperties(topic);
        containerProps.setMessageListener((MessageListener<String, String>) record -> processMessage(record));

        // Create a new ConcurrentMessageListenerContainer instance using the container properties
        ConcurrentMessageListenerContainer<String, String> container = kafkaListenerContainerFactory.createContainer(topic);
        container.getContainerProperties().setMessageListener((MessageListener<String, String>) record -> processMessage(record));

        // Set concurrency level if necessary
        container.setConcurrency(3); // You can adjust the concurrency level as per your needs

        // Start the container and add it to the listener containers map
        container.start();
        listenerContainers.put(topic, container);
    }

    private void processMessage(ConsumerRecord<String, String> record) {
        // Process the Kafka message using MessageProcessingService
        try {
            String topic = record.topic();
            String messageBody = record.value();

            // Delegate the message to MessageProcessingService for further processing
            messageProcessingService.processMessage(messageBody, topic);
        } catch (Exception e) {
            // Handle exceptions if any issue occurs during message processing
            System.err.println("Error processing message from topic " + record.topic() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stopAllConsumers() {
        listenerContainers.values().forEach(ConcurrentMessageListenerContainer::stop);
    }

    public void stopConsumer(String topic) {
        ConcurrentMessageListenerContainer<String, String> container = listenerContainers.get(topic);
        if (container != null) {
            container.stop();
            listenerContainers.remove(topic);
        }
    }

    public void restartConsumer(String topic) {
        stopConsumer(topic);
        addConsumerForTopic(topic);
    }
}


*/
/*
import com.rits.integration.model.IntegrationEntity;
import com.rits.integration.repository.IntegrationRepository;
import com.rits.service.MessageProcessingService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConsumerManagementService {

    @Autowired
    private ConsumerFactory<String, String> consumerFactory;

    @Autowired
    private IntegrationRepository integrationRepository;

    @Autowired
    private MessageProcessingService messageProcessingService;  // Injecting MessageProcessingService

    private final Map<String, ConcurrentMessageListenerContainer<String, String>> listenerContainers = new HashMap<>();

    public void initializeConsumers() {
        List<IntegrationEntity> entities = integrationRepository.findAll();
        List<String> topics = IntegrationEntity.getAllUniqueIdentifiers(entities);

        for (String topic : topics) {
            addConsumerForTopic(topic);
        }
    }

    public void addConsumerForTopic(String topic) {
        // Define container properties
        ContainerProperties containerProps = new ContainerProperties(topic);
        containerProps.setMessageListener((MessageListener<String, String>) record -> processMessage(record));

        // Create a new ConcurrentMessageListenerContainer instance
        ConcurrentMessageListenerContainer<String, String> container =
                new ConcurrentMessageListenerContainer<>(consumerFactory, containerProps);

        // Set concurrency directly
        container.setConcurrency(3); // You can adjust the concurrency level as per your needs

        // Start the container and add it to the listener containers map
        container.start();
        listenerContainers.put(topic, container);
    }

    private void processMessage(ConsumerRecord<String, String> record) {
        // Process the Kafka message using MessageProcessingService
        try {
            String topic = record.topic();
            String messageBody = record.value();

            // Delegate the message to MessageProcessingService for further processing
            messageProcessingService.processMessage(messageBody, topic);
        } catch (Exception e) {
            // Handle exceptions if any issue occurs during message processing
            System.err.println("Error processing message from topic " + record.topic() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stopAllConsumers() {
        listenerContainers.values().forEach(ConcurrentMessageListenerContainer::stop);
    }

    public void stopConsumer(String topic) {
        ConcurrentMessageListenerContainer<String, String> container = listenerContainers.get(topic);
        if (container != null) {
            container.stop();
            listenerContainers.remove(topic);
        }
    }
}
*/
