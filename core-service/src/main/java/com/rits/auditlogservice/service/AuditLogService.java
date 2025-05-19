package com.rits.auditlogservice.service;

import com.rits.auditlogservice.dto.AuditLogRequest;
import com.rits.auditlogservice.model.AuditLog;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface AuditLogService {
    public Boolean log(@RequestBody AuditLogRequest activityLogRequest)  throws Exception;
    public void producer(AuditLogRequest activityLog) throws Exception;
    public List<AuditLog> getAuditlogByUser(AuditLogRequest actvityLogRequest);
    public List<AuditLog> getAuditlogByCatagory(AuditLogRequest actvityLogRequest);
    public List<AuditLog> getAuditlogByUserAndCatagory(AuditLogRequest actvityLogRequest);

    List<AuditLog> getAuditLogs(AuditLogRequest auditLogRequest);
}
