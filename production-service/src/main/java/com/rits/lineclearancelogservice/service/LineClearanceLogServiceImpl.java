package com.rits.lineclearancelogservice.service;

import com.rits.Utility.ProcessOrderUtility;
import com.rits.lineclearancelogservice.dto.LineClearanceLogRequest;
import com.rits.lineclearancelogservice.dto.LineClearanceLogRequestList;
import com.rits.lineclearancelogservice.exception.LineClearanceLogException;
import com.rits.lineclearancelogservice.model.*;
import com.rits.lineclearancelogservice.repository.LineClearanceLogRepository;
import com.rits.lineclearanceservice.dto.RetrieveLineClearanceLogRequest;
import com.rits.lineclearanceservice.model.RetrieveLineClearanceLogResponse;
import com.rits.lineclearanceservice.service.LineClearanceService;
import com.rits.logbuyoffservice.dto.LogbuyOffRequest;
import com.rits.logbuyoffservice.exception.LogBuyOffException;
import com.rits.productionlogservice.dto.ProductionLogRequest;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.asm.Advice;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


import java.util.*;


@Service
@RequiredArgsConstructor
public class LineClearanceLogServiceImpl implements LineClearanceLogService {

    private final LineClearanceLogRepository lineClearanceLogRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private LineClearanceService lineClearanceService;

    private final WebClient.Builder webClientBuilder;

    @Value("${oee-calculation-service.url}/downtime/logmachinelog")
    private String logmachinelog;

    ZonedDateTime currentZonedDateTime = ZonedDateTime.now();

    // Method to validate request
    private void validateRequest(LineClearanceLogRequest request) {

        if (request == null) {
            throw new LineClearanceLogException(311);
        }
        if (request.getEvidenceRequired() != null && request.getEvidenceRequired()) {
            if (request.getEvidence() == null || request.getEvidence().isEmpty() || request.getEvidence().isBlank()) {
                throw new LineClearanceLogException(312); // Throw an error if evidence is missing
            }
        }
        if (request.getBatchNo() == null || request.getBatchNo().isEmpty()) {
            throw new LineClearanceLogException(8001);
        }
        if (request.getTempleteName() == null || request.getTempleteName().isEmpty()) {
            throw new LineClearanceLogException(313);
        }
    }



    @Override
    public MessageResponse startLineClearanceLog(LineClearanceLogRequestList lineClearanceLogRequestList) throws Exception {
        StringBuilder batchNoMessage = new StringBuilder();
        List<LineClearanceLogRequest> lineClearanceLogRequests = lineClearanceLogRequestList.getLineClearanceLogRequestList();

        if (lineClearanceLogRequests == null || lineClearanceLogRequests.isEmpty()) {
            throw new LineClearanceLogException(354);
        }
        for (LineClearanceLogRequest request : lineClearanceLogRequests) {
            LineClearanceLog exisitnglineClearance = lineClearanceLogRepository.findBySiteAndTempleteNameAndBatchNoAndOperationAndPhaseAndTaskNameAndResourceId(request.getSite(), request.getTempleteName(), request.getBatchNo(), request.getOperation(), request.getPhase(), request.getTaskName(), request.getResourceId());
            if (exisitnglineClearance!=null && !"New".equalsIgnoreCase(exisitnglineClearance.getStatus())) continue;
            if ((request.getUserId() == null) || request.getUserId().isEmpty()) {
                throw new LineClearanceLogException(8004, request.getBatchNo());
            }
            // Validate request parameters and check permission
            if (request.getTempleteName() != null && request.getStatus() != null &&
                    !lineClearanceService.checkPermission(request.getTempleteName(), request.getSite(), 1, request.getUserId(), request.getStatus())) {
                throw new LineClearanceLogException(325); // Permission Denied
            }
//        if (request.getEvidenceRequired() != null && request.getEvidenceRequired()) {
//            if (request.getEvidence() == null || request.getEvidence().isEmpty() || request.getEvidence().isBlank()) {
//                throw new LineClearanceLogException(312); // Throw an error if evidence is missing
//            }
//        }

            String handle = createHandle(request);

            // Check if a LineClearanceLog with the same handle already exists
            LineClearanceLog existingLog = lineClearanceLogRepository.findBySiteAndHandleAndResourceIdAndActive(request.getSite(), handle, request.getResourceId(), 1);
            if (existingLog != null) {
//            throw new LineClearanceLogException(327); // Handle already exists in the database
                if (existingLog.getStatus().equalsIgnoreCase("new")) {
                    LineClearanceLog newLog = buildLineClearanceLog(request, handle, request.getStatus());
                    newLog.setCreatedDateTime(LocalDateTime.now());
                    newLog.setStartedDateTime(LocalDateTime.now());
                    newLog.setStartedBy(request.getUserId());
                    newLog.setCreatedBy(request.getUserId());
                    // Save the new log entry to the repository
                    lineClearanceLogRepository.save(newLog);
                    logMachine(request.getSite(),request.getWorkCenterId(),request.getResourceId(),request.getReason(),"SCHEDULED_DOWN",request.getUserId());
                    newLog.setItem(request.getItem());
                    newLog.setItemVersion(request.getItemVersion());
                    newLog.setQuantity(request.getQuantity());
                    newLog.setOrderNumber(request.getOrderNumber());
                    productionLog(newLog);
                } else {
                    throw new LineClearanceLogException(331);
                }
            } else {

                // Use the builder method to create a new log with status "New"
                LineClearanceLog newLog = buildLineClearanceLog(request, handle, request.getStatus());
                newLog.setCreatedDateTime(LocalDateTime.now());
                newLog.setStartedDateTime(LocalDateTime.now());
                newLog.setStartedBy(request.getUserId());
                newLog.setCreatedBy(request.getUserId());
                // Save the new log entry to the repository
                lineClearanceLogRepository.save(newLog);
                logMachine(request.getSite(),request.getWorkCenterId(),request.getResourceId(),request.getReason(),"SCHEDULED_DOWN",request.getUserId());
                newLog.setItem(request.getItem());
                newLog.setItemVersion(request.getItemVersion());
                newLog.setQuantity(request.getQuantity());
                newLog.setOrderNumber(request.getOrderNumber());
                productionLog(newLog);
            }
            batchNoMessage.append(request.getBatchNo()).append(",");
        }
        if (batchNoMessage.length() > 0) {
            batchNoMessage.setLength(batchNoMessage.length() - 1);
        }
        if (batchNoMessage.length() == 0)throw new LineClearanceLogException(331);


        // Return success message
        return new MessageResponse("For the Batch " + batchNoMessage+ " Line Clearance started successfully.");
    }

