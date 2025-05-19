package com.rits.workcenterservice.service;

import com.rits.operationservice.dto.AuditLogRequest;
import com.rits.workcenterservice.dto.*;
import com.rits.workcenterservice.exception.WorkCenterException;
import com.rits.workcenterservice.model.Association;
import com.rits.workcenterservice.model.MessageDetails;
import com.rits.workcenterservice.model.MessageModel;
import com.rits.workcenterservice.model.WorkCenter;
import com.rits.workcenterservice.repository.WorkCenterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class WorkCenterServiceImpl implements WorkCenterService {
    private final WorkCenterRepository workCenterRepository;
    private final WebClient.Builder webClientBuilder;
    private final ApplicationEventPublisher eventPublisher;
    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;

    @Value("${resource-service.url}/isExist")
    private String isResourceExistUrl;
    @Value("${auditlog-service.url}/producer")
    private String auditlogUrl;

    @Override
    public MessageModel createWorkCenter(WorkCenterRequest workCenterRequest) throws Exception {

        if (workCenterRepository.existsByWorkCenterAndSiteAndActiveEquals(workCenterRequest.getWorkCenter(), workCenterRequest.getSite(), 1)) {
            throw new WorkCenterException(601, workCenterRequest.getWorkCenter());
        } else {
            if (workCenterRequest.getDefaultParentWorkCenter() != null && !workCenterRequest.getDefaultParentWorkCenter().isEmpty() && !(workCenterRepository.existsByWorkCenterAndSiteAndActiveEquals(workCenterRequest.getDefaultParentWorkCenter(), workCenterRequest.getSite(), 1))) {
                throw new WorkCenterException(603, workCenterRequest.getDefaultParentWorkCenter());
            }
            if (workCenterRequest.getAssociationList() != null && !workCenterRequest.getAssociationList().isEmpty()) {
                List<Association> associationList = workCenterRequest.getAssociationList();
                for (Association association : associationList) {
//                    if (association.getType().equalsIgnoreCase("resource")) {
//                        IsExist isExist = IsExist.builder().site(workCenterRequest.getSite()).resource(association.getAssociateId()).build();
//                        Boolean resourceExist = webClientBuilder.build()
//                                .post()
//                                .uri(isResourceExistUrl)
//                                .bodyValue(isExist)
//                                .retrieve()
//                                .bodyToMono(Boolean.class)
//                                .block();
//                        if (!resourceExist) {
//                            throw new WorkCenterException(604,"");
//                        }
//                    }
                    if (association.getType().equalsIgnoreCase("Resource")) {

                        if (isWorkCenterExistWithAssociationList(association.getAssociateId(), workCenterRequest.getSite())) {
                            throw new WorkCenterException(610, association.getAssociateId());
                        }
                    }


                    if (association.getType().equalsIgnoreCase("workCenter")) {

                        if (!isWorkCenterExist(association.getAssociateId(), workCenterRequest.getSite())) {
                            throw new WorkCenterException(605, association.getAssociateId());
                        }
                    }
                }
            }

            if (workCenterRequest.getRouting() != null && !workCenterRequest.getRouting().isEmpty() &&
                    (workCenterRequest.getRoutingVersion() == null || workCenterRequest.getRoutingVersion().isEmpty())) {
                throw new WorkCenterException(606,workCenterRequest.getRoutingVersion());
            }
            if (workCenterRequest.getDescription() == null || workCenterRequest.getDescription().isEmpty()) {
                workCenterRequest.setDescription(workCenterRequest.getWorkCenter());
            }
            WorkCenter workCenter = WorkCenter.builder()
                    .site(workCenterRequest.getSite())
                    .workCenter(workCenterRequest.getWorkCenter())
                    .description(workCenterRequest.getDescription())
                    .status(workCenterRequest.getStatus())
                    .routing(workCenterRequest.getRouting())
                    .routingVersion(workCenterRequest.getRoutingVersion())
                    .workCenterCategory(workCenterRequest.getWorkCenterCategory())
                    .activityHookList(workCenterRequest.getActivityHookList())
                    .erpWorkCenter(workCenterRequest.getErpWorkCenter())
                    .addAsErpWorkCenter(workCenterRequest.isAddAsErpWorkCenter())
                    .defaultParentWorkCenter(workCenterRequest.getDefaultParentWorkCenter())
                    .associationList(workCenterRequest.getAssociationList())
                    .customDataList(workCenterRequest.getCustomDataList())
                    .inUse(workCenterRequest.isInUse())
                    .trackOee(workCenterRequest.isTrackOee()) // Include trackOee
                    .active(1)
                    .createdDateTime(LocalDateTime.now())
                    .handle("WorkCenterBo:" + workCenterRequest.getSite() + "," + workCenterRequest.getWorkCenter())
                    .build();

            return MessageModel.builder().message_details(new MessageDetails(workCenter.getWorkCenter() + " Created SuccessFully", "S")).response(workCenterRepository.save(workCenter)).build();
        }
    }

    private boolean isWorkCenterExistWithAssociationList(String associateId, String site) {
        if (workCenterRepository.findByActiveAndSiteAndAssociationListTypeAndAssociationListAssociateId(1, site, "Resource", associateId)!=null) {
            return true;
        }
        return false;
    }

    private boolean isWorkCenterExistWithAssociationList(String associateId, String site, String excludedWorkCenter) {
        WorkCenter workCenter = workCenterRepository.findByActiveAndSiteAndAssociationListTypeAndAssociationListAssociateId(1, site, "Resource", associateId);

        if (workCenter != null) {
            if (!workCenter.getWorkCenter().equals(excludedWorkCenter)) {  // Exclude the specified workcenter
                return true;
            }
        }
        return false;
    }

    @Override
    public MessageModel updateWorkCenter(WorkCenterRequest workCenterRequest) throws Exception {
        if (workCenterRepository.existsByWorkCenterAndSiteAndActiveEquals(workCenterRequest.getWorkCenter(), workCenterRequest.getSite(), 1)) {
            if (workCenterRequest.getDefaultParentWorkCenter() != null && !workCenterRequest.getDefaultParentWorkCenter().isEmpty() && !(workCenterRepository.existsByWorkCenterAndSiteAndActiveEquals(workCenterRequest.getDefaultParentWorkCenter(), workCenterRequest.getSite(), 1))) {
                throw new WorkCenterException(603, workCenterRequest.getDefaultParentWorkCenter());
            }
            if (workCenterRequest.getAssociationList() != null && !workCenterRequest.getAssociationList().isEmpty()) {
                List<Association> associationList = workCenterRequest.getAssociationList();
                for (Association association : associationList
                ) {
                    if (association.getType().equalsIgnoreCase("Resource")) {
                        String currentWorkCenter = workCenterRequest.getWorkCenter();

                        if (isWorkCenterExistWithAssociationList(association.getAssociateId(), workCenterRequest.getSite(), currentWorkCenter)) {
                            throw new WorkCenterException(610, association.getAssociateId());
                        }
                    }

                    if (association.getType().equalsIgnoreCase("workCenter")) {

                        if (!isWorkCenterExist(association.getAssociateId(), workCenterRequest.getSite())) {
                            throw new WorkCenterException(605, association.getAssociateId());
                        }
                    }
                }
            }
            if (workCenterRequest.getRouting() != null && !workCenterRequest.getRouting().isEmpty() &&
                    (workCenterRequest.getRoutingVersion() == null || workCenterRequest.getRoutingVersion().isEmpty())) {
                throw new WorkCenterException(606,workCenterRequest.getRoutingVersion());
            }

            if (workCenterRequest.getDescription() == null || workCenterRequest.getDescription().isEmpty()) {
                workCenterRequest.setDescription(workCenterRequest.getWorkCenter());
            }
            WorkCenter existingWorkCenter = workCenterRepository.findByWorkCenterAndActiveAndSite(workCenterRequest.getWorkCenter(), 1, workCenterRequest.getSite());
            WorkCenter updatedWorkCenter = WorkCenter.builder()
                    .site(existingWorkCenter.getSite())
                    .workCenter(existingWorkCenter.getWorkCenter())
                    .description(workCenterRequest.getDescription())
                    .status(workCenterRequest.getStatus())
                    .routing(workCenterRequest.getRouting())
                    .routingVersion(workCenterRequest.getRoutingVersion())
                    .workCenterCategory(workCenterRequest.getWorkCenterCategory())
                    .erpWorkCenter(workCenterRequest.getErpWorkCenter())
                    .addAsErpWorkCenter(workCenterRequest.isAddAsErpWorkCenter())
                    .activityHookList(workCenterRequest.getActivityHookList())
                    .defaultParentWorkCenter(workCenterRequest.getDefaultParentWorkCenter())
                    .associationList(workCenterRequest.getAssociationList())
                    .customDataList(workCenterRequest.getCustomDataList())
                    .active(1)
                    .inUse(workCenterRequest.isInUse())
                    .trackOee(workCenterRequest.isTrackOee()) // Include trackOee
                    .createdDateTime(existingWorkCenter.getCreatedDateTime())
                    .modifiedDateTime(LocalDateTime.now())
                    .handle(existingWorkCenter.getHandle())
                    .build();

            return MessageModel.builder().message_details(new MessageDetails(workCenterRequest.getWorkCenter() + " updated SuccessFully", "S")).response(workCenterRepository.save(updatedWorkCenter)).build();

        } else {
            throw new WorkCenterException(602, workCenterRequest.getWorkCenter());
        }

    }

    @Override
    public WorkCenter retrieveWorkCenter(String workCenter, String site) throws Exception {
        if (workCenterRepository.existsByWorkCenterAndSiteAndActiveEquals(workCenter, site, 1)) {
            return workCenterRepository.findByWorkCenterAndActiveAndSite(workCenter, 1, site);
        }
        throw new WorkCenterException(602, workCenter);
    }

    //if
    @Override
    public MessageModel deleteWorkCenter(String workCenter, String site) throws Exception {
        if (workCenterRepository.existsByWorkCenterAndSiteAndActiveEquals(workCenter, site, 1)) {
            WorkCenter existingWorkCenter = workCenterRepository.findByWorkCenterAndActiveAndSite(workCenter, 1, site);
            if (!existingWorkCenter.isInUse()) {
                existingWorkCenter.setActive(0);
                workCenterRepository.save(existingWorkCenter);

                return MessageModel.builder().message_details(new MessageDetails(workCenter + " deleted SuccessFully", "S")).build();

            } else {
                throw new WorkCenterException(609);
            }
        } else {
            throw new WorkCenterException(602, workCenter);
        }
    }

    @Override
    public Boolean isWorkCenterExist(String workCenter, String site) throws Exception {
        if (workCenterRepository.existsByWorkCenterAndSiteAndActiveEquals(workCenter, site, 1)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public WorkCenterResponseList getAllWorkCenterList(String workCenter, String site) throws Exception {
        List<WorkCenterResponse> workCenterResponses;
        if (workCenter != null && !workCenter.isEmpty()) {
            workCenterResponses = workCenterRepository.findByWorkCenterContainingIgnoreCaseAndSiteAndActiveEquals(workCenter, site, 1);


            if (workCenterResponses.isEmpty()) {
                throw new WorkCenterException(602, workCenter);
            }
            return WorkCenterResponseList.builder().workCenterList(workCenterResponses).build();
        } else {
            List<WorkCenterResponse> workCenterResponse = workCenterRepository.findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(1, site);
            return WorkCenterResponseList.builder().workCenterList(workCenterResponse).build();
        }
    }

    @Override
    public WorkCenterResponseList retrieveTop50(String site) throws Exception {
        List<WorkCenterResponse> reasonCodeList = workCenterRepository.findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(1, site);
        return WorkCenterResponseList.builder().workCenterList(reasonCodeList).build();
    }


    @Override
    public WorkCenterResponseList getErpWorkCenterList(String site) throws Exception {
        List<WorkCenterResponse> workCenterResponses;
        workCenterResponses = workCenterRepository.findByActiveAndSiteAndAddAsErpWorkCenter(1, site, true);
        return WorkCenterResponseList.builder().workCenterList(workCenterResponses).build();
    }

    @Override
    public WorkCenterResponseList getListOfWorkCenter(String workCenterCategory, String site) throws Exception {
        List<WorkCenterResponse> workCenterResponses = workCenterRepository.findByWorkCenterCategoryAndActiveAndSite(workCenterCategory, 1, site);
        if (!(workCenterResponses.isEmpty())) {
            return WorkCenterResponseList.builder().workCenterList(workCenterResponses).build();
        }
        throw new WorkCenterException(602, "workCenterCategory :" + workCenterCategory);

    }

    @Override
    public List<Association> associateObjectToWorkCenter(String workCenter, String site, List<Association> associationList) throws Exception {
        WorkCenter existingWorkCenter = workCenterRepository.findByWorkCenterAndActiveAndSite(workCenter, 1, site);
        if (existingWorkCenter == null) {
            throw new WorkCenterException(602, workCenter);
        }
        if (existingWorkCenter.getAssociationList() == null) {
            existingWorkCenter.setAssociationList(associationList);
        }
        List<Association> existingAssociations = existingWorkCenter.getAssociationList();
        for (Association newAssociation : associationList) {
            boolean alreadyExists = existingAssociations.stream()
                    .anyMatch(existingAssociation -> existingAssociation.getAssociateId().equals(newAssociation.getAssociateId()));
            if (!alreadyExists) {
                existingAssociations.add(newAssociation);
            }
        }
        existingWorkCenter.setAssociationList(existingAssociations);
        existingWorkCenter.setModifiedDateTime(LocalDateTime.now());
        workCenterRepository.save(existingWorkCenter);

        return existingAssociations;
    }


    @Override
    public List<Association> removeObjectFromWorkCenter(String workCenter, String site, List<String> sequences) throws Exception {
        WorkCenter existingWorkCenter = workCenterRepository.findByWorkCenterAndActiveAndSite(workCenter, 1, site);
        if (existingWorkCenter == null) {
            throw new WorkCenterException(602, workCenter);
        }
        if (existingWorkCenter.getAssociationList() == null) {
            throw new WorkCenterException(607, workCenter);
        }
        List<Association> existingAssociations = existingWorkCenter.getAssociationList();
        for (String sequence : sequences) {
            if (!existingAssociations.stream().anyMatch(association -> association.getSequence().equalsIgnoreCase(sequence))) {
                throw new WorkCenterException(607, sequence);
            }
        }
        existingAssociations.removeIf(association -> sequences.contains(association.getSequence()));
        existingWorkCenter.setAssociationList(existingAssociations);
        existingWorkCenter.setModifiedDateTime(LocalDateTime.now());
        workCenterRepository.save(existingWorkCenter);
        return existingWorkCenter.getAssociationList();
    }


    @Override
    public Association findDefaultResourceForWorkCenter(String workCenter, String site) throws Exception {
        WorkCenter existingWorkCenter = workCenterRepository.findByWorkCenterAndActiveAndSite(workCenter, 1, site);
        if (existingWorkCenter != null) {
            List<Association> associations = existingWorkCenter.getAssociationList();
            Optional<Association> defaultAssociation = associations.stream()
                    .filter(Association::isDefaultResource)
                    .findFirst();
            return defaultAssociation.orElseThrow(() -> new WorkCenterException(607, workCenter));
        }
        throw new WorkCenterException(607, workCenter);
    }

    @Override
    public Response getParentWorkCenter(String workCenter, String site) throws Exception {
        if (workCenterRepository.existsByWorkCenterAndSiteAndActiveEquals(workCenter, site, 1)) {
            WorkCenter existingWorkCenter = workCenterRepository.findByWorkCenterAndActiveAndSite(workCenter, 1, site);
            Response response = Response.builder().message(existingWorkCenter.getDefaultParentWorkCenter()).build();
            return response;
        } else {
            Response response = Response.builder().error("workCenter" + workCenter + " doesn't exist").build();
            return response;
        }
    }

//    @Override
//    public String callExtension(Extension extension) throws Exception {
//        String extensionResponse = webClientBuilder.build()
//                .post()
//                .uri(extensionUrl)
//                .bodyValue(extension)
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();
//        if (extensionResponse == null) {
//            throw new WorkCenterException(800);
//        }
//        return extensionResponse;
//    }

    @Override
    public AvailableWorkCenterList getAllAvailableWorkCenter(String site, String workCenter) throws Exception {
        List<AvailableWorkCenter> availableWorkCenters = workCenterRepository.findByActiveAndSite(1, site);
        if (workCenter != null && !workCenter.isEmpty()) {
            WorkCenter existingWorkCenter = workCenterRepository.findByWorkCenterAndActiveAndSite(workCenter, 1, site);
            if (existingWorkCenter == null) {
                throw new WorkCenterException(602, workCenter);
            }
            List<Association> associationList = existingWorkCenter.getAssociationList();
            if (associationList != null && !associationList.isEmpty()) {

                List<String> associationIds = associationList.stream().map(Association::getAssociateId).collect(Collectors.toList());
                availableWorkCenters.removeIf(workCenters -> associationIds.contains(workCenters.getWorkCenter()));
            }
        }
        return AvailableWorkCenterList.builder().availableWorkCenterList(availableWorkCenters).build();
    }

    @Override
    public String getWorkCenterByResource(String site, String resource) {
        WorkCenter center = workCenterRepository.findByActiveAndSiteAndAssociationListTypeAndAssociationListAssociateId(1, site, "Resource", resource);
        String workCenter = null;
        if (center != null && center.getWorkCenter() != null && !center.getWorkCenter().isEmpty()) {
            workCenter = center.getWorkCenter();
        }
        return workCenter;
    }

    @Override
    public AuditLogRequest createAuditLog(WorkCenterRequest workCenterRequest) {
        return AuditLogRequest.builder()
                .site(workCenterRequest.getSite())
                .action_code("WORK-CENTER-CREATED")
                .action_detail("WorkCenter Created " + workCenterRequest.getWorkCenter())
                .action_detail_handle("ActionDetailBO:" + workCenterRequest.getSite() + "," + "WORK-CENTER-CREATED" + workCenterRequest.getWorkCenter() + ":" + "com.rits.workcenterservice.service")
                .activity("From Service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(workCenterRequest.getWorkCenter())
                .operation_revision("*")
                .work_center(workCenterRequest.getWorkCenter())
                .router(workCenterRequest.getRouting())
                .router_revision(workCenterRequest.getRoutingVersion())
                .txnId("WORK-CENTER-CREATED" + (LocalDateTime.now()) + workCenterRequest.getWorkCenter())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("WORK_CENTER")
                .topic("audit-log")
                .build();
    }

    @Override
    public AuditLogRequest updateAuditLog(WorkCenterRequest workCenterRequest) {
        return AuditLogRequest.builder()
                .site(workCenterRequest.getSite())
                .action_code("WORK-CENTER-UPDATED")
                .action_detail("WorkCenter Updated " + workCenterRequest.getWorkCenter())
                .action_detail_handle("ActionDetailBO:" + workCenterRequest.getSite() + "," + "WORK-CENTER-UPDATED" + workCenterRequest.getWorkCenter() + ":" + "com.rits.workcenterservice.service")
                .activity("From Service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(workCenterRequest.getWorkCenter())
                .work_center(workCenterRequest.getWorkCenter())
                .router(workCenterRequest.getRouting())
                .router_revision(workCenterRequest.getRoutingVersion())
                .txnId("WORK-CENTER-UPDATED" + String.valueOf(LocalDateTime.now()) + workCenterRequest.getWorkCenter())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("WORK_CENTER")
                .topic("audit-log")
                .build();

    }
    @Override
    public AuditLogRequest deleteAuditLog(RetrieveRequest workCenterRequest) {
    return AuditLogRequest.builder()
            .site(workCenterRequest.getSite())
            .action_code("WORK-CENTER-DELETED")
            .action_detail("WorkCenter Deleted")
            .action_detail_handle("ActionDetailBO:"+workCenterRequest.getSite()+","+"WORK-CENTER-DELETED"+workCenterRequest.getWorkCenter()+":"+"com.rits.workcenterservice.service")
            .activity("From Service")
            .date_time(String.valueOf(LocalDateTime.now()))
            .userId(workCenterRequest.getWorkCenter())
            .work_center(workCenterRequest.getWorkCenter())
            .router(workCenterRequest.getWorkCenter())
            .router_revision(workCenterRequest.getWorkCenter())
            .txnId("WORK-CENTER-DELETED"+String.valueOf(LocalDateTime.now())+workCenterRequest.getWorkCenter())
            .created_date_time(String.valueOf(LocalDateTime.now()))
            .category("Delete")
            .topic("audit-log")
            .build();

    }

    @Override
    public List<WorkCenter> getTrackOeeWorkCenters(String site) throws Exception {
        //List<WorkCenter> trackOeeWorkCenters = workCenterRepository.findByTrackOeeAndActiveAndSite(true, 1, site);

        // Use the repository method that filters by workCenterCategory.
        List<WorkCenter> trackOeeWorkCenters = workCenterRepository
                .findByTrackOeeAndActiveAndSiteAndWorkCenterCategory(true, 1, site, "Line");

        if (trackOeeWorkCenters.isEmpty()) {
            throw new WorkCenterException(602, "No work centers with trackOee enabled found for site: " + site);
        }
        return trackOeeWorkCenters;
    }

    @Override
    public String getCellForWorkcenter(String childWorkCenterId, String site) throws WorkCenterException {
        WorkCenter cell = workCenterRepository.findCellForLine(site, childWorkCenterId);
        if (cell == null) {
            throw new WorkCenterException(601, "No Cell found for workcenter id: " + childWorkCenterId + " at site: " + site);
        }
        return cell.getWorkCenter(); // Return "TABLET", for example.
    }

    @Override
    public String getCellGroupForCell(String cellId, String site) throws WorkCenterException {
        WorkCenter cellGroup = workCenterRepository.findCellGroupForCell(site, cellId);
        if (cellGroup == null) {
            throw new WorkCenterException(602, "No Cell Group found for cell id: " + cellId + " at site: " + site);
        }
        return cellGroup.getWorkCenter(); // Return "MAKALI", for example.
    }


}