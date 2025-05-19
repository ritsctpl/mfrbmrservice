package com.rits.operationservice.service;

import com.rits.operationservice.dto.*;
import com.rits.operationservice.model.OperationMessageModel;
import com.rits.operationservice.model.Operation;

import java.util.List;

public interface OperationService {
    public OperationMessageModel createOperation(OperationRequest operationRequest) throws Exception;
    public OperationResponseList getOperationListByCreationDate(String site) throws Exception;
    public OperationResponseList getOperationList(String site,String operation) throws Exception;
    public Operation retrieveOperation(String site,String operation,String revision) throws Exception;
    public OperationMessageModel updateOperation(OperationRequest operationRequest) throws Exception ;
    public OperationMessageModel deleteOperation(String operation,String revision,String site,String userId) throws Exception;
    public Boolean isOperationExist(String site,String operation,String revision) throws Exception;
    public Boolean isOperationExistByHandle(String site,String operation) throws Exception;
    public OperationResponseList getOperationListByErpOperation(String site) throws Exception;
    public OperationResponseList getAllOperation(String site) throws Exception;

    AuditLogRequest createAuditLog(OperationRequest operationRequest);

    AuditLogRequest updateAuditLog(OperationRequest operationRequest);

    AuditLogRequest deleteAuditLog(OperationRequest operationRequest);

    OperationResponse retrieveOperationByCurrentVersion(String site, String operation) throws Exception;

//    public List<Operation> retrieveCertificateList(Operation operation);
//
//    public List<AllResourceResponse> getResourceListByOp(String site, String operation, String storedUrlPodName);
    public Operation retrieveByOperationAndSite(String site,String operation) throws Exception;

    List<Operation> getOperationsBySite(OperationRequest operationRequest);
}
