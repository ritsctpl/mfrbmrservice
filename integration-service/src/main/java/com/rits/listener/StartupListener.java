package com.rits.listener;

import com.rits.integration.repository.IntegrationRepository;
import com.rits.service.ConsumerManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class StartupListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private ConsumerManagementService consumerManagementService;

    @Autowired
    private IntegrationRepository integrationRepository;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // Initialize consumers for all topics from the IntegrationRepository
        consumerManagementService.initializeConsumers();
    }
}
