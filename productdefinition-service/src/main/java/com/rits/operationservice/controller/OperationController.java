package com.rits.operationservice.controller;

import com.rits.kafkaservice.ProducerEvent;
import com.rits.operationservice.dto.*;
import com.rits.operationservice.exception.OperationException;
import com.rits.operationservice.model.OperationMessageModel;
import com.rits.operationservice.model.Operation;
import com.rits.operationservice.service.OperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/operation-service")
public class OperationController {
    private final OperationService operationService;

    private final WebClient.Builder webClientBuilder;
    private final ApplicationEventPublisher eventPublisher;
    private final ApplicationContext context;
    @PostMapping("/shutdown")
    public void shutdown() {
        System.out.println("Shutting down...");
        SpringApplication.exit(context, () -> 1);
    }
    @Value("${auditlog-service.url}/producer")
    private String auditlogUrl;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public OperationMessageModel createOperation(@RequestBody OperationRequest operationRequest)  {

            try {
                OperationMessageModel operationMessageModel = operationService.createOperation(operationRequest);
                AuditLogRequest activityLog = operationService.createAuditLog(operationRequest);
                eventPublisher.publishEvent(new ProducerEvent(activityLog));
                return operationMessageModel;
            } catch (OperationException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

    }



    @PostMapping("/retrieveTop50")
    @ResponseStatus(HttpStatus.OK)
    public OperationResponseList getOperationListByCreationDate(@RequestBody OperationRequest operationRequest) {
        if(operationRequest.getSite()!=null && !operationRequest.getSite().isEmpty()){
            try {
                return operationService.getOperationListByCreationDate(operationRequest.getSite());
            } catch (OperationException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new OperationException(2303,operationRequest.getSite());
    }

    @PostMapping("/retrieveByOperation")
    @ResponseStatus(HttpStatus.OK)
    public OperationResponseList getOperationList(@RequestBody OperationRequest operationRequest) {
        if(operationRequest.getSite()!=null && !operationRequest.getSite().isEmpty()){
            try {
                return operationService.getOperationList(operationRequest.getSite(),operationRequest.getOperation());
            } catch (OperationException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new OperationException(2303,operationRequest.getSite());
    }



    @PostMapping("/retrieve")
    @ResponseStatus(HttpStatus.OK)
    public Operation retrieveOperation(@RequestBody OperationRequest operationRequest) {
        if(operationRequest.getSite()!=null && !operationRequest.getSite().isEmpty()){
            try {
                return operationService.retrieveOperation(operationRequest.getSite(),operationRequest.getOperation(),operationRequest.getRevision());
            } catch (OperationException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new OperationException(2303,operationRequest.getSite());
    }


    @PostMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public OperationMessageModel updateOperation(@RequestBody OperationRequest operationRequest) {
        if(operationRequest.getSite()!=null && !operationRequest.getSite().isEmpty()){
            try {
                OperationMessageModel operationMessageModel =operationService.updateOperation(operationRequest);
                AuditLogRequest activityLog = operationService.updateAuditLog(operationRequest);
                eventPublisher.publishEvent(new ProducerEvent(activityLog));
                return operationMessageModel;
            } catch (OperationException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new OperationException(2303,operationRequest.getSite());
    }



    @PostMapping("/isExist")
    @ResponseStatus(HttpStatus.OK)
    public Boolean isOperationExist(@RequestBody OperationRequest operationRequest) {
        if (operationRequest.getSite() != null && !operationRequest.getSite().isEmpty()) {
            try {
                return operationService.isOperationExist(operationRequest.getSite(),operationRequest.getOperation(), operationRequest.getRevision());
            } catch (OperationException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new OperationException(2303,operationRequest.getSite());
    }
    @PostMapping("/isExistByHandle")
    @ResponseStatus(HttpStatus.OK)
    public Boolean isOperationExistByHandle(@RequestBody OperationRequest operationRequest) {
        if (operationRequest.getSite() != null && !operationRequest.getSite().isEmpty()) {
            try {
                return operationService.isOperationExistByHandle(operationRequest.getSite(),operationRequest.getOperation());
            } catch (OperationException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new OperationException(2303,operationRequest.getSite());
    }


    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    public OperationMessageModel deleteOperation(@RequestBody OperationRequest operationRequest){
        if(operationRequest.getSite()!=null && !operationRequest.getSite().isEmpty()){
            try {
                OperationMessageModel response= operationService.deleteOperation(operationRequest.getOperation(),operationRequest.getRevision(), operationRequest.getSite(), operationRequest.getUserId());
                AuditLogRequest activityLog = operationService.deleteAuditLog(operationRequest);
                eventPublisher.publishEvent(new ProducerEvent(activityLog));
                return response;
            } catch (OperationException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new OperationException(2303,operationRequest.getSite());
    }


    @PostMapping("retrieveByErpOperation")
    public OperationResponseList getOperationListByErpOperation(@RequestBody OperationRequest operationRequest)
    {
        try {
            return operationService.getOperationListByErpOperation(operationRequest.getSite());
        } catch (OperationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("retrieveBySite")
    public OperationResponseList getAllOperation(@RequestBody OperationRequest operationRequest)
    {
        try {
            return operationService.getAllOperation(operationRequest.getSite());
        } catch (OperationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/retrieveOperationByCurrentVersion")
    @ResponseStatus(HttpStatus.OK)
    public OperationResponse retrieveOperationByCurrentVersion(@RequestBody OperationRequest operationRequest) {
        if(operationRequest.getSite()!=null && !operationRequest.getSite().isEmpty()){
            try {
                return operationService.retrieveOperationByCurrentVersion(operationRequest.getSite(),operationRequest.getOperation());
            } catch (OperationException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new OperationException(2303,operationRequest.getSite());
    }

//    @PostMapping("retrieveCertificateList")
//    public List<Operation> retrieveCertificateList(@RequestBody Operation operation)
//    {
//        try {
//            return operationService.retrieveCertificateList(operation);
//        } catch (OperationException e) {
//            throw e;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//    @PostMapping("getResourceListByOp")
//    public List<AllResourceResponse> getResourceListByOp(@RequestBody Operation operation)
//    {
//        try {
//            return operationService.getResourceListByOp(operation.getSite(),operation.getOperation(), operation.getStoredUrlPodName());
//        } catch (OperationException e) {
//            throw e;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
    @PostMapping("/retrieveByOperationAndSite")
    @ResponseStatus(HttpStatus.OK)
    public Operation retrieveByOperationAndSite(@RequestBody OperationRequest operationRequest) {
        if(operationRequest.getSite()!=null && !operationRequest.getSite().isEmpty()){
            try {
                return operationService.retrieveByOperationAndSite(operationRequest.getSite(),operationRequest.getOperation());
            } catch (OperationException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new OperationException(2303,operationRequest.getSite());
    }

    @PostMapping("/retrieveOperationsBySite")
    public List<Operation> getOperationsBySite(@RequestBody OperationRequest operationRequest){
        try {
            return operationService.getOperationsBySite(operationRequest);
        } catch (OperationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }





}