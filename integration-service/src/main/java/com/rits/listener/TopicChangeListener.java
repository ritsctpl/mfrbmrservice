package com.rits.listener;

import com.rits.event.IntegrationEntityChangeEvent;
import com.rits.integration.model.IntegrationEntity;
import com.rits.integration.repository.IntegrationRepository;
import com.rits.service.ConsumerManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class TopicChangeListener implements ApplicationListener<IntegrationEntityChangeEvent> {

    @Autowired
    private ConsumerManagementService consumerManagementService;

    @Autowired
    private IntegrationRepository integrationRepository;

    /*@Override
    public void onApplicationEvent(IntegrationEntityChangeEvent event) {
        IntegrationEntity integrationEntity = event.getIntegrationEntity();

        if (integrationEntity != null) {
            String topic = integrationEntity.getIdentifier();
            if (integrationEntity.getType().equals("ADD")) {
                // Add a new consumer for the topic
                consumerManagementService.addConsumerForTopic(topic);
            } else if (integrationEntity.getType().equals("DELETE")) {
                // Stop the consumer for the deleted topic
                consumerManagementService.stopConsumer(topic);
            } else if (integrationEntity.getType().equals("UPDATE")) {
                // Update the consumer for the topic
                consumerManagementService.restartConsumer(topic);
            }
        }
    }*/
    @Override
    public void onApplicationEvent(IntegrationEntityChangeEvent event) {
        IntegrationEntity integrationEntity = event.getIntegrationEntity();

        if (integrationEntity != null) {
            String topic = integrationEntity.getIdentifier();
            String operationType = integrationEntity.getOperationType();

            switch (operationType) {
                case "CREATE":
                    consumerManagementService.addConsumerForTopic(topic);
                    break;
                case "DELETE":
                    consumerManagementService.stopConsumer(topic);
                    break;
                case "UPDATE":
                    consumerManagementService.restartConsumer(topic);
                    break;
                default:
                    System.out.println("Unknown operation type: " + operationType);
            }
        }
    }

}

/*
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.integration.model.IntegrationEntity;
import com.rits.service.ConsumerManagementService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.data.mongodb.core.mapping.event.AfterDeleteEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.stereotype.Component;

@Component
public class TopicChangeListener implements ApplicationListener<Object> {

    @Autowired
    private ConsumerManagementService consumerManagementService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onApplicationEvent(Object event) {
        try {
            if (event instanceof AfterSaveEvent) {
                // Handling addition or update of IntegrationEntity
                AfterSaveEvent<IntegrationEntity> saveEvent = (AfterSaveEvent<IntegrationEntity>) event;
                IntegrationEntity savedEntity = saveEvent.getSource();
                if (savedEntity != null && savedEntity.getIdentifier() != null) {
                    consumerManagementService.startOrUpdateConsumer(savedEntity.getIdentifier());
                }
            } else if (event instanceof AfterDeleteEvent) {
                // Handling deletion of IntegrationEntity
                AfterDeleteEvent<IntegrationEntity> deleteEvent = (AfterDeleteEvent<IntegrationEntity>) event;
                Document deletedDocument = deleteEvent.getSource();

                // Convert Document to IntegrationEntity
                IntegrationEntity deletedEntity = objectMapper.readValue(deletedDocument.toJson(), IntegrationEntity.class);
                if (deletedEntity != null && deletedEntity.getIdentifier() != null) {
                    consumerManagementService.stopConsumer(deletedEntity.getIdentifier());
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
*/