    @Override
    public MessageResponse completeLineClearanceLog(LineClearanceLogRequestList lineClearanceLogRequestList) {
        StringBuilder batchNoMessage = new StringBuilder();
        List<LineClearanceLogRequest> lineClearanceLogRequests = lineClearanceLogRequestList.getLineClearanceLogRequestList();

        if (lineClearanceLogRequests == null || lineClearanceLogRequests.isEmpty()) {
            throw new LineClearanceLogException(354);
        }
        for (LineClearanceLogRequest request : lineClearanceLogRequests) {

            // Fetch the line clearance log by batch number and template name
            LineClearanceLog exisitnglineClearance = lineClearanceLogRepository.findBySiteAndTempleteNameAndBatchNoAndOperationAndPhaseAndTaskNameAndResourceId(request.getSite(), request.getTempleteName(), request.getBatchNo(), request.getOperation(), request.getPhase(), request.getTaskName(),request.getResourceId());


            if (exisitnglineClearance == null) {
                throw new LineClearanceLogException(332, request.getBatchNo());  // Batch ID Not Exist
            }
            if ((request.getUserId() == null) || request.getUserId().isEmpty()) {
                throw new LineClearanceLogException(8004, request.getBatchNo());
            }

            if (request.getTempleteName() != null && request.getUserId() != null && request.getStatus() != null &&
                    !lineClearanceService.checkPermission(request.getTempleteName(), request.getSite(), 1, request.getUserId(), request.getStatus())) {
                throw new LineClearanceLogException(325);
            }

            if (exisitnglineClearance.getStatus().equalsIgnoreCase("complete")) {
                continue;
//                return new MessageResponse("Line clearance for Batch " + batchno + " is already completed.");
            }

            if (!exisitnglineClearance.getStatus().equalsIgnoreCase("Start")) {
                throw new LineClearanceLogException(332);
            }

            if (request.getEvidenceRequired() != null && request.getEvidenceRequired()) {
                if (request.getEvidence() == null || request.getEvidence().isEmpty() || request.getEvidence().isBlank()) {
                    throw new LineClearanceLogException(312); // Throw an error if evidence is missing
                }
            }


            // Proceed to mark the line clearance as "complete"
            exisitnglineClearance.setStatus(request.getStatus());
            exisitnglineClearance.setEvidence(request.getEvidence());
            exisitnglineClearance.setCreatedBy(exisitnglineClearance.getCreatedBy());
            exisitnglineClearance.setStartedBy(exisitnglineClearance.getStartedBy());
            exisitnglineClearance.setStartedDateTime(exisitnglineClearance.getCreatedDateTime());
            exisitnglineClearance.setCreatedDateTime(exisitnglineClearance.getStartedDateTime());
            exisitnglineClearance.setCompletedBy(request.getUserId());
            exisitnglineClearance.setCompletedDateTime(LocalDateTime.now());
            exisitnglineClearance.setUpdatedBy(request.getUserId());
            exisitnglineClearance.setUpdatedDateTime(LocalDateTime.now());
            exisitnglineClearance.setEvidence(request.getEvidence());
            exisitnglineClearance.setReason(request.getReason());
            exisitnglineClearance.setActive(1);  // Mark as active after completion
            exisitnglineClearance.setModifiedBy(request.getUserId());
            exisitnglineClearance.setModifiedDateTime(LocalDateTime.now());
            exisitnglineClearance.setApproved(true);

            // Save the updated log
            lineClearanceLogRepository.save(exisitnglineClearance);
            logMachine(request.getSite(),request.getWorkCenterId(),request.getResourceId(),request.getReason(),"PRODUCTION",request.getUserId());
            batchNoMessage.append(request.getBatchNo()).append(",");
        }
        if (batchNoMessage.length() > 0) {
            batchNoMessage.setLength(batchNoMessage.length() - 1);
        }
        if (batchNoMessage.length() == 0) return new MessageResponse("Line clearance for this Batch already completed.");


        // Return success message for completed line clearance
        return new MessageResponse("For the Batch " + batchNoMessage + " Line Clearance completed successfully.");
    }

