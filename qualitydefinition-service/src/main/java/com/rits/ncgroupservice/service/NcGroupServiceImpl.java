package com.rits.ncgroupservice.service;

import com.rits.buyoffservice.dto.AuditLogRequest;
import com.rits.ncgroupservice.dto.*;
import com.rits.ncgroupservice.exception.NcGroupException;
import com.rits.ncgroupservice.model.*;
import com.rits.ncgroupservice.repository.NcGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NcGroupServiceImpl implements NcGroupService {
    private final NcGroupRepository ncGroupRepository;
    private final WebClient.Builder webClientBuilder;
    private final MessageSource localMessageSource;
    @Value("${ncCode-service.url}/retrieveBySite")
    private String ncCodeUrl;

    @Value("${ncCode-service.url}/addNCGroups")
    private String associateNCGroupsToNCGroupsList;

    @Value("${ncCode-service.url}/removeNCGroups")
    private String removeNCGroupsToNCGroupsList;

    @Value("${operation-service.url}/retrieveBySite")
    private String operationUrl;
    @Value("${operation-service.url}/isExist")
    private String isOperationExist;
    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;
    @Value("${auditlog-service.url}/producer")
    private String auditlogUrl;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }


    @Override
    public NcGroupMessageModel createNcGroup(NcGroupRequest ncGroupRequest) throws Exception {
        if (ncGroupRepository.existsByActiveAndSiteAndNcGroup(1, ncGroupRequest.getSite(), ncGroupRequest.getNcGroup())) {
            throw new NcGroupException(3401, ncGroupRequest.getNcGroup());
        } else {
            try {
                getValidated(ncGroupRequest);
                updateNcCode(null,ncGroupRequest.getNcCodeDPMOCategoryList(),ncGroupRequest);
            } catch (NcGroupException ncGroupException) {
                throw ncGroupException;
            } catch (Exception e) {
                throw e;
            }


            NcGroup ncGroup = buildNcGroupFromRequest(ncGroupRequest);

            AuditLogRequest activityLog = buildAuditLogRequest(ncGroupRequest);

            sendAuditLog(activityLog);
            String createdMessage = getFormattedMessage(1, ncGroupRequest.getNcGroup());



            return NcGroupMessageModel.builder()
                    .message_details(new MessageDetails(createdMessage, "S"))
                    .response(ncGroupRepository.save(ncGroup))
                    .build();
        }
    }

    private void getValidated(NcGroupRequest ncGroupRequest) {
        validateUserId(ncGroupRequest.getUserId());


        if (ncGroupRequest.getDescription() == null || ncGroupRequest.getDescription().isEmpty()) {
            ncGroupRequest.setDescription(ncGroupRequest.getNcGroup());
        }

        if (ncGroupRequest.isValidAtAllOperations()) {
            List<Operation> operationList = getOperationList(ncGroupRequest);
            ncGroupRequest.setOperationList(operationList);
        }else{
            isOperationExist(ncGroupRequest);
        }

    }

    private void isOperationExist(NcGroupRequest ncGroupRequest) {
        List<Operation> operationsToValidate=ncGroupRequest.getOperationList();
        for(Operation operation: operationsToValidate){
            IsOperationExist isOperationExistRequest=IsOperationExist.builder().site(ncGroupRequest.getSite()).operation(operation.getOperation()).build();
            Boolean response=isOperationExistWebClient(isOperationExistRequest);
            if(!response){
                throw new NcGroupException(5003,operation.getOperation());
            }

        }
    }

    private Boolean isOperationExistWebClient(IsOperationExist isOperationExistRequest) {
        Boolean response= webClientBuilder.build()
                .post()
                .uri(isOperationExist)
                .bodyValue(isOperationExistRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return response;
    }

    private void validateUserId(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new NcGroupException(117);
        }
    }

    private NcGroup buildNcGroupFromRequest(NcGroupRequest ncGroupRequest) {
        return NcGroup.builder()
                .site(ncGroupRequest.getSite())
                .ncGroup(ncGroupRequest.getNcGroup())
                .handle("NcGroupBo:" + ncGroupRequest.getSite() + "," + ncGroupRequest.getNcGroup())
                .description(ncGroupRequest.getDescription())
                .ncGroupFilterPriority(ncGroupRequest.getNcGroupFilterPriority())
                .ncCodeDPMOCategoryList(ncGroupRequest.getNcCodeDPMOCategoryList())
                .validAtAllOperations(ncGroupRequest.isValidAtAllOperations())
                .operationList(ncGroupRequest.getOperationList())
                .active(1)
                .createdBy(ncGroupRequest.getUserId())
                .createdDateTime(LocalDateTime.now())
                .build();
    }

    private AuditLogRequest buildAuditLogRequest(NcGroupRequest ncGroupRequest) {
        return AuditLogRequest.builder()
                .site(ncGroupRequest.getSite())
                .change_stamp("Create")
                .action_code("NCGROUP-CREATE")
                .action_detail("NcGroup Created " + ncGroupRequest.getNcGroup())
                .action_detail_handle("ActionDetailBO:" + ncGroupRequest.getSite() + "," + "NCGROUP-CREATE" + "," + ncGroupRequest.getUserId() + ":" + "com.rits.ncgroupservice.service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(ncGroupRequest.getUserId())
                .txnId("NCGROUP-CREATE" + (LocalDateTime.now()) + ncGroupRequest.getUserId())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("NC_GROUP")
                .build();
    }

    private void sendAuditLog(AuditLogRequest activityLog) {
        webClientBuilder.build()
                .post()
                .uri(auditlogUrl)
                .bodyValue(activityLog)
                .retrieve()
                .bodyToMono(AuditLogRequest.class)
                .block();
    }


    private List<Operation> getOperationList(NcGroupRequest ncGroupRequest) {

        OperationResponseList operationResponse = webClientBuilder.build()
                .post()
                .uri(operationUrl)
                .bodyValue(ncGroupRequest)
                .retrieve()
                .bodyToMono(OperationResponseList.class)
                .block();
        if (operationResponse==null|| operationResponse.getOperationList()==null) {
            throw new NcGroupException(2000);
        }
        return operationResponse.getOperationList();

    }

    @Override
    public NcGroupMessageModel updateNcGroup(NcGroupRequest ncGroupRequest) throws Exception {
        if (ncGroupRepository.existsByActiveAndSiteAndNcGroup(1, ncGroupRequest.getSite(), ncGroupRequest.getNcGroup())) {

            NcGroup existingNcGroup = ncGroupRepository.findByActiveAndSiteAndNcGroup(1, ncGroupRequest.getSite(), ncGroupRequest.getNcGroup());


            List<NcCodeDPMOCategory> updatedNccodeList = ncGroupRequest.getNcCodeDPMOCategoryList();
            List<NcCodeDPMOCategory> existingNccodeList = existingNcGroup.getNcCodeDPMOCategoryList();
            try {
                getValidated(ncGroupRequest);
                updateNcCode(existingNccodeList,updatedNccodeList,ncGroupRequest);
            } catch (NcGroupException ncGroupException) {
                throw ncGroupException;
            } catch (Exception e) {
                throw e;
            }
            NcGroup ncGroup =updateBuilder(existingNcGroup,ncGroupRequest);
            AuditLogRequest activityLog = updateAuditLog(ncGroupRequest);
            sendAuditLog(activityLog);
            String updatedMessage = getFormattedMessage(2, ncGroupRequest.getNcGroup());


            return NcGroupMessageModel.builder().message_details(new MessageDetails(updatedMessage,"S")).response(ncGroupRepository.save(ncGroup)).build();

        } else {
            throw new NcGroupException(3402, ncGroupRequest.getNcGroup());
        }
    }

    private AuditLogRequest updateAuditLog(NcGroupRequest ncGroupRequest) {
        return AuditLogRequest.builder()
                .site(ncGroupRequest.getSite())
                .change_stamp("Update")
                .action_code("NCGROUP-UPDATE")
                .action_detail("NcGroup Updated "+ncGroupRequest.getNcGroup())
                .action_detail_handle("ActionDetailBO:"+ncGroupRequest.getSite()+","+"NCGROUP-UPDATE"+","+ncGroupRequest.getUserId()+":"+"com.rits.ncgroupservice.service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(ncGroupRequest.getUserId())
                .txnId("NCGROUP-UPDATE"+String.valueOf(LocalDateTime.now())+ncGroupRequest.getUserId())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("NC_GROUP")
                .build();
    }

    private NcGroup updateBuilder(NcGroup existingNcGroup, NcGroupRequest ncGroupRequest) {
        return  NcGroup.builder()
                .site(existingNcGroup.getSite())
                .ncGroup(existingNcGroup.getNcGroup())
                .handle(existingNcGroup.getHandle())
                .description(ncGroupRequest.getDescription())
                .ncGroupFilterPriority(ncGroupRequest.getNcGroupFilterPriority())
                .ncCodeDPMOCategoryList(ncGroupRequest.getNcCodeDPMOCategoryList())
                .validAtAllOperations(ncGroupRequest.isValidAtAllOperations())
                .operationList(ncGroupRequest.getOperationList())
                .active(1)
                .createdBy(existingNcGroup.getCreatedBy())
                .modifiedBy(ncGroupRequest.getUserId())
                .createdDateTime(existingNcGroup.getCreatedDateTime())
                .modifiedDateTime(LocalDateTime.now())
                .build();
    }

    private void updateNcCode(List<NcCodeDPMOCategory> existingNccodeList, List<NcCodeDPMOCategory> updatedNccodeList,NcGroupRequest ncGroupRequest) {
        if (existingNccodeList != null && !existingNccodeList.isEmpty()) {

            List<String> NcCodeToAdd = new ArrayList<>();
            List<String> NcCodeToRemove = new ArrayList();

            // Compare the updated user list with the existing list and perform necessary operations
            for (NcCodeDPMOCategory updatedNcCode : updatedNccodeList) {
                boolean alreadyExists = existingNccodeList.stream().anyMatch(user -> user.getNcCode().equals(updatedNcCode.getNcCode()));
                if (!alreadyExists) {
                    NcCodeToAdd.add(updatedNcCode.getNcCode());
                }
            }

            for (NcCodeDPMOCategory existNcCode : existingNccodeList) {
                boolean isRemoved = updatedNccodeList.stream().noneMatch(user -> user.getNcCode().equals(existNcCode.getNcCode()));
                if (isRemoved) {
                    NcCodeToRemove.add(existNcCode.getNcCode());
                }
            }

            List<String> NcCodeAddList = new ArrayList<>();
            NcCodeAddList.add(ncGroupRequest.getNcGroup());

            if (!NcCodeToAdd.isEmpty()) {
                for (String AddncCode : NcCodeToAdd) {
                    // Create a UserRequest for adding a user to the user group
                    NCGroupsListRequests addncGroupsListRequest =   NCGroupsListRequests.builder()
                            .site(ncGroupRequest.getSite())
                            .ncCode(AddncCode)
                            .ncGroup(NcCodeAddList)
                            .build();

                    Boolean ncCodeAdd = webClientBuilder.build()
                            .post()
                            .uri(associateNCGroupsToNCGroupsList)
                            .bodyValue(addncGroupsListRequest)
                            .retrieve()
                            .bodyToMono(Boolean.class)
                            .block();

                    // com.rits.userservice.model.User addUserResponse = userServiceImpl.addUserGroup(addUserRequest, userGroupList);

                    if (ncCodeAdd == null || !ncCodeAdd) {
                        throw new NcGroupException (3402, ncGroupRequest.getNcGroup());
                    }
                }
            }

            List<String> NcCodeRemovList = new ArrayList<>();
            NcCodeRemovList.add(ncGroupRequest.getNcGroup());

            if (!NcCodeToRemove.isEmpty()) {
                for (String RemovncCode : NcCodeToRemove) {

                    NCGroupsListRequests removncGroupsListRequest = NCGroupsListRequests.builder()
                            .site(ncGroupRequest.getSite())
                            .ncCode(RemovncCode)
                            .ncGroup(NcCodeRemovList)
                            .build();

                    Boolean ncCodeRemov = webClientBuilder.build()
                            .post()
                            .uri(removeNCGroupsToNCGroupsList)
                            .bodyValue(removncGroupsListRequest)
                            .retrieve()
                            .bodyToMono(Boolean.class)
                            .block();

                    //com.rits.userservice.model.User removeUserResponse = userServiceImpl.removeUserGroup(removeUserRequest, userGroupRemovList);

                    if (ncCodeRemov == null || !ncCodeRemov) {
                        throw new NcGroupException (3402, ncGroupRequest.getNcGroup());
                    }
                }
            }
        }

        else {

            List<com.rits.licencevalidationservice.model.NCGroups> NCGroupLists = new ArrayList<>();
            com.rits.licencevalidationservice.model.NCGroups NCGroupReq =  com.rits.licencevalidationservice.model.NCGroups.builder().ncGroup(ncGroupRequest.getNcGroup()).build();
            NCGroupLists.add(NCGroupReq);

            if (!ncGroupRequest.getNcGroup().isEmpty()) {
                for (NcCodeDPMOCategory NcCode : ncGroupRequest.getNcCodeDPMOCategoryList()) {
                    List<String> NcGroup = new ArrayList<>();
                    NcGroup.add(NCGroupReq.getNcGroup());

                    NCGroupsListRequests addncGroupsListRequest = NCGroupsListRequests.builder()
                            .site(ncGroupRequest.getSite())
                            .ncCode(NcCode.getNcCode())
                            .ncGroup(NcGroup)
                            .build();

                    Boolean ncCodeAdd = webClientBuilder.build()
                            .post()
                            .uri(associateNCGroupsToNCGroupsList)
                            .bodyValue(addncGroupsListRequest)
                            .retrieve()
                            .bodyToMono(Boolean.class)
                            .block();

                    // com.rits.userservice.model.User addUserResponse = userServiceImpl.addUserGroup(addUserRequest, userGroupList);

                    if (ncCodeAdd == null|| !ncCodeAdd) {
                        throw new NcGroupException (3402, ncGroupRequest.getNcGroup());
                    }
                }
            }

        }
    }

    @Override
    public NcGroup retrieveNcGroup(String site, String ncGroup) throws Exception {
        NcGroup existingNcGroup = ncGroupRepository.findByActiveAndSiteAndNcGroup(1, site, ncGroup);
        if (existingNcGroup == null) {
            throw new NcGroupException(3402, ncGroup);
        }
        return existingNcGroup;
    }

    @Override
    public NcGroupResponseList getAllNCGroup(String site, String ncGroup) throws Exception {
        if (ncGroup != null && !ncGroup.isEmpty()) {
            List<NcGroupResponse> ncGroupResponses = ncGroupRepository.findByActiveAndSiteAndNcGroupContainingIgnoreCase(1, site, ncGroup);
            if (ncGroupResponses.isEmpty()) {
                throw new NcGroupException(3402, ncGroup);
            }
            return NcGroupResponseList.builder().ncGroupList(ncGroupResponses).build();
        } else {
            return getAllNCGroupByCreatedDate(site);
        }
    }

    @Override
    public NcGroupResponseList getAllNCGroupByCreatedDate(String site) throws Exception {
        List<NcGroupResponse> ncGroupResponses = ncGroupRepository.findByActiveAndSiteOrderByCreatedDateTimeDesc(1, site);
        return NcGroupResponseList.builder().ncGroupList(ncGroupResponses).build();
    }

    @Override
    public NcGroupMessageModel deleteNcGroup(String site, String ncGroup, String userId) throws Exception {
        if (userId == null || userId.isEmpty()) {
            throw new NcGroupException(117);
        }

        NcGroup existingNcGroup = ncGroupRepository.findByActiveAndSiteAndNcGroup(1, site, ncGroup);
        if (existingNcGroup == null) {
           throw new NcGroupException(3402, ncGroup);
        }
        if(existingNcGroup.getNcCodeDPMOCategoryList() != null && !existingNcGroup.getNcCodeDPMOCategoryList().isEmpty())
        {
            List<String> NcCodeRemovList = new ArrayList<>();
            NcCodeRemovList.add(ncGroup);
            for(NcCodeDPMOCategory ncCode : existingNcGroup.getNcCodeDPMOCategoryList())
            {
                NCGroupsListRequests removncGroupsListRequest = NCGroupsListRequests.builder()
                        .site(site)
                        .ncCode(ncCode.getNcCode())
                        .ncGroup(NcCodeRemovList)
                        .build();

                Boolean ncCodeRemov = webClientBuilder.build()
                        .post()
                        .uri(removeNCGroupsToNCGroupsList)
                        .bodyValue(removncGroupsListRequest)
                        .retrieve()
                        .bodyToMono(Boolean.class)
                        .block();

                if (ncCodeRemov == null || !ncCodeRemov) {
                    throw new NcGroupException (3402, ncGroup);
                }
            }
        }
        existingNcGroup.setActive(0);
        existingNcGroup.setModifiedBy(userId);
        existingNcGroup.setModifiedDateTime(LocalDateTime.now());
        ncGroupRepository.save(existingNcGroup);
        AuditLogRequest activityLog = deleteAuditLog(ncGroup,site,userId);
        sendAuditLog(activityLog);
        String deletedMessage = getFormattedMessage(3, ncGroup);



        return NcGroupMessageModel.builder().message_details(new MessageDetails(deletedMessage,"S")).build();

    }

    private AuditLogRequest deleteAuditLog(String ncGroup, String site, String userId) {
        return AuditLogRequest.builder()
                .site(site)
                .change_stamp("Delete")
                .action_code("NCGROUP-DELETE")
                .action_detail("NcGroup Updated "+ncGroup)
                .action_detail_handle("ActionDetailBO:"+site+","+"NCGROUP-UPDATE"+","+userId+":"+"com.rits.ncgroupservice.service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(userId)
                .txnId("NCGROUP-UPDATE"+String.valueOf(LocalDateTime.now())+userId)
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("NC_GROUP")
                .build();
    }

    @Override
    public List<NcCodeDPMOCategory> getAllNcCode(String site, String ncGroup) throws Exception {
        NcGroupRequest request = NcGroupRequest.builder().site(site).build();
        List<NcCodeDPMOCategory> ncCodeResponse = webClientBuilder.build()
                .post()
                .uri(ncCodeUrl)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<NcCodeDPMOCategory>>() {
                })
                .block();
        if (ncCodeResponse == null || ncCodeResponse.isEmpty()) {
            throw new NcGroupException(2000);
        }
        if (ncGroup != null && !ncGroup.isEmpty()) {
            NcGroup existingNcGroup = ncGroupRepository.findByActiveAndSiteAndNcGroup(1, site, ncGroup);
            if (existingNcGroup == null) {
                throw new NcGroupException(3400, ncGroup);
            }
            List<NcCodeDPMOCategory> existingNcCodeList = existingNcGroup.getNcCodeDPMOCategoryList();
            if (existingNcCodeList != null && !existingNcCodeList.isEmpty()) {
                ncCodeResponse.removeIf(ncCode -> existingNcCodeList.stream().anyMatch(existingNcCode -> existingNcCode.getNcCode().equals(ncCode.getNcCode())));
                return ncCodeResponse;
            }
        }
        return ncCodeResponse;
    }

    @Override
    public Boolean assignNcCode(String site, String ncGroup, List<NcCodeDPMOCategory> ncCodeList) throws Exception {
        NcGroup existingNcGroup = ncGroupRepository.findByActiveAndSiteAndNcGroup(1, site, ncGroup);
        if (existingNcGroup == null) {
            throw new NcGroupException(3400, ncGroup);
        }
        List<NcCodeDPMOCategory> existingNcCodeList = existingNcGroup.getNcCodeDPMOCategoryList();
        if (existingNcCodeList == null) {
            existingNcCodeList = new ArrayList<>();
        }
        if (existingNcCodeList.isEmpty()) {
            existingNcCodeList.addAll(ncCodeList);
        } else {
            for (NcCodeDPMOCategory ncCode : ncCodeList) {
                boolean alreadyExists = existingNcCodeList.stream().anyMatch(addNcCode -> Objects.equals(addNcCode.getNcCode(), ncCode.getNcCode()));
                if (!alreadyExists) {
                    existingNcCodeList.add(ncCode);
                }
            }
        }
        existingNcGroup.setNcCodeDPMOCategoryList(existingNcCodeList);
        existingNcGroup.setModifiedDateTime(LocalDateTime.now());
        ncGroupRepository.save(existingNcGroup);
        return true;
    }

    @Override
    public Boolean removeNcCode(String site, String ncGroup, List<NcCodeDPMOCategory> ncCodeList) throws Exception {
        NcGroup existingNcGroup = ncGroupRepository.findByActiveAndSiteAndNcGroup(1, site, ncGroup);
        if (existingNcGroup == null) {
            throw new NcGroupException(3400, ncGroup);
        }
        List<NcCodeDPMOCategory> existingNcCodeList = existingNcGroup.getNcCodeDPMOCategoryList();
        if (existingNcCodeList == null || existingNcCodeList.isEmpty()) {
            return true;
        }
        for (NcCodeDPMOCategory ncCode : ncCodeList) {
            existingNcCodeList.removeIf(existingNcCode -> existingNcCode.getNcCode().equals(ncCode.getNcCode()));
        }
        existingNcGroup.setNcCodeDPMOCategoryList(existingNcCodeList);
        existingNcGroup.setModifiedDateTime(LocalDateTime.now());
        ncGroupRepository.save(existingNcGroup);
        return true;
    }


    @Override
    public List<Operation> getAllOperation(String site, String ncGroup) throws Exception {
        NcGroupRequest request = NcGroupRequest.builder().site(site).build();
        OperationResponseList operationResponseList = webClientBuilder.build()
                .post()
                .uri(operationUrl)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OperationResponseList.class)
                .block();
        if (operationResponseList==null||operationResponseList.getOperationList()==null) {
            throw new NcGroupException(2000);
        }
        List<Operation> operationResponse=operationResponseList.getOperationList();
        if (ncGroup != null && !ncGroup.isEmpty()) {
            NcGroup existingNcGroup = ncGroupRepository.findByActiveAndSiteAndNcGroup(1, site, ncGroup);
            if (existingNcGroup == null) {
                throw new NcGroupException(3400, ncGroup);
            }
            List<Operation> existingOperationList = existingNcGroup.getOperationList();
            if (existingOperationList != null && !existingOperationList.isEmpty()) {
                operationResponse.removeIf(operation -> existingOperationList.stream().anyMatch(existingOperation -> existingOperation.getOperation().equals(operation.getOperation())));
                return operationResponse;
            }
        }
        return operationResponse;
    }

    @Override
    public List<Operation> assignOperation(String site, String ncGroup, List<Operation> operationList) throws Exception {
        NcGroup existingNcGroup = ncGroupRepository.findByActiveAndSiteAndNcGroup(1, site, ncGroup);
        if (existingNcGroup == null) {
            throw new NcGroupException(3400, ncGroup);
        }
        List<Operation> existingOperationList = existingNcGroup.getOperationList();
        if (existingOperationList == null) {
            existingOperationList = new ArrayList<>();
        }
        if (existingOperationList.isEmpty()) {
            existingOperationList.addAll(operationList);
        } else {
            for (Operation operation : operationList) {
                boolean alreadyExists = existingOperationList.stream().anyMatch(addOperation -> Objects.equals(addOperation.getOperation(), operation.getOperation()));
                if (!alreadyExists) {
                    existingOperationList.add(operation);
                }
            }
        }
        existingNcGroup.setOperationList(existingOperationList);
        existingNcGroup.setModifiedDateTime(LocalDateTime.now());
        ncGroupRepository.save(existingNcGroup);
        return existingOperationList;
    }

    @Override
    public List<Operation> removeOperation(String site, String ncGroup, List<Operation> operationList) throws Exception {
        NcGroup existingNcGroup = ncGroupRepository.findByActiveAndSiteAndNcGroup(1, site, ncGroup);
        if (existingNcGroup == null) {
            throw new NcGroupException(3400, ncGroup);
        }
        List<Operation> existingOperationList = existingNcGroup.getOperationList();
        if (existingOperationList == null || existingOperationList.isEmpty()) {
            return existingOperationList;
        }
        for (Operation operation : operationList) {
            existingOperationList.removeIf(existingOperation -> existingOperation.getOperation().equals(operation.getOperation()));
        }
        existingNcGroup.setOperationList(existingOperationList);
        existingNcGroup.setModifiedDateTime(LocalDateTime.now());
        ncGroupRepository.save(existingNcGroup);
        return existingOperationList;
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
            throw new NcGroupException(800);
        }
        return extensionResponse;
    }

    @Override
    public List<NcGroupResponse> getAvailableNcGroup(String site) throws Exception {
        return ncGroupRepository.findByActiveAndSite(1, site);
    }
    @Override
    public List<NcGroupResponse> getNcGroupByOperation(String site,String operation) throws Exception {
        return ncGroupRepository.findByActiveAndSiteAndOperationList_Operation(1, site,operation);
    }
}
