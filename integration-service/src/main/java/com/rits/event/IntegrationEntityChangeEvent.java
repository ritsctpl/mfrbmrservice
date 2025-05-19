package com.rits.event;

import org.springframework.context.ApplicationEvent;
import com.rits.integration.model.IntegrationEntity;

public class IntegrationEntityChangeEvent extends ApplicationEvent {

    private final IntegrationEntity integrationEntity;

    public IntegrationEntityChangeEvent(Object source, IntegrationEntity integrationEntity) {
        super(source);
        this.integrationEntity = integrationEntity;
    }

    public IntegrationEntity getIntegrationEntity() {
        return integrationEntity;
    }
}