    @Override
    public MessageResponse rejectLineClearanceLog(LineClearanceLogRequest request) {

        // Validate the incoming request
        if (request == null) {
            throw new LineClearanceLogException(311); // Null request
        }
        validateBasicRequest(request);
        if (request.getEvidenceRequired() != null && request.getEvidenceRequired()) {
            if (request.getEvidence() == null || request.getEvidence().isEmpty() || request.getEvidence().isBlank()) {
                throw new LineClearanceLogException(312); // Throw an error if evidence is missing
            }
        }
        // Validate rejection status
        if (!"reject".equalsIgnoreCase(request.getStatus())) {
            throw new LineClearanceLogException(318, "Invalid status. Only 'reject' is allowed.");
        }

        // Validate reason for rejection
        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new LineClearanceLogException(317, "Rejection reason is required.");
        }

        // Check if the batch number and other fields exist in the repository
        LineClearanceLog existingLineClearanceLog = lineClearanceLogRepository.findBySiteAndTempleteNameAndBatchNoAndOperationAndPhaseAndTaskName(request.getSite(),request.getTempleteName(),request.getBatchNo(),request.getOperation(),request.getPhase(),request.getTaskName());


        if (existingLineClearanceLog == null) {
            throw new LineClearanceLogException(328);
        }

        if(!existingLineClearanceLog.getStatus().equalsIgnoreCase("new")){
            throw new LineClearanceLogException(333);
        }
        // Check if already rejected
        if ("reject".equalsIgnoreCase(existingLineClearanceLog.getStatus())) {
            throw new LineClearanceLogException(319, "This batch is already rejected.");
        }

        // Update the existing log
        existingLineClearanceLog.setStatus(request.getStatus());
        existingLineClearanceLog.setEvidence(request.getEvidence());
        existingLineClearanceLog.setReason(request.getReason());
        existingLineClearanceLog.setActive(1);
        existingLineClearanceLog.setModifiedBy(request.getUserId());
        existingLineClearanceLog.setModifiedDateTime(LocalDateTime.now());

        // Save the updated log
        lineClearanceLogRepository.save(existingLineClearanceLog);

