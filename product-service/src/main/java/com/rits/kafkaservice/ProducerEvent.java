package com.rits.kafkaservice;


import com.rits.containermaintenanceservice.dto.AuditLogRequest;

public class ProducerEvent {
    private AuditLogRequest message;

    public ProducerEvent(AuditLogRequest message) {
        this.message = message;
    }

    public AuditLogRequest  getSendResult() {
        return message;
    }
}

