package com.rits.operationservice.service;
import com.rits.operationservice.dto.*;
import com.rits.operationservice.exception.*;
import com.rits.operationservice.model.*;
import com.rits.resourceservice.dto.ResourceTypeRequest;
import com.rits.operationservice.repository.OperationRepository;
import com.rits.resourceservice.model.Resource;
import com.rits.resourceservice.service.ResourceService;
import com.rits.resourcetypeservice.Model.ResourceMemberList;
import com.rits.resourcetypeservice.Model.ResourceType;
import com.rits.resourcetypeservice.Service.ResourceTypeService;
import com.rits.routingservice.model.PcuInQueue;
import com.rits.workcenterservice.service.WorkCenterService;
import lombok.RequiredArgsConstructor;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;



import java.time.LocalDateTime;
import java.util.*;


@Service
@RequiredArgsConstructor
public class OperationServiceImpl implements OperationService {
    private final OperationRepository operationRepository;
    private final WebClient.Builder webClientBuilder;
    private final ResourceService resourceService;
    private final ResourceTypeService resourceTypeService;
    private final WorkCenterService workCenterService;
    private final MessageSource localMessageSource;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }


    //    @Value("${resourceType-service.url}/isExist")
//    private String resourceTypeExistsUrl;

    @Value("${resource.type.uri}/retrieveByResourceType")
    private String resourceTypeUrl;

    @Value("${resource-service.url}/retrieveResourceDiscription")
    private String resourceServiceUrl;

    @Value("${pod-service.url}/retrieveAll")
    private String podServiceUrl;

    @Override
    public OperationMessageModel createOperation(OperationRequest operationRequest) throws Exception{
        if(operationRepository.existsByOperationAndRevisionAndSiteAndActive(operationRequest.getOperation(), operationRequest.getRevision(), operationRequest.getSite(),1)){
//            return updateOperation(operationRequest);
            throw new OperationException(2300);
        }
        try{
            updateCurrentVersionToFalse(operationRequest);
            getValidated(operationRequest);
        }catch(OperationException operationException){
            throw operationException;
        }catch(Exception e){
            throw e;
        }
        updateDescriptionIfEmpty(operationRequest);




            Operation createOperation = createOperationBuilder(operationRequest);
        String createdMessage = getFormattedMessage(21,operationRequest.getOperation(),operationRequest.getRevision());


        return OperationMessageModel.builder().message_details(new MessageDetails(createdMessage, "S")).response(operationRepository.save(createOperation)).build();
        }

    private void getValidated(OperationRequest operationRequest) throws Exception {
        getValidate(operationRequest);
        isResorceExist(operationRequest);
        isResourceTypeExist(operationRequest);
        isWorkCenterExist(operationRequest);

    }

    private void isWorkCenterExist(OperationRequest operationRequest) throws Exception {
        if(operationRequest.getWorkCenter()!=null && !operationRequest.getWorkCenter().isEmpty()){
            if(!workCenterService.isWorkCenterExist(operationRequest.getWorkCenter(), operationRequest.getSite())){
                throw new OperationException(600,operationRequest.getWorkCenter());
            }
        }
    }

    private void isResourceTypeExist(OperationRequest operationRequest) throws Exception {
        if(operationRequest.getResourceType()!=null&&!operationRequest.getResourceType().isEmpty()) {
            if (!resourceTypeService.isResourceTypeExist(operationRequest.getResourceType(), operationRequest.getSite())) {
                throw new OperationException(2400, operationRequest.getResourceType());
            }
        }
    }

    private void isResorceExist(OperationRequest operationRequest) throws Exception {
        if(operationRequest.getDefaultResource()!=null&&!operationRequest.getDefaultResource().isEmpty()){
            if(!(resourceService.isResourceExist(operationRequest.getDefaultResource() ,operationRequest.getSite()))){
                throw new OperationException(9301,operationRequest.getDefaultResource());
            }
        }
    }

    private void updateDescriptionIfEmpty(OperationRequest operationRequest) {
        if (operationRequest.getDescription() == null || operationRequest.getDescription().isEmpty()) {
            operationRequest.setDescription(operationRequest.getOperation());
        }
    }


    private Operation createOperationBuilder(OperationRequest operationRequest) {
        return Operation.builder()
                .site(operationRequest.getSite())
                .handle("OperationBO:" + operationRequest.getSite() + "," + operationRequest.getOperation()+","+operationRequest.getRevision())
                .operation(operationRequest.getOperation())
                .revision(operationRequest.getRevision())
                .description(operationRequest.getDescription())
                .status(operationRequest.getStatus())
                .operationType(operationRequest.getOperationType())
                .resourceType(operationRequest.getResourceType())
                .defaultResource(operationRequest.getDefaultResource())
                .erpOperation(operationRequest.getErpOperation())
                .addAsErpOperation(operationRequest.isAddAsErpOperation())
                .workCenter(operationRequest.getWorkCenter())
                .currentVersion(operationRequest.isCurrentVersion())
                .maxLoopCount(operationRequest.getMaxLoopCount())
                .certificationList(operationRequest.getCertificationList())
                .subStepList(operationRequest.getSubStepList())
                .activityHookList(operationRequest.getActivityHookList())
                .operationCustomDataList(operationRequest.getOperationCustomDataList())
                .active(1)
                .createdBy(operationRequest.getUserId())
                .createdDateTime(LocalDateTime.now())
                .build();
    }

    private void updateCurrentVersionToFalse(OperationRequest operationRequest) {


        if(operationRequest.isCurrentVersion()) {
            List<Operation> existingOperations = operationRepository.findByOperationAndSiteAndActive(operationRequest.getOperation(), operationRequest.getSite(), 1);


            existingOperations.stream()
                    .filter(Operation::isCurrentVersion)
                    .map(operations -> {
                        operations.setCurrentVersion(false);
                        operations.setModifiedDateTime(LocalDateTime.now());
                        return operations;
                    })
                    .forEach(operationRepository::save);
        }

        }


    @Override
    public OperationResponseList getOperationListByCreationDate(String site) throws Exception {
        List<OperationResponse> operationResponses=operationRepository.findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(1,site);
        return OperationResponseList.builder().operationList(operationResponses).build();
    }
    @Override
    public OperationResponseList getOperationList(String site,String operation) throws Exception {
        if (operation != null && !operation.isEmpty()) {
            List<OperationResponse> operationResponses = operationRepository.findByOperationContainingIgnoreCaseAndSiteAndActive(operation, site,1);
            if (operationResponses != null && !operationResponses.isEmpty()) {
                return OperationResponseList.builder().operationList(operationResponses).build();
            } else {
                throw new OperationException(2301,operation);
            }
        } else {
            return getOperationListByCreationDate(site);
        }
    }
    @Override
    public Operation retrieveOperation(String site,String operation,String revision) throws Exception{
        Operation operation1=null;
        if(revision!=null&& !revision.isEmpty()) {
            operation1= operationRepository.findByOperationAndRevisionAndSiteAndActive(operation, revision, site, 1);



        }else{
            operation1=operationRepository.findByOperationAndCurrentVersionAndSiteAndActive(operation,true,site,1);
        }
        if(operation1!=null){
            return operation1;
        }else{
            throw new OperationException(2301,operation);
        }
    }
    @Override
    public OperationMessageModel updateOperation(OperationRequest operationRequest) throws Exception{
        if(operationRepository.existsByOperationAndRevisionAndSiteAndActive(operationRequest.getOperation(), operationRequest.getRevision(), operationRequest.getSite(),1)){
        Operation operation = operationRepository.findByOperationAndRevisionAndSiteAndActive(operationRequest.getOperation(), operationRequest.getRevision(), operationRequest.getSite(),1);
            try{
                getValidated(operationRequest);
                updateCurrentVersionToFalse(operationRequest);
            }catch(OperationException operationException){
                throw operationException;
            }catch(Exception e){
                throw e;
            }
            updateDescriptionIfEmpty(operationRequest);

            Operation updateOperation = updateOperationBuilder(operation,operationRequest);
            String updatedMessage = getFormattedMessage(22,operationRequest.getOperation(),operationRequest.getRevision());

            return OperationMessageModel.builder().message_details(new MessageDetails(updatedMessage, "S")).response(operationRepository.save(updateOperation)).build();



        } else {
            throw new OperationException(2301,operationRequest.getOperation());
        }
    }

    private void getValidate(OperationRequest operationRequest) {
        if(operationRequest.getRevision() == null || operationRequest.getRevision().isEmpty()){
            throw new OperationException(107);
        }if(operationRequest.getUserId()==null || operationRequest.getUserId().isEmpty()){
            throw new OperationException(108);
        }
    }

    private Operation updateOperationBuilder(Operation operation, OperationRequest operationRequest) {
        return Operation.builder()
                .site(operation.getSite())
                .handle(operation.getHandle())
                .operation(operation.getOperation())
                .revision(operation.getRevision())
                .description(operationRequest.getDescription())
                .status(operationRequest.getStatus())
                .operationType(operationRequest.getOperationType())
                .resourceType(operationRequest.getResourceType())
                .defaultResource(operationRequest.getDefaultResource())
                .erpOperation(operationRequest.getErpOperation())
                .workCenter(operationRequest.getWorkCenter())
                .currentVersion(operationRequest.isCurrentVersion())
                .addAsErpOperation(operationRequest.isAddAsErpOperation())
                .maxLoopCount(operationRequest.getMaxLoopCount())
                .certificationList(operationRequest.getCertificationList())
                .subStepList(operationRequest.getSubStepList())
                .activityHookList(operationRequest.getActivityHookList())
                .operationCustomDataList(operationRequest.getOperationCustomDataList())
                .active(1)
                .createdBy(operation.getCreatedBy())
                .modifiedBy(operationRequest.getUserId())
                .createdDateTime(operation.getCreatedDateTime())
                .modifiedDateTime(LocalDateTime.now())
                .build();
    }

    @Override
    public OperationMessageModel deleteOperation(String operation,String revision,String site,String userId) throws Exception{
        if(revision == null || revision.isEmpty()){
            throw new OperationException(107);
        }if(userId==null || userId.isEmpty()){
            throw new OperationException(108);
        }
        if (operationRepository.existsByOperationAndRevisionAndSiteAndActive(operation,revision,site, 1)) {
            Operation exisitngOperation=operationRepository.findByOperationAndRevisionAndActiveAndSite(operation,revision,1,site);
            exisitngOperation.setActive(0);
            exisitngOperation.setModifiedDateTime(LocalDateTime.now());
            exisitngOperation.setModifiedBy(userId);
            operationRepository.save(exisitngOperation);
            String deletedMessage = getFormattedMessage(23,operation,revision);
            return OperationMessageModel.builder().message_details(new MessageDetails(deletedMessage, "S")).build();


        } else {
            throw new OperationException(2301,operation);
        }
    }
    @Override
    public Boolean isOperationExist(String site,String operation,String revision) throws Exception {
        boolean operationExist;
        if(revision!=null&&!revision.isEmpty()){
            operationExist=operationRepository.existsByOperationAndRevisionAndSiteAndActive(operation,revision,site,1);//db.getCollection("operation").findOne({operation:"operationName"})
        }else{
            operationExist=operationRepository.existsByOperationAndCurrentVersionAndSiteAndActive(operation,true,site,1);
        }
        return operationExist;
    }



    @Override
    public Boolean isOperationExistByHandle(String site,String operation) throws Exception {
        return operationRepository.existsBySiteAndActiveAndHandle(site,1,operation);
    }



    @Override
    public OperationResponseList getOperationListByErpOperation(String site) throws Exception
    {
        return OperationResponseList.builder().operationList(operationRepository.findByActiveAndSiteAndAddAsErpOperation(1,site,true)).build();
    }

    @Override
    public OperationResponseList getAllOperation(String site) throws Exception {
        return OperationResponseList.builder().operationList(operationRepository.findBySiteAndActive(site,1)).build();
    }

    @Override
    public OperationResponse retrieveOperationByCurrentVersion(String site, String operation) throws Exception
    {
        OperationResponse response = operationRepository.findByActiveAndSiteAndOperationAndCurrentVersion(1, site, operation, true);
        if(response==null){
            throw new OperationException(2305, operation);
        }
        return response;
    }

    @Override
    public AuditLogRequest createAuditLog(OperationRequest operationRequest) {
        return AuditLogRequest.builder()
                .site(operationRequest.getSite())
                .action_code("OPERATION-CREATE")
                .action_detail("Operation Created "+operationRequest.getOperation()+"/"+operationRequest.getRevision())
                .action_detail_handle("ActionDetailBO:"+operationRequest.getSite()+","+"OPERATION-CREATE"+operationRequest.getUserId()+":"+"com.rits.operationservice.controller")
                .activity("From Service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(operationRequest.getUserId())
                .operation_revision("*")
                .work_center(operationRequest.getWorkCenter())
                .operation(operationRequest.getOperation())
                .resrce(operationRequest.getDefaultResource())
                .txnId("OPERATION-CREATE"+LocalDateTime.now()+operationRequest.getUserId())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("Create")
                .topic("audit-log")
                .build();
    }

    @Override
    public AuditLogRequest updateAuditLog(OperationRequest operationRequest) {
        return AuditLogRequest.builder()
                .site(operationRequest.getSite())
                .action_code("OPERATION-UPDATE")
                .action_detail("Operation Updated "+operationRequest.getOperation()+"/"+operationRequest.getRevision())
                .action_detail_handle("ActionDetailBO:"+operationRequest.getSite()+","+"OPERATION-UPDATE"+operationRequest.getUserId()+":"+"com.rits.operationservice.controller")
                .activity("From Service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(operationRequest.getUserId())
                .work_center(operationRequest.getWorkCenter())
                .resrce(operationRequest.getDefaultResource())
                .operation(operationRequest.getOperation())
                .txnId("OPERATION-UPDATE"+String.valueOf(LocalDateTime.now())+operationRequest.getUserId())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("Update")
                .topic("audit-log")
                .build();
    }

    @Override
    public AuditLogRequest deleteAuditLog(OperationRequest operationRequest) {
        return AuditLogRequest.builder()
                .site(operationRequest.getSite())
                .action_code("OPERATION-DELETE")
                .action_detail("Operation Deleted "+operationRequest.getOperation()+"/"+operationRequest.getRevision())
                .action_detail_handle("ActionDetailBO:"+operationRequest.getSite()+","+"OPERATION-DELETE"+operationRequest.getUserId()+":"+"com.rits.operationservice.controller")
                .activity("From Service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(operationRequest.getUserId())
                .work_center(operationRequest.getWorkCenter())
                .resrce(operationRequest.getDefaultResource())
                .operation(operationRequest.getOperation())
                .txnId("OPERATION-DELETED"+String.valueOf(LocalDateTime.now())+operationRequest.getUserId())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("Delete")
                .topic("audit-log")
                .build();
    }

    @Override
    public Operation retrieveByOperationAndSite(String site,String operation) throws Exception{
        Operation operation1=null;
        if(operation!=null&& !operation.isEmpty()) {
            operation1= operationRepository.findBySiteAndActiveAndOperationAndCurrentVersion(site, 1, operation, true);
        }
        if(operation1!=null){
            return operation1;
        }else{
            throw new OperationException(2301,operation);
        }
    }

    @Override
    public List<Operation> getOperationsBySite(OperationRequest operationRequest) {
        List<Operation> operations=operationRepository.findByOperationAndSiteAndActive(operationRequest.getOperation(),operationRequest.getSite(),1);
        if(operations==null || operations.isEmpty()){
            throw new OperationException(2305,operationRequest.getOperation());
        }
        return operations;
    }

//    @Override
//    public List<Operation> retrieveCertificateList(Operation operation){
//        List<Operation> certificationList = operationRepository.findBySiteAndOperationAndActive(operation.getOperation(), operation.getSite(),1);
//        return certificationList;
//    }
//
//    @Override
//    public List<AllResourceResponse> getResourceListByOp(String site, String operation, String storedUrlPodName){
//
//        AllResourceList allResourceList = new AllResourceList();
//        List<Operation> obj = operationRepository.findBySiteAndOperationAndActive(site, operation,1);// all operation data
//        ResourceTypeRequest resourceTypeRequest1 = ResourceTypeRequest.builder().site(site).resourceType(obj.get(0).getResourceType()).build();
//
//        ResourceType resourceType = webClientBuilder.build()
//                .post()
//                .uri(resourceTypeUrl)
//                .bodyValue(resourceTypeRequest1)
//                .retrieve()
//                .bodyToMono(ResourceType.class)
//                .block();// operation based = res lists
//
//        Set<String> resMemberList = new HashSet<>();
//        for(int i=0;i<resourceType.getResourceMemberList().size();i++){
//            resMemberList.add(resourceType.getResourceMemberList().get(i).getResource());
//        }
//
//        PodListRequest podListRequest = PodListRequest.builder().podName(storedUrlPodName).site(site).build();
//        PodResponseList podResponseList = webClientBuilder.build()
//                .post()
//                .uri(podServiceUrl)
//                .bodyValue(podListRequest)
//                .retrieve()
//                .bodyToMono(PodResponseList.class)
//                .block();
//
//        resMemberList.add(podResponseList.getPodList().get(0).getDefaultResource());
//        List<String> resList = new ArrayList<>(resMemberList);
//
//        ResourceTypeRequest resourceTypeRequest2 = ResourceTypeRequest.builder().site(site).resourceTypeList(resList).build();
//        Map<String, String> resAndDiscription = webClientBuilder.build()
//                .post()
//                .uri(resourceServiceUrl)
//                .bodyValue(resourceTypeRequest2)
//                .retrieve()
//                .bodyToMono(Map.class)  // response is a Map<String, String>
//                .block();
//
//        allResourceList.setAllResourceList(resAndDiscription);
//        return allResourceList.getAllResourceList();
//    }
}
// ----- When certificaion and substep service willbe done un comment this and use in appropriate place in craet method------



//  In application.properties gice this  #subStep-service.url=http://localhost:8084/app/v1/subStep-service
//#certification-service.url=http://localhost:8085/app/v1/certification-service



// before the method starts
//    @Value("${sub-step-service.url}/isExist")
//    private String subStepExistsUrl;
//    @Value("${certification-service.url}/isExist")
//    private String certificationExistsUrl;



//in create method use below code
//            // Check if the sub step exists
//            List<SubStep> subStepList = operationRequest.getSubStepList();
//            for (SubStep subStep : subStepList) {
//                String subStepBo = subStep.getSubStepBo();
//                Boolean subStepExists = webClientBuilder.build()
//                        .post()
//                        .uri(subStepExistsUrl)
//                        .bodyValue(subStepBo)
//                        .retrieve()
//                        .bodyToMono(Boolean.class)
//                        .block();
//                if (!subStepExists) {
//                    throw new SubStepNotFoundException("Sub step Bo", subStepBo);
//                }
//            }
//            // Check if the certification exists
//            List<Certification> certificationList = operationRequest.getCertificationList();
//            for (Certification certification : certificationList) {
//                String certificationBo = certification.getCertificationBo();
//                Boolean certificationExists = webClient
//                        .post()
//                        .uri(certificationExistsUrl)
//                        .bodyValue(certificationBo)
//                        .retrieve()
//                        .bodyToMono(Boolean.class)
//                        .block();
//                if (!certificationExists) {
//                    throw new CertificationNotFoundException("Certification Bo", certificationBo);
//                }
//            }