        // Return a success message
        return new MessageResponse("For the Batch " + request.getBatchNo() + " rejected due to failed criteria");
    }

    @Override
    public MessageResponse approveLineClearanceLog(LineClearanceLogRequest request) {


        validateRequest(request);
        if(request.getStatus().equalsIgnoreCase("approved")){

            // Check if the batch number already exists in the repository
            LineClearanceLog existingLineClearanceLog = lineClearanceLogRepository.findBySiteAndTempleteNameAndBatchNoAndOperationAndPhaseAndTaskName(request.getSite(),request.getTempleteName(),request.getBatchNo(),request.getOperation(),request.getPhase(),request.getTaskName());


            if(existingLineClearanceLog==null){
                throw new LineClearanceLogException(328, request.getBatchNo());  // Batch Id Not Exist
            }
            if(!existingLineClearanceLog.getStatus().equalsIgnoreCase("new")){
                throw new LineClearanceLogException(333);
            }
            // Check if already rejected
            if ("approved".equalsIgnoreCase(existingLineClearanceLog.getStatus())) {
                throw new LineClearanceLogException(329, "This batch is already Approved.");
            }
            if (request.getEvidenceRequired() != null && request.getEvidenceRequired()) {
                if (request.getEvidence() == null || request.getEvidence().isEmpty() || request.getEvidence().isBlank()) {
                    throw new LineClearanceLogException(312); // Throw an error if evidence is missing
                }
            }
            existingLineClearanceLog.setStatus("Approved");
            existingLineClearanceLog.setEvidence(request.getEvidence());
            existingLineClearanceLog.setActive(1);
            existingLineClearanceLog.setModifiedBy(request.getUserId());
            existingLineClearanceLog.setModifiedDateTime(LocalDateTime.now());
            lineClearanceLogRepository.save(existingLineClearanceLog);
            return new MessageResponse("For the Batch " + request.getBatchNo() + " Line Clearance approved successfully");

        }else{
            throw new LineClearanceLogException(320);
        }


    }
    @Override
    public MessageResponse updateLineClearanceLog(LineClearanceLogRequest request) {



        if (request == null) {
            throw new LineClearanceLogException(311);
        }
        if (request.getBatchNo() == null || request.getBatchNo().isEmpty()) {
            throw new LineClearanceLogException(8003);
        }
        if (request.getTempleteName() == null || request.getTempleteName().isEmpty()) {
            throw new LineClearanceLogException(313);
        }

        // Check if the batch number already exists in the repository
        LineClearanceLog existingLineClearanceLog = lineClearanceLogRepository.findBySiteAndTempleteNameAndBatchNoAndOperationAndPhaseAndTaskNameAndResourceId(request.getSite(),request.getTempleteName(),request.getBatchNo(),request.getOperation(),request.getPhase(),request.getTaskName(), request.getResourceId());
        if(existingLineClearanceLog==null){
            throw new LineClearanceLogException(328, request.getBatchNo());  // Batch Id Not exist
        }
        if(request.getNewStatus().equalsIgnoreCase(existingLineClearanceLog.getStatus())){
            throw new LineClearanceLogException(321);

        }
        if (request.getEvidenceRequired() != null && request.getEvidenceRequired()) {
            if (request.getEvidence() == null || request.getEvidence().isEmpty() || request.getEvidence().isBlank()) {
                throw new LineClearanceLogException(312); // Throw an error if evidence is missing
            }
        }
        existingLineClearanceLog.setStatus(request.getNewStatus());
        existingLineClearanceLog.setActive(1);
        existingLineClearanceLog.setEvidence(request.getEvidence());
        existingLineClearanceLog.setModifiedBy(request.getUserId());
        existingLineClearanceLog.setModifiedDateTime(LocalDateTime.now());
        lineClearanceLogRepository.save(existingLineClearanceLog);
        return new MessageResponse(request.getBatchNo()+" status updated to "+request.getNewStatus() );
    }

    @Override
    public List<LineClearanceLogHistoryResponse> retriveLineClearanceLogHistory(LineClearanceLogRequest request) {


        if (request == null) {
            throw new LineClearanceLogException(311);
        }
        if(request.getSite()==null || request.getSite().isBlank() || request.getSite().isEmpty()){
            throw new LineClearanceLogException(315) ;
        }
        if (request.getBatchNo()==null || request.getBatchNo().isBlank() || request.getBatchNo().isEmpty()){
            throw new LineClearanceLogException(8003) ;
        }
        if(request.getTaskName()==null || request.getTaskName().isBlank() || request.getTaskName().isEmpty()){
            throw new LineClearanceLogException(319) ;
        }
        List<LineClearanceLog> logs = lineClearanceLogRepository.findBySiteAndBatchNoAndTaskNameAndActive(request.getSite(), request.getBatchNo(), request.getTaskName(),1);

        // Map the logs to the response DTO
        return logs.stream()
                .map(log -> new LineClearanceLogHistoryResponse(
                        log.getSite(),
                        log.getTempleteName(),
                        log.getBatchNo(),
                        log.getTaskName(),
                        log.getStatus(),
                        log.getPhase(),
                        log.getOperation(),
                        log.getModifiedDateTime(),
                        log.getModifiedBy()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public MessageResponse validateLineClearanceLog(LineClearanceLogRequest request) {


        if (request == null) {
            throw new LineClearanceLogException(311);
        }
        if (request.getEvidenceRequired() != null && request.getEvidenceRequired()) {
            if (request.getEvidence() == null || request.getEvidence().isEmpty() || request.getEvidence().isBlank()) {
                throw new LineClearanceLogException(312); // Throw an error if evidence is missing
            }
        }
        if (request.getBatchNo() == null || request.getBatchNo().isEmpty()) {
            throw new LineClearanceLogException(8001);
        }
        if(request.getTaskName()==null || request.getTaskName().isBlank() || request.getTaskName().isEmpty()){
            throw new LineClearanceLogException(330) ;
        }
        if(!request.getStatus().isEmpty() || request.getStatus().isBlank()||request.getStatus()==null){
            throw new LineClearanceLogException(324);
        }
//
        return new MessageResponse( "Line clearance validation passed");
    }





    @Override
    public List<RetrieveLineClearanceLogResponse> retrieveLineClearanceList(LineClearanceLogRequest request) {
        List<RetrieveLineClearanceLogResponse> log = new ArrayList<>();
        System.out.println("Current Time------>"+currentZonedDateTime);
        // Log the request to see the input data
        System.out.println("Received request: " + request);
        if(request.getBatchNo() == null || request.getBatchNo().isEmpty()){
            throw new LineClearanceLogException(8001);
        }

        // Handle cases based on the availability of resourceId and workCenterId
        if (request.getResourceId() != null && !request.getResourceId().trim().isEmpty() &&
                request.getWorkCenterId() != null && !request.getWorkCenterId().trim().isEmpty()) {

            log = lineClearanceLogRepository.findByBatchNoAndResourceIdAndWorkCenterId(request.getBatchNo(),
                    request.getResourceId(), request.getWorkCenterId());
        } else if (request.getWorkCenterId() != null && !request.getWorkCenterId().trim().isEmpty()) {

            log = lineClearanceLogRepository.findByBatchNoAndWorkCenterId(request.getBatchNo(), request.getWorkCenterId());
        } else if (request.getResourceId() != null && !request.getResourceId().trim().isEmpty()) {

            log = lineClearanceLogRepository.findByBatchNoAndResourceId(request.getBatchNo(), request.getResourceId());
        } else {
            // If both resourceId and workCenterId are empty, return empty list or handle as needed
            return new ArrayList<>();
        }



        List<RetrieveLineClearanceLogResponse> updatedLog = new ArrayList<>();

        // Prepare the request to retrieve external data
        RetrieveLineClearanceLogRequest retrieveLineClearanceLogRequest = RetrieveLineClearanceLogRequest.builder()
                .site(request.getSite())
                .resourceId(request.getResourceId())
                .workCenterId(request.getWorkCenterId())
                .build();

        // Retrieve the external line clearance log data
        List<RetrieveLineClearanceLogResponse> retrieveLineClearanceList = lineClearanceService.retrieveLineClearanceList(retrieveLineClearanceLogRequest);

        // If both sides (local and external logs) are empty, return an empty list
        if ((log == null || log.isEmpty()) && (retrieveLineClearanceList == null || retrieveLineClearanceList.isEmpty())) {
            return new ArrayList<>();
        }

        // Compare local and external logs
        if ((log != null && !log.isEmpty()) && (retrieveLineClearanceList != null && !retrieveLineClearanceList.isEmpty())) {
            for (RetrieveLineClearanceLogResponse externalLog : retrieveLineClearanceList) {
                boolean foundMatch = false;
                boolean foundMatchInLocal = false;

                for (RetrieveLineClearanceLogResponse localLog : log) {
                    if (localLog.getTaskName().equalsIgnoreCase(externalLog.getTaskName()) &&
                            localLog.getTempleteName().equalsIgnoreCase(externalLog.getTempleteName())) {


                        long clearanceTime = 0;
                        try {
                            clearanceTime = Long.parseLong(externalLog.getClearanceTimeLimit());  // Convert clearance time string to long
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid clearance time format: " + externalLog.getClearanceTimeLimit());
                            clearanceTime = 0;  // Set to default value in case of error
                        }
                        if (localLog.getCompletedDateTime() != null) {
                            // Convert completedDateTime to ZonedDateTime and then extract the LocalTime
                            ZonedDateTime completedDateTime = localLog.getCompletedDateTime().atZone(ZoneId.of("UTC"));
                            LocalTime completedTime = completedDateTime.toLocalTime();

                            // Calculate the time limit by adding the clearance time to the completed time
                            LocalTime timeLimit = completedTime.plusMinutes(clearanceTime);

                            // Check if the current time has passed the time limit
                            if (clearanceTime!=0&&LocalTime.now().isAfter(timeLimit)) {
                                LineClearanceLog existingLog = lineClearanceLogRepository.findBySiteAndTempleteNameAndBatchNoAndOperationAndPhaseAndTaskNameAndResourceId(request.getSite(),localLog.getTempleteName(),request.getBatchNo(),request.getOperation(),request.getPhase(),localLog.getTaskName(), request.getResourceId());


                                if (existingLog == null) {
                                    throw new LineClearanceLogException(328, request.getBatchNo());  // Batch Id Not exist
                                }

                                localLog.setStatus("new");
                                localLog.setTempleteName(existingLog.getTempleteName());
                                localLog.setTaskName(existingLog.getTaskName());
                                localLog.setDescription(existingLog.getDescription());
                                localLog.setReason(existingLog.getReason());

                                if(existingLog.getEvidenceRequired()==externalLog.getEvidenceRequired()){
                                    localLog.setEvidenceRequired(existingLog.getEvidenceRequired());
                                }else{
                                    localLog.setEvidenceRequired(externalLog.getEvidenceRequired());
                                    existingLog.setEvidenceRequired(externalLog.getEvidenceRequired());
                                }
                                if(existingLog.getIsMandatory()==externalLog.getIsMandatory()){
                                    localLog.setIsMandatory(existingLog.getIsMandatory());
                                }else{
                                    localLog.setIsMandatory(externalLog.getIsMandatory());
                                    existingLog.setIsMandatory(externalLog.getIsMandatory());
                                }



                                localLog.setEvidence(existingLog.getEvidence());
                                existingLog.setStatus("new");
                                lineClearanceLogRepository.save(existingLog);

                                System.out.println("Status set to 'new' because time limit exceeded");
                            }
                        }

// Add the log to the updated list, retaining the status if not updated

                        if(!updatedLog.contains(localLog)){
                            updatedLog.add(localLog);
                        }
                        foundMatch = true;

                        break;
                    }


                }

                // If no match found, add a new log entry
                if (!foundMatch ) {
                    RetrieveLineClearanceLogResponse newLog = new RetrieveLineClearanceLogResponse();
                    newLog.setTempleteName(externalLog.getTempleteName());
                    newLog.setTaskName(externalLog.getTaskName());
                    newLog.setDescription(externalLog.getDescription());
                    newLog.setIsMandatory(externalLog.getIsMandatory());
                    newLog.setEvidenceRequired(externalLog.getEvidenceRequired());
                    newLog.setEvidence(externalLog.getEvidence());
                    newLog.setClearanceTimeLimit(externalLog.getClearanceTimeLimit());
                    newLog.setReason(externalLog.getReason());
                    newLog.setStatus("new");
                    if(!updatedLog.contains(newLog)){
                        updatedLog.add(newLog);
                    }
                }

            }
        }

        // Handle case where local logs are empty but external logs are available
        if ((log == null || log.isEmpty()) && (retrieveLineClearanceList != null && !retrieveLineClearanceList.isEmpty())) {
            for (RetrieveLineClearanceLogResponse externalLog : retrieveLineClearanceList) {
                RetrieveLineClearanceLogResponse newLog = new RetrieveLineClearanceLogResponse();
                newLog.setTempleteName(externalLog.getTempleteName());
                newLog.setTaskName(externalLog.getTaskName());
                newLog.setDescription(externalLog.getDescription());
                newLog.setIsMandatory(externalLog.getIsMandatory());
                newLog.setEvidenceRequired(externalLog.getEvidenceRequired());
                newLog.setEvidence(externalLog.getEvidence());
                newLog.setClearanceTimeLimit(externalLog.getClearanceTimeLimit());
                newLog.setStatus("new");
                newLog.setReason(externalLog.getReason());
                if(!updatedLog.contains(newLog)){
                    updatedLog.add(newLog);
                }

            }
        }

        return updatedLog;
    }

    @Override
    public MessageResponse storeFile(LineClearanceLogRequestList lineClearanceLogRequestList) {
        List<LineClearanceLogRequest> lineClearanceLogRequests = lineClearanceLogRequestList.getLineClearanceLogRequestList();
        if (lineClearanceLogRequests == null || lineClearanceLogRequests.isEmpty()) {
            throw new LineClearanceLogException(354);
        }
        for (LineClearanceLogRequest request : lineClearanceLogRequests) {

            String handle = createHandle(request);
            LineClearanceLog existingLog = lineClearanceLogRepository.findBySiteAndHandleAndActive(request.getSite(), handle, 1);
            if (existingLog == null) {
                throw new LineClearanceLogException(355, request.getBatchNo());
            }
            existingLog.setEvidence(request.getEvidence());
            lineClearanceLogRepository.save(existingLog);
        }

        return new MessageResponse("File Attached");
    }


    private void validateBasicRequest(LineClearanceLogRequest request) {

        if (StringUtils.isEmpty(request.getBatchNo().toString())) {
            throw new LineClearanceLogException(8001);  // Batch number missing
        }
        if (StringUtils.isEmpty(request.getTempleteName())) {
            throw new LineClearanceLogException(8005);  // Template name missing
        }

    }

    private String createHandle(LineClearanceLogRequest request){
        if(request.getBatchNo() == null || request.getBatchNo().isEmpty()){
            throw new LineClearanceLogException(8001);
        }
        if(request.getTempleteName() == null || request.getTempleteName().isEmpty()){
            throw new LineClearanceLogException(8005);
        }
        String batchNoBO = "BatchNoBO:" + request.getSite() + "," + request.getBatchNo()+","+request.getOperation()+","+request.getPhase();
        String templateBO = "TemplateNameBO:" + request.getSite() + "," + request.getTempleteName();
        String taskNameBO= "TaskNameBO:" + request.getSite()+","+request.getTaskName();
//        String workCenterBO = "WorkCenterBO:" + request.getSite() + "," + request.getWorkCenterId() ;
//        String resourceIdBO = "ResourceIdBO:" + request.getSite() + "," + request.getResourceId();

        return "LineClearanceLog:" + request.getSite() + "," + batchNoBO + ","+ templateBO+","+taskNameBO;
    }

    private LineClearanceLog buildLineClearanceLog(LineClearanceLogRequest request, String handle, String status) {
        return LineClearanceLog.builder()
                .handle(handle)
                .batchNo(request.getBatchNo())
                .site(request.getSite())
                .templeteName(request.getTempleteName())
                .description(StringUtils.isEmpty(request.getDescription()) ? request.getTempleteName() : request.getDescription())
                .phase(request.getPhase())
                .operation(request.getOperation())
                .resourceId(request.getResourceId())
                .workCenterId(request.getWorkCenterId())
                .userId(request.getUserId())
                .taskName(request.getTaskName())
                .taskDescription(request.getTaskDescription())
                .isMandatory(request.getIsMandatory())
                .evidenceRequired(request.getEvidenceRequired())
                .evidence(null)
                .status(request.getStatus())
                .reason(request.getReason())
                .approvedDateTime(LocalDateTime.now())
                .reason(request.getReason())
                .approvedBy(request.getUserId())
                .active(1)
                .build();
    }




    @Override
    public List<LineClearanceLog> retriveLineClearanceLogList(LineClearanceLogRequest request) {
        String site = request.getSite();
        String batchNo = (request.getBatchNo());
        String templeteName = request.getTempleteName();
        String userId = request.getUserId();
        String dateRange = request.getDateRange();
        LocalDateTime startDate = request.getStartDate();
        LocalDateTime endDate = request.getEndDate();
        String resourceId = request.getResourceId();
        String status = request.getStatus();

        if (site == null || site.isEmpty()) {
            throw new LineClearanceLogException(315);
        }

        Criteria criteria = Criteria.where("site").is(site).and("active").is(1);
        LocalDateTime now = LocalDateTime.now();

        if (batchNo != null && !batchNo.isEmpty()) {
            criteria.and("batchNo").is(batchNo);
        }

        if (templeteName != null && !templeteName.isEmpty()) {
            criteria.and("templeteName").is(templeteName);
        }

        if (userId != null && !userId.isEmpty()) {
            criteria.and("userId").is(userId);
        }

        if (resourceId != null && !resourceId.isEmpty()) {
            criteria.and("resourceId").is(resourceId);
        }

        if (status != null && !status.isEmpty()) {
            criteria.and("status").is(status);
        }

        // Apply date filters based on dateRange
        if (dateRange != null) {
            switch (dateRange) {
                case "24hours":
                    criteria.and("createdDateTime").gte(now.minusHours(24)).lte(now);
                    break;
                case "today":
                    LocalDate today = now.toLocalDate();
                    criteria.and("createdDateTime").gte(today.atStartOfDay()).lte(now);
                    break;
                case "yesterday":
                    LocalDate yesterday = now.toLocalDate().minusDays(1);
                    criteria.and("createdDateTime")
                            .gte(yesterday.atStartOfDay())
                            .lte(yesterday.atTime(23, 59, 59));
                    break;
                case "thisWeek":
                    LocalDate startOfWeek = now.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    criteria.and("createdDateTime").gte(startOfWeek.atStartOfDay()).lte(now);
                    break;
                case "lastWeek":
                    LocalDate lastWeekStart = now.toLocalDate().minusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    LocalDate lastWeekEnd = lastWeekStart.plusDays(6);
                    criteria.and("createdDateTime").gte(lastWeekStart.atStartOfDay()).lte(lastWeekEnd.atTime(23, 59, 59));
                    break;
                case "thisMonth":
                    LocalDate startOfMonth = now.toLocalDate().withDayOfMonth(1);
                    criteria.and("createdDateTime").gte(startOfMonth.atStartOfDay()).lte(now);
                    break;
                case "lastMonth":
                    LocalDate lastMonthStart = now.toLocalDate().minusMonths(1).withDayOfMonth(1);
                    LocalDate lastMonthEnd = lastMonthStart.withDayOfMonth(lastMonthStart.lengthOfMonth());
                    criteria.and("createdDateTime").gte(lastMonthStart.atStartOfDay()).lte(lastMonthEnd.atTime(23, 59, 59));
                    break;
                case "thisYear":
                    LocalDate startOfYear = now.toLocalDate().withDayOfYear(1);
                    criteria.and("createdDateTime").gte(startOfYear.atStartOfDay()).lte(now);
                    break;
                case "lastYear":
                    LocalDate lastYearStart = now.toLocalDate().minusYears(1).withDayOfYear(1);
                    LocalDate lastYearEnd = lastYearStart.withDayOfYear(lastYearStart.lengthOfYear());
                    criteria.and("createdDateTime").gte(lastYearStart.atStartOfDay()).lte(lastYearEnd.atTime(23, 59, 59));
                    break;
                case "custom":
                    if (startDate != null && endDate != null) {
                        criteria.and("createdDateTime").gte(startDate).lte(endDate);
                    } else {
                        throw new LineClearanceLogException(5124, "Custom date range requires both startDate and endDate.");
                    }
                    break;
                default:
                    throw new LineClearanceLogException(5123, "Invalid dateRange value.");
            }
        }

        Query query = new Query(criteria);
        query.with(Sort.by(Sort.Direction.DESC, "createdDateTime"));

        return mongoTemplate.find(query, LineClearanceLog.class);
    }

    public List<LineClearanceLogResponse> getLineClearanceLogList(LineClearanceLogRequest request) throws Exception {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be empty");
        }

        if (request.getSite() == null || request.getBatchNo() == null || request.getResourceId() == null
                || request.getOperation() == null || request.getPhase() == null) {
            throw new IllegalArgumentException("some required fields are empty");
        }

        List<LineClearanceLogResponse> logResponseList = lineClearanceLogRepository
                .findByActiveAndSiteAndBatchNoAndResourceIdAndOperationAndPhase(1,
                        request.getSite(), request.getBatchNo(), request.getResourceId(), request.getOperation(), request.getPhase());

        return logResponseList != null ? logResponseList : Collections.emptyList();
    }

//    @Override
//    public boolean checkLineClearance(String site, String batchNo, String resourceId) {
//        List<LineClearanceLog> lineClearanceLogs = lineClearanceLogRepository.findBySiteAndBatchNoAndResourceId(site, batchNo, resourceId);
//
//        if (lineClearanceLogs == null || lineClearanceLogs.isEmpty()) {
//            return false;
//        }
//
//        return lineClearanceLogs.stream().anyMatch(log -> "COMPLETE".equalsIgnoreCase(log.getStatus()));
//    }
    @Override
    public boolean checkLineClearance(String site, String batchNo, String resourceId, String operation, String phase) {
        LineClearanceLogRequest request = LineClearanceLogRequest.builder()
                .site(site)
                .resourceId(resourceId)
                .batchNo(batchNo)
                .operation(operation)
                .phase(phase)
                .build();
        List<RetrieveLineClearanceLogResponse> lineClearanceLogs = retrieveLineClearanceList(request);

        if (lineClearanceLogs == null || lineClearanceLogs.isEmpty()) {
            return false;
        }

        if (lineClearanceLogs.stream().anyMatch(log -> "NEW".equalsIgnoreCase(log.getStatus()) || "START".equalsIgnoreCase(log.getStatus()))) {
            return false;
        }
        return lineClearanceLogs.stream().anyMatch(log -> "COMPLETE".equalsIgnoreCase(log.getStatus()));
    }

    @Override
    public boolean changeLineCleranceStatusToNew(String site, String batchNo, String resourceId) {
        lineClearanceLogRepository.deleteBySiteAndBatchNoAndResourceId(site, batchNo, resourceId);
        return true;
    }


    private boolean logMachine(String site, String workcenterId, String resourceId, String reason, String logEvent, String user) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("siteId", site);
        requestBody.put("workcenterId", workcenterId);
        requestBody.put("resourceId", resourceId);
        requestBody.put("reason", reason);
        requestBody.put("logEvent", logEvent);
        requestBody.put("active", 1);
        requestBody.put("commentUsr", user);

        String response = webClientBuilder.build()
                .post()
                .uri(logmachinelog)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return true;
    }

    private boolean productionLog(LineClearanceLog lineClearanceLog) throws Exception {
        ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                .site(lineClearanceLog.getSite())
                .eventType("startSfcBatch")
                .userId(lineClearanceLog.getUserId())
                .batchNo(lineClearanceLog.getBatchNo())
                .operation(lineClearanceLog.getOperation())
                .phaseId(lineClearanceLog.getPhase())
                .reasonCode(lineClearanceLog.getReason())
                .workcenterId(lineClearanceLog.getWorkCenterId())
                .resourceId(lineClearanceLog.getResourceId())
                .orderNumber(lineClearanceLog.getOrderNumber())
                .item(lineClearanceLog.getItem())
                .itemVersion(lineClearanceLog.getItemVersion())
                .material(lineClearanceLog.getItem())
                .materialVersion(lineClearanceLog.getItemVersion())
                .shopOrderBO(lineClearanceLog.getOrderNumber())
                .qty(lineClearanceLog.getQuantity())
                .topic("production-log")
                .status("Active")
                .createdDatetime(LocalDateTime.now())
                .eventData(lineClearanceLog.getBatchNo() + " Line Clearance Started successfully")
                .build();

        boolean productionLog = ProcessOrderUtility.productionLog(productionLogRequest);
        if (!productionLog) {
            throw new LineClearanceLogException(7024);
        }
        return true;
    }

}
