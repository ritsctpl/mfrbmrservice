package com.rits.auditlogservice.service;

import com.rits.auditlogservice.dto.AuditLogRequest;

public class ProducerEvent {
    private AuditLogRequest message;

    public ProducerEvent(AuditLogRequest message) {
        this.message = message;
    }

    public AuditLogRequest  getSendResult() {
        return message;
    }
}

