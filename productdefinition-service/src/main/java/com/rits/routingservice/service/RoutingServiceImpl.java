package com.rits.routingservice.service;

import com.rits.operationservice.dto.AuditLogRequest;
import com.rits.routingservice.dto.*;
import com.rits.routingservice.exception.RoutingException;
import com.rits.routingservice.model.*;
import com.rits.routingservice.repository.RoutingRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.Local;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RoutingServiceImpl implements RoutingService {
    private final RoutingRepository routingRepository;
    private final WebClient.Builder webClientBuilder;
    private final MessageSource localMessageSource;
    private final ApplicationEventPublisher eventPublisher;
    @Value("${bom-service.url}/isExist")
    private String isBomExist;
    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;
    @Value("${pcuinwork.url}/retrieveListPCUOfPcuBO")
    private String livedataUrlinWork;

    @Value("${livedata.url}/retrieveListOfPcuBO")
    private String livedataUrl;
    @Value("${auditlog-service.url}/producer")
    private String auditlogUrl;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }


    @Override
    public RoutingMessageModel createRouting(RoutingRequest routingRequest) throws Exception {
        if (routingRepository.existsByRoutingAndVersionAndSiteAndActiveEquals(routingRequest.getRouting(), routingRequest.getVersion(), routingRequest.getSite(), 1)) {
            throw new RoutingException(501,routingRequest.getRouting(), routingRequest.getVersion());
        } else {
            try {
                validateRevisionAndUserID(routingRequest);
                validateBomExistence(routingRequest);
                deactivateExistingRoutingVersions(routingRequest);
                validateAndProcessRoutingSteps(routingRequest);
                setDefaultDescriptionIfEmpty(routingRequest);
                setParentRouteFlag(routingRequest);
                processRoutingDetails(routingRequest);
            } catch (RoutingException routingException) {
                throw routingException;
            } catch (Exception e) {
                throw e;
            }

            // Saving new routing
            Routing routing = createNewRoutingObject(routingRequest);
            String createdMessage = getFormattedMessage(11, routingRequest.getRouting(), routingRequest.getVersion());


            return RoutingMessageModel.builder()
                    .message_details(new MessageDetails(createdMessage, "S"))
                    .response(routingRepository.save(routing))
                    .build();
        }
    }

    private List<String> validateOperations(RoutingRequest routingRequest) {
        List<String> operationValidationReport = new ArrayList<>();
        Set<String> uniqueOperations = new HashSet<>();

        for (RoutingStep step : routingRequest.getRoutingStepList()) {
            String operation = step.getOperation();
            if (operation != null && !operation.isEmpty()) {
                if (!uniqueOperations.add(operation)) {
                    operationValidationReport.add("Operation " + operation + " is already in use.");
                }
            }
        }
        return operationValidationReport.isEmpty() ? null : operationValidationReport;
    }

    private List<String> validateEntrySteps(RoutingRequest routingRequest) {
        List<String> entryStepValidationReport = new ArrayList<>();
        boolean hasEntryStep = false;
        int entryStepCount = 0;
        int lastReportingStepCount = 0;
        int totalSteps = routingRequest.getRoutingStepList().size();

        for (RoutingStep step : routingRequest.getRoutingStepList()) {
            boolean entryStep = step.isEntryStep();
            boolean lastReportingStep = step.isLastReportingStep();

            if (entryStep) {
                entryStepCount++;
                hasEntryStep = true;
            }
            if (lastReportingStep) {
                lastReportingStepCount++;
            }
        }

        switch (routingRequest.getSubType()) {
            case "Sequential":
                if (entryStepCount != 1) entryStepValidationReport.add("There must be exactly one entryStep.");
                if (lastReportingStepCount != 1) entryStepValidationReport.add("There must be exactly one lastReportingStep.");
                break;
            case "Simultaneous":
                if (!hasEntryStep) entryStepValidationReport.add("At least one entryStep must be true.");
                if (lastReportingStepCount != 1) entryStepValidationReport.add("There must be exactly one last reporting step.");
                break;
            case "AnyOrder":
                if (entryStepCount != totalSteps) entryStepValidationReport.add("All entrySteps must be true.");
                if (lastReportingStepCount != 1) entryStepValidationReport.add("There must be exactly one lastReportingStep.");
                break;
            default:
                entryStepValidationReport.add("Invalid subType: " + routingRequest.getSubType());
                break;
        }

        return entryStepValidationReport.isEmpty() ? null : entryStepValidationReport;
    }

    private void validateRevisionAndUserID(RoutingRequest routingRequest) {
        if (routingRequest.getVersion() == null || routingRequest.getVersion().isEmpty()) {
            throw new RoutingException(107);
        }
        if (routingRequest.getUserId() == null || routingRequest.getUserId().isEmpty()) {
            throw new RoutingException(108);
        }
    }

    private void validateBomExistence(RoutingRequest routingRequest) {
        if (routingRequest.getBom() != null && !routingRequest.getBom().isEmpty()) {
            IsExist isExist = IsExist.builder().site(routingRequest.getSite()).bom(routingRequest.getBom()).revision(routingRequest.getBomVersion()).build();
            Boolean bomExist = webClientBuilder.build()
                    .post()
                    .uri(isBomExist)
                    .bodyValue(isExist)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();

            if (!bomExist) {
                throw new RoutingException(200, routingRequest.getBom());
            }
        }
    }


    private void deactivateExistingRoutingVersions(RoutingRequest routingRequest) {
        List<Routing> routingList = routingRepository.findByRoutingAndSiteAndActive(routingRequest.getRouting(), routingRequest.getSite(), 1);
        routingList.stream()
                .filter(Routing::isCurrentVersion)
                .map(routing -> {
                    routing.setCurrentVersion(false);
                    routing.setUpdatedDateTime(LocalDateTime.now());
                    return routing;
                }).forEach(routingRepository::save);

    }

    private void validateAndProcessRoutingSteps(RoutingRequest routingRequest) {
        if (routingRequest.getRoutingStepList() != null && !routingRequest.getRoutingStepList().isEmpty()) {
            if (routingRequest.getSubType() != null && !routingRequest.getSubType().isEmpty()) {


                if (routingRequest.getSubType().equalsIgnoreCase("simultaneous") || routingRequest.getSubType().equalsIgnoreCase("anyOrder")) {
                    List<RoutingStep> routingSteps = routingRequest.getRoutingStepList();
                    Boolean allEntryStepsAreTrue = routingSteps.stream()
                            .map(RoutingStep::isEntryStep)
                            .allMatch(Boolean.TRUE::equals);
//                        if (!allEntryStepsAreTrue) {
//                            throw new RoutingException(504);
//                        }
                    if (routingRequest.getSubType().equalsIgnoreCase("anyOrder")) {
                        if (!allEntryStepsAreTrue) {
                            throw new RoutingException(504);
                        }
                    }

                    // Update the needToBeCompleted field for each step
                    for (RoutingStep step : routingSteps) {
                        if(step.isEntryStep() ){
                            if(!step.getPreviousStepId().equals("00")
                        ) {
                                throw new RoutingException(515);
                            }
                        }
                        if(step.isLastReportingStep()){//
                            if(!step.getNextStepId().equals("00")){
                                throw new RoutingException(516);
                            }
                        }
                        StringBuilder needToBeCompleted = new StringBuilder();
                        String currentStepId = step.getStepId();

                        for (RoutingStep otherStep : routingSteps) {
                            String stepId = otherStep.getStepId();
                            if (!stepId.equals(currentStepId)) {
                                if (needToBeCompleted.length() > 0) {
                                    needToBeCompleted.append(",");
                                }
                                needToBeCompleted.append(stepId);
                            }
                        }

                        step.setNeedToBeCompleted(needToBeCompleted.toString());
                    }
                }

                if (routingRequest.getSubType().equalsIgnoreCase("parallel")) {
                    List<RoutingStep> routingSteps = routingRequest.getRoutingStepList();
                    boolean allStepsAreValid = routingSteps.stream()
                            .filter(step -> step.isEntryStep())
                            .noneMatch(step -> routingSteps.stream()
                                    .filter(RoutingStep::isEntryStep)
                                    .anyMatch(entryStep -> step.getNextStepId().equals(entryStep.getStepId())));
                    if (!allStepsAreValid) {
                        throw new RoutingException(505);
                    }
                }
            }
        }
    }

    private void setDefaultDescriptionIfEmpty(RoutingRequest routingRequest) {
        if (routingRequest.getDescription() == null || routingRequest.getDescription().isEmpty()) {
            routingRequest.setDescription(routingRequest.getRouting());
        }
    }

    private void setParentRouteFlag(RoutingRequest routingRequest) {
        routingRequest.setParentRoute(routingRequest.getParentRouterBO() != null && !routingRequest.getParentRouterBO().isEmpty());
    }

    private void processRoutingDetails(RoutingRequest routingRequest) {
        if (routingRequest.getRoutingStepList() != null && !routingRequest.getRoutingStepList().isEmpty()) {
            List<RoutingStep> routingStepList = routingRequest.getRoutingStepList();
            Set<String> operationSet = new HashSet<>();

            for (RoutingStep step : routingStepList) {
                step.setParentStep(true);
                String operation = step.getOperation();
                String operationVersion = step.getOperationVersion();


                if (operation != null && !operation.isEmpty()) {
                    String operationKey = operation + "_" + operationVersion;

                    if (!operationSet.add(operationKey)) {
                        throw new RoutingException(507, operation, operationVersion);
                    }
                }

                if ("Routing".equalsIgnoreCase(step.getStepType())) {
                    String routerBO = step.getRoutingBO();
                    Routing routingDetail = routingRepository.findByActiveAndSiteAndHandle(1, routingRequest.getSite(), routerBO);

                    if (routingDetail != null) {
                        if (!routingDetail.getRoutingStepList().isEmpty()) {
                            List<Routing> routingDetails = new ArrayList<>();


                            for (int i = 0; i < routingDetail.getRoutingStepList().size(); i++) {
                                RoutingStep routingStep = routingDetail.getRoutingStepList().get(i);
                                routingStep.setParentStep(false);
                            }

                            routingDetails.add(routingDetail);
                            step.setRouterDetails(routingDetails);
                        }
                    } else {
                        throw new RoutingException(512);
                    }
                }
            }
        }

        if (routingRequest.getRoutingStepList() != null && !routingRequest.getRoutingStepList().isEmpty()) {
            // Extract unique work centers from routing steps
            List<String> uniqueWorkCenters = routingRequest.getRoutingStepList().stream()
                    .map(RoutingStep::getWorkCenter)
                    .distinct()
                    .collect(Collectors.toList());
            List<String> childstepidlist = routingRequest.getRoutingStepList().stream()
                    .map(RoutingStep::getNextStepId).collect(Collectors.toList());
            // Create the "lanes" array

            for (int i = 0; i < routingRequest.getRoutingStepList().size(); i++) {
                String childstepdetail = childstepidlist.get(i).trim();
                List<Integer> childstep = new ArrayList<>();
                if (childstepdetail.contains(",")) {
                    String[] steps = childstepdetail.split(",");
                    for (String step : steps) {
                        Integer childstepid = Integer.parseInt(step);
                        if (childstepid != 0)
                            childstep.add(childstepid);
                    }
                } else {
                    Integer childstepid = Integer.parseInt(childstepdetail);
                    if (childstepid != 0)
                        childstep.add(childstepid);
                }
                routingRequest.getRoutingStepList().get(i).setChildStepId(childstep);
            }
            List<RoutingLane> lanes = new ArrayList<>();
            for (int i = 0; i < uniqueWorkCenters.size(); i++) {
                RoutingLane lane = new RoutingLane("sap-icon://order-status", uniqueWorkCenters.get(i), i);
                lanes.add(lane);
            }

            // Update the RoutingRequest object with the "lanes" array
            routingRequest.setLanes(lanes);
        }
    }

    private Routing createNewRoutingObject(RoutingRequest routingRequest) {
        return Routing.builder()
                .site(routingRequest.getSite())
                .routing(routingRequest.getRouting())
                .version(routingRequest.getVersion())
                .handle("RoutingBo:" + routingRequest.getSite() + "," + routingRequest.getRouting() + "," + routingRequest.getVersion())
                .description(routingRequest.getDescription())
                .status(routingRequest.getStatus())
                .routingType(routingRequest.getRoutingType())
                .subType(routingRequest.getSubType())
                .currentVersion(routingRequest.isCurrentVersion())
                .relaxedRoutingFlow(routingRequest.isRelaxedRoutingFlow())
                .document(routingRequest.getDocument())
                .dispositionGroup(routingRequest.getDispositionGroup())
                .bom(routingRequest.getBom())
                .bomVersion(routingRequest.getBomVersion())
                .replicationToErp(routingRequest.isReplicationToErp())
                .isParentRoute(routingRequest.isParentRoute())
                .parentRouterBO(routingRequest.getParentRouterBO())
                .routingStepList(routingRequest.getRoutingStepList())
                .customDataList(routingRequest.getCustomDataList())
                .inUse(routingRequest.isInUse())
                .createdBy(routingRequest.getUserId())
                .active(1)
                .createdDateTime(LocalDateTime.now())
                .build();
    }


    @Override
    public RoutingMessageModel updateRouting(RoutingRequest routingRequest) throws Exception {
        if (routingRepository.existsByRoutingAndVersionAndSiteAndActiveEquals(routingRequest.getRouting(), routingRequest.getVersion(), routingRequest.getSite(), 1)) {
            try {
                List<String> operationErrors = validateOperations(routingRequest);
                List<String> entryStepErrors = validateEntrySteps(routingRequest);

                List<String> combinedErrors = new ArrayList<>();
                Integer errorCode = null;

                if (operationErrors != null && !operationErrors.isEmpty()) {
                    errorCode = 550;
                    combinedErrors.addAll(operationErrors);
                }

                if (entryStepErrors != null && !entryStepErrors.isEmpty()) {
                    errorCode = 551;
                    combinedErrors.addAll(entryStepErrors);
                }

                if (!combinedErrors.isEmpty()) {
                    String errorMessage = String.join(", ", combinedErrors);
                    return RoutingMessageModel.builder()
                            .errorCode(errorCode)
                            .message_details(new MessageDetails(errorMessage, "e"))
                            .build();
                }
                validateRevisionAndUserID(routingRequest);
                validateBomExistence(routingRequest);
                deactivateExistingRoutingVersions(routingRequest);
                validateAndProcessRoutingSteps(routingRequest);
                setDefaultDescriptionIfEmpty(routingRequest);
                setParentRouteFlag(routingRequest);
                processRoutingDetails(routingRequest);
            } catch (RoutingException routingException) {
                throw routingException;
            } catch (Exception e) {
                throw e;
            }

            Routing existingRouting = routingRepository.findByRoutingAndVersionAndSiteAndActive(routingRequest.getRouting(), routingRequest.getVersion(), routingRequest.getSite(), 1);
            if (existingRouting.isInUse()) {
                throw new RoutingException(506, routingRequest.getRouting(), routingRequest.getVersion());
            }
            Routing updatedRouting = updateRoutingBuilder(existingRouting, routingRequest);
            String updatedMessage = getFormattedMessage(12, routingRequest.getRouting(), routingRequest.getVersion());


            return RoutingMessageModel.builder().message_details(new MessageDetails(updatedMessage, "S")).response(routingRepository.save(updatedRouting)).build();


        } else {
            return createRouting(routingRequest);
        }
    }

    private Routing updateRoutingBuilder(Routing existingRouting, RoutingRequest routingRequest) {
        return Routing.builder()
                .site(existingRouting.getSite())
                .routing(existingRouting.getRouting())
                .version(existingRouting.getVersion())
                .handle(existingRouting.getHandle())
                .description(routingRequest.getDescription())
                .status(routingRequest.getStatus())
                .routingType(routingRequest.getRoutingType())
                .subType(routingRequest.getSubType())
                .currentVersion(routingRequest.isCurrentVersion())
                .relaxedRoutingFlow(routingRequest.isRelaxedRoutingFlow())
                .document(routingRequest.getDocument())
                .dispositionGroup(routingRequest.getDispositionGroup())
                .bom(routingRequest.getBom())
                .bomVersion(routingRequest.getBomVersion())
                .replicationToErp(routingRequest.isReplicationToErp())
                .isParentRoute(routingRequest.isParentRoute())
                .parentRouterBO(routingRequest.getParentRouterBO())
                .routingStepList(routingRequest.getRoutingStepList())
                .lanes(routingRequest.getLanes())
                .customDataList(routingRequest.getCustomDataList())
                .active(existingRouting.getActive())
                .inUse(routingRequest.isInUse())
                .createdBy(existingRouting.getCreatedBy())
                .modifiedBy(routingRequest.getUserId())
                .createdDateTime(existingRouting.getCreatedDateTime())
                .updatedDateTime(LocalDateTime.now())
                .build();
    }

    @Override
    public Routing retrieveRouting(String site, String routing, String version) throws Exception {
        Routing existingRouting;

        if (version != null && !version.isEmpty()) {
            existingRouting = routingRepository.findByRoutingAndVersionAndSiteAndActive(routing, version, site, 1);

            if (existingRouting == null) {
                throw new RoutingException(502, routing, version);
            }
        } else {
            existingRouting = routingRepository.findByRoutingAndCurrentVersionAndSiteAndActive(routing, true, site, 1);
            if (existingRouting == null) {
                throw new RoutingException(502, routing, "currentVersion");
            }
        }
        return existingRouting;
    }

    @Override
    public RoutingResponseList getRoutingList(String site, String routing) throws Exception {
        if (routing != null && !routing.isEmpty()) {
            List<RoutingResponse> routingResponse = routingRepository.findByActiveAndSiteAndRoutingContainingIgnoreCase(1, site, routing);
            if (routingResponse.isEmpty()) {
                throw new RoutingException(502, routing, "currentVersion");
            }
            return RoutingResponseList.builder().routingList(routingResponse).build();
        }
        return getRoutingListByCreationDate(site);
    }

    @Override
    public RoutingResponseList getRoutingListByCreationDate(String site) throws Exception {
        List<RoutingResponse> routingResponses = routingRepository.findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(1, site);
        return RoutingResponseList.builder().routingList(routingResponses).build();
    }

    @Override
    public RoutingMessageModel deleteRouting(String site, String routing, String version, String userId) throws Exception {
        if (version == null || version.isEmpty()) {
            throw new RoutingException(107);
        }
        if (userId == null || userId.isEmpty()) {
            throw new RoutingException(108);
        }
        if (routingRepository.existsByRoutingAndVersionAndSiteAndActiveEquals(routing, version, site, 1)) {
            Routing existingRouting = routingRepository.findByRoutingAndVersionAndSiteAndActive(routing, version, site, 1);
            if (!existingRouting.isInUse()) {
                existingRouting.setActive(0);
                existingRouting.setModifiedBy(userId);
                existingRouting.setUpdatedDateTime(LocalDateTime.now());
                routingRepository.save(existingRouting);
                String deletedMessage = getFormattedMessage(13, routing, version);
                return RoutingMessageModel.builder().message_details(new MessageDetails(deletedMessage, "S")).build();
            }
            throw new RoutingException(514);
        }
        throw new RoutingException(502, routing, version);
    }

    @Override
    public boolean isExist(String site, String routing, String version) throws Exception {
        if (version != null && !version.isEmpty()) {
            return routingRepository.existsByRoutingAndVersionAndSiteAndActiveEquals(routing, version, site, 1);
        } else {
            return routingRepository.existsByRoutingAndCurrentVersionAndSiteAndActive(routing, true, site, 1);
        }
    }

    @Override
    public String callExtension(Extension extension) throws Exception {
        String extensionResponse = webClientBuilder.build()
                .post()
                .uri(extensionUrl)
                .bodyValue(extension)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        if (extensionResponse == null) {
            throw new RoutingException(800);
        }
        return extensionResponse;
    }

    @Override
    public List<RoutingResponse> getAllRouting(String site) {
        return routingRepository.findByActiveAndSite(1, site);
    }

    @Override
    public RoutingMessageModel findNextStepIDDetails(String site, String routing, String version, String operation, String operationVersion) throws Exception {
        if (routingRepository.existsByRoutingAndVersionAndSiteAndActiveEquals(routing, version, site, 1)) {
            Routing router = routingRepository.findByRoutingAndVersionAndSiteAndActive(routing, version, site, 1);
            List<RoutingStep> routingStepList = router.getRoutingStepList();
            String nextStepId = "";

            for (RoutingStep routingStep : routingStepList) {
                if (routingStep.getOperation().equals(operation) && routingStep.getOperationVersion().equals(operationVersion)) {
                    nextStepId = routingStep.getNextStepId();
                }
            }
            if (router.getSubType().equalsIgnoreCase("Sequential")) {
                if (nextStepId.equalsIgnoreCase("00")) {
                    return RoutingMessageModel.builder().message_details(new MessageDetails("RoutingStep", "S")).build();
                }
            }

            String[] nextStepIdsArray = nextStepId.split(",");

            if (nextStepIdsArray.length > 1) {
                return RoutingMessageModel.builder()
                        .nextStepId(nextStepId)
                        .message_details(new MessageDetails("NextStepId", "S"))
                        .build();
            } else if (nextStepIdsArray.length == 1 && !nextStepIdsArray[0].isEmpty()) {
                RoutingStep nextRoutingStep = routingStepList.stream()
                        .filter(step -> step.getStepId().equals(nextStepIdsArray[0]))
                        .findFirst()
                        .orElseThrow(() -> new RoutingException(510, operation));

                return RoutingMessageModel.builder()
                        .routingStep(nextRoutingStep)
                        .message_details(new MessageDetails("RoutingStep", "S"))
                        .build();
            } else {
                throw new RoutingException(510, operation);
            }
        } else {
            throw new RoutingException(502, routing, version);
        }
    }


    @Override
    public RoutingMessageModel findNextStepIDDetailsOfParentStepId(String site, String routing, String version, String stepId) throws Exception {
        if (routingRepository.existsByRoutingAndVersionAndSiteAndActiveEquals(routing, version, site, 1)) {
            Routing router = routingRepository.findByRoutingAndVersionAndSiteAndActive(routing, version, site, 1);
            List<RoutingStep> routingStepList = router.getRoutingStepList();
            String nextStepId = "";

            for (RoutingStep routingStep : routingStepList) {
                if (routingStep.getStepId().equals(stepId)) {
                    nextStepId = routingStep.getNextStepId();
                    break;
                }
            }
            if (router.getSubType().equalsIgnoreCase("Sequential")) {
                if (nextStepId.equalsIgnoreCase("00")) {
                    return RoutingMessageModel.builder().message_details(new MessageDetails("RoutingStep", "S")).build();
                }
            }

            String[] nextStepIdArray = nextStepId.split(",");
            if (nextStepIdArray.length > 1) {
                return RoutingMessageModel.builder()
                        .nextStepId(nextStepId)
                        .message_details(new MessageDetails("NextStepId", "S"))
                        .build();
            } else if (nextStepIdArray.length == 1 && !nextStepIdArray[0].isEmpty()) {
                for (RoutingStep routingStep : routingStepList) {
                    if (routingStep.getStepId().equals(nextStepIdArray[0])) {
                        return RoutingMessageModel.builder()
                                .routingStep(routingStep)
                                .message_details(new MessageDetails("RoutingStep", "S"))
                                .build();
                    }
                }
            }

            throw new RoutingException(511, stepId);
        } else {
            throw new RoutingException(502, routing, version);
        }
    }


    @Override
    public List<RoutingStep> getOperationQueueList(String site, String routing, String version, String operation) throws Exception {
        if (routingRepository.existsByRoutingAndVersionAndSiteAndActiveEquals(routing, version, site, 1)) {

            String[] needsToBeComplete = null;
            Routing router = routingRepository.findByRoutingAndVersionAndSiteAndActive(routing, version, site, 1);
            List<RoutingStep> routingStepList = router.getRoutingStepList();
            List<RoutingStep> routingSteps = new ArrayList<>();
            String[] nextStepId = null;
            for (RoutingStep routingStep : routingStepList) {
                if (routingStep.getOperation().equals(operation)) {
                    if (routingStep.getNextStepId().equals("00") || routingStep.getNextStepId().equals("")) {
                        needsToBeComplete = routingStep.getNeedToBeCompleted().split(",");
                    } else {
                        nextStepId = routingStep.getNextStepId().split(",");

                    }

                }
            }
            if (needsToBeComplete == null && nextStepId == null) {
                throw new RoutingException(510, operation);
            }
            if (nextStepId == null) {
                for (RoutingStep routingStep : routingStepList) {
                    for (String nextStep : needsToBeComplete) {

                        if (routingStep.getStepId().equals(nextStep)) {
                            routingSteps.add(routingStep);
                        }
                    }
                }

            } else {
                for (RoutingStep routingStep : routingStepList) {
                    for (String nextStep : nextStepId) {

                        if (routingStep.getStepId().equals(nextStep)) {
                            routingSteps.add(routingStep);
                        }
                    }
                }

            }
            return routingSteps;
        }
        throw new RoutingException(502, routing, version);
    }

    @Override
    public String getOperationNextStepID(String site, String routing, String version, String operation) throws Exception {
        if (routingRepository.existsByRoutingAndVersionAndSiteAndActiveEquals(routing, version, site, 1)) {

            Routing router = routingRepository.findByRoutingAndVersionAndSiteAndActive(routing, version, site, 1);
            List<RoutingStep> routingStepList = router.getRoutingStepList();
            String nextStepId = null;
            for (RoutingStep routingStep : routingStepList) {
                if (routingStep.getOperation().equals(operation)) {
                    if (routingStep.getNextStepId().equals("00") || routingStep.getNextStepId().equals("")) {
                        nextStepId = routingStep.getNeedToBeCompleted();
                    } else {
                        nextStepId = routingStep.getNextStepId();

                    }

                }
            }
            return nextStepId;
        }
        throw new RoutingException(502, routing, version);
    }

    @Override
    public RoutingMessageModel getStepDetails(String site, String routing, String version, String stepId) throws Exception {
        if (routingRepository.existsByRoutingAndVersionAndSiteAndActiveEquals(routing, version, site, 1)) {
            Routing router = routingRepository.findByRoutingAndVersionAndSiteAndActive(routing, version, site, 1);
            List<RoutingStep> routingStepList = router.getRoutingStepList();
            for (RoutingStep routingStep : routingStepList) {
                if (routingStep.getStepId().equals(stepId)) {
                    return RoutingMessageModel.builder()
                            .routingStep(routingStep)
                            .message_details(new MessageDetails("RoutingStep", "S"))
                            .build();
                }
            }
        }
        throw new RoutingException(502, routing, version);
    }

    @Override
    public RoutingMessageModel findStepDetailsByNextStepId(String site, String routing, String version, String operation, String nextStepId, String operationVersion) throws Exception {
        if (routingRepository.existsByRoutingAndVersionAndSiteAndActiveEquals(routing, version, site, 1)) {
            Routing router = routingRepository.findByRoutingAndVersionAndSiteAndActive(routing, version, site, 1);
            List<RoutingStep> routingStepList = router.getRoutingStepList();
            String prevStepId = null;

            for (RoutingStep routingStep : routingStepList) {
                if (routingStep.getStepType().equalsIgnoreCase("operation") &&
                        routingStep.getOperation().equals(operation) &&
                        routingStep.getOperationVersion().equals(operationVersion)) {
                    prevStepId = routingStep.getStepId();
                    break;
                }
                if (routingStep.getStepType().equalsIgnoreCase("routing")) {
                    for (RoutingStep step : routingStep.getRouterDetails().get(0).getRoutingStepList()) {
                        if (step.getOperation().equalsIgnoreCase(operation) && step.getOperationVersion().equals(operationVersion)) {
                            prevStepId = routingStep.getStepId();
                            break;
                        }
                    }
                }
            }

            for (RoutingStep routingStep : routingStepList) {
                if (routingStep.getStepId().equals(nextStepId) &&
                        routingStep.getPreviousStepId().contains(prevStepId)) {
                    return RoutingMessageModel.builder()
                            .routingStep(routingStep)
                            .message_details(new MessageDetails("RoutingStep", "S"))
                            .build();
                }
            }

            return null;
        } else {
            throw new RoutingException(502, routing, version);
        }
    }


    @Override
    public boolean inUseUpdate(String site, String routing, String version) throws Exception {
        if (routingRepository.existsByRoutingAndVersionAndSiteAndActiveEquals(routing, version, site, 1)) {
            Routing existingRouting = routingRepository.findByRoutingAndVersionAndSiteAndActive(routing, version, site, 1);
            existingRouting.setInUse(true);
            routingRepository.save(existingRouting);
            return true;
        }
        return false;
    }

    @Override
    public Routing retrieveRoutingwithLiveRecord(String site, String routing, String version) throws Exception {
        Routing existingRouting;

        List<PcuInQueue> liveDataResponse = null;
        List<PcuInQueue> liveDataResponseinWork = null;

        if (version != null && !version.isEmpty()) {
            existingRouting = routingRepository.findByRoutingAndVersionAndSiteAndActive(routing, version, site, 1);
            if (existingRouting != null && existingRouting.getRoutingStepList().size() > 0) {
                for (int i = 0; i < existingRouting.getRoutingStepList().size(); i++) {
                    PcuInQueue liverequest = new PcuInQueue();
                    liverequest.setOperationBO("OperationBO:" + site + "," + existingRouting.getRoutingStepList().get(i).getOperation() + ",A");
                    liverequest.setSite(site);
                    liveDataResponse = /*(List<PcuInQueue>) webClientBuilder.build()
                        .post()
                        .uri(livedataUrl)
                        .bodyValue(liverequest)
                        .retrieve()
                        .bodyToMono(PcuInQueue.class)
                        .block();*/
                            webClientBuilder.build()
                                    .post()
                                    .uri(livedataUrl)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(liverequest)
                                    .retrieve()
                                    .bodyToFlux(PcuInQueue.class)
                                    .collectList()
                                    .block();
                    liveDataResponseinWork = /*(List<PcuInQueue>) webClientBuilder.build()
                        .post()
                        .uri(livedataUrl)
                        .bodyValue(liverequest)
                        .retrieve()
                        .bodyToMono(PcuInQueue.class)
                        .block();*/
                            webClientBuilder.build()
                                    .post()
                                    .uri(livedataUrlinWork)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(liverequest)
                                    .retrieve()
                                    .bodyToFlux(PcuInQueue.class)
                                    .collectList()
                                    .block();

                    liveDataResponse.addAll(liveDataResponseinWork);

                    if (liveDataResponse != null) {
                        Set<String> uniqueShopOrders = new HashSet<>();
                        Set<String> uniquePCUS = new HashSet<>();

                        // Iterate through the list and add shopOrder values to the Set
                        for (PcuInQueue data : liveDataResponse) {
                            uniqueShopOrders.add(data.getShopOrderBO());
                            uniquePCUS.add(data.getPcuBO().split(",")[1]);
                        }

                        // Convert the Set back to a List if needed
                        List<String> uniqueShopOrderList = new ArrayList<>(uniqueShopOrders);
                        List<String> uniquePCUList = new ArrayList<>(uniquePCUS);
                        existingRouting.getRoutingStepList().get(i).setShopordercount(String.valueOf(uniqueShopOrderList.size()));
                        existingRouting.getRoutingStepList().get(i).setPcucount(String.valueOf(uniquePCUList.size()));

                    } else {
                        throw new RoutingException(901, routing);
                    }
                }
            }
            if (existingRouting == null) {
                throw new RoutingException(502, routing, version);
            }
        } else {
            existingRouting = routingRepository.findByRoutingAndCurrentVersionAndSiteAndActive(routing, true, site, 1);
            if (existingRouting == null) {
                throw new RoutingException(502, routing, "currentVersion");
            }
        }
        return existingRouting;
    }

    @Override
    public AuditLogRequest createAuditLog(RoutingRequest routingRequest) {
        return AuditLogRequest.builder()
                .site(routingRequest.getSite())
                .action_code("ROUTING-CREATED")
                .action_detail("Routing Created " + routingRequest.getRouting() + "/" + routingRequest.getVersion())
                .action_detail_handle("ActionDetailBO:" + routingRequest.getSite() + "," + "ROUTING-CREATED" + routingRequest.getRouting() + ":" + "com.rits.routingservice.service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(routingRequest.getRouting())
                .operation_revision("*")
                .txnId("ROUTING-CREATED" + LocalDateTime.now() + routingRequest.getRouting())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .router(routingRequest.getRouting())
                .router_revision(routingRequest.getRouting())
                .category("Create")
                .activity("From Service")
                .topic("audit-log")
                .build();
    }

    @Override
    public AuditLogRequest updateAuditLog(RoutingRequest routingRequest) {
        return AuditLogRequest.builder()
                .site(routingRequest.getSite())
                .action_code("ROUTING-UPDATED")
                .action_detail("Routing Updated " + routingRequest.getRouting() + "/" + routingRequest.getVersion())
                .action_detail_handle("ActionDetailBO:" + routingRequest.getSite() + "," + "ROUTING-UPDATED" + routingRequest.getRouting() + ":" + "com.rits.routingservice.service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(routingRequest.getRouting())
                .router(routingRequest.getRouting())
                .router_revision(routingRequest.getRouting())
                .operation_revision("*")
                .txnId("ROUTING-UPDATED" + LocalDateTime.now() + routingRequest.getRouting())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("Update")
                .activity("From Service")
                .topic("audit-log")
                .build();
    }

    @Override
    public AuditLogRequest deleteAuditLog(RoutingRequest routingRequest) {
        return AuditLogRequest.builder()
                .site(routingRequest.getSite())
                .action_code("ROUTING-DELETED")
                .action_detail("Routing Deleted " + routingRequest.getRouting() + "/" + routingRequest.getVersion())
                .action_detail_handle("ActionDetailBO:" + routingRequest.getSite() + "," + "ROUTING-DELETED" + routingRequest.getRouting() + ":" + "com.rits.routingservice.service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(routingRequest.getRouting())
                .router(routingRequest.getRouting())
                .router_revision(routingRequest.getRouting())
                .operation_revision("*")
                .txnId("ROUTING-DELETED" + LocalDateTime.now() + routingRequest.getRouting())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("Delete")
                .activity("From Service")
                .topic("audit-log")
                .build();
    }

    @Override
    public List<RoutingType> retrieveByType(RoutingRequest routingRequest) {
        List<RoutingType> routingResponse = new ArrayList<>();
        if (routingRequest.getRoutingType() != null && !routingRequest.getRoutingType().isEmpty() || routingRequest.getSite() != null && !routingRequest.getSite().isEmpty() || routingRequest.getRouting() != null && !routingRequest.getRouting().isEmpty()) {
           routingResponse = routingRepository.findByActiveAndSiteAndRoutingTypeContainingIgnoreCase(1, routingRequest.getSite(), routingRequest.getRoutingType());
            if (routingResponse.isEmpty()) {
                throw new RoutingException(502, routingRequest.getRouting(), "currentVersion");
            }
        }
        return routingResponse;
    }





}

