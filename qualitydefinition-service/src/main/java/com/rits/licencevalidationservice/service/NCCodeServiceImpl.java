package com.rits.licencevalidationservice.service;

import com.rits.buyoffservice.dto.AuditLogRequest;
import com.rits.licencevalidationservice.dto.*;
import com.rits.licencevalidationservice.exception.NCCodeException;
import com.rits.licencevalidationservice.model.*;
import com.rits.licencevalidationservice.repository.NCCodeRepository;
import com.rits.ncgroupservice.model.NcCodeDPMOCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class NCCodeServiceImpl implements NCCodeService{
    private final NCCodeRepository ncCodeRepository;
    private final WebClient.Builder webClientBuilder;
    @Value("${routing-service.url}/retrieveBySite")
    private String routingServiceUrl;

    @Value("${ncgroup-service.url}/retrieveBySite")
    private String ncgroupServiceUrl;

    @Value("${ncgroup-service.url}/addNcCode")
    private String assignNcCode;

    @Value("${ncgroup-service.url}/removeNcCode")
    private String removeNcCode;
//    @Value("${ncgroup-service.url}/removeNcCode")
//    private String removeNcCode;

    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;
    @Value("${auditlog-service.url}/producer")
    private String auditlogUrl;


    @Override
    public MessageModel createNCCode(NCCodeRequest ncCodeRequest) throws Exception{
        long recordPresent = ncCodeRepository.countByNcCodeAndSiteAndActive(ncCodeRequest.getNcCode(), ncCodeRequest.getSite(), 1);
        if (recordPresent > 0) {
            throw new NCCodeException(5000, ncCodeRequest.getNcCode());
        } else {
            if(ncCodeRequest.getDescription()==null || ncCodeRequest.getDescription().isEmpty()){
                ncCodeRequest.setDescription(ncCodeRequest.getNcCode());
            }

            if(ncCodeRequest.getNcGroupsList() != null && !ncCodeRequest.getNcGroupsList().isEmpty())
            {
                List<NcCodeDPMOCategory> ncGroupAddList = new ArrayList<>();
                NcCodeDPMOCategory ncCodeDPMOAddReq = new NcCodeDPMOCategory(ncCodeRequest.getNcCode());
                ncGroupAddList.add(ncCodeDPMOAddReq);
                for(NCGroups ncGroups : ncCodeRequest.getNcGroupsList())
                {

                    NcGroupRequests addncGroupRequest =   NcGroupRequests.builder()
                            .site(ncCodeRequest.getSite())
                            .ncGroup(ncGroups.getNcGroup())
                            .ncCodeDPMOCategoryList(ncGroupAddList)
                            .build();

                    Boolean ncGroupAdd = webClientBuilder.build()
                            .post()
                            .uri(assignNcCode)
                            .bodyValue(addncGroupRequest)
                            .retrieve()
                            .bodyToMono(Boolean.class)
                            .block();

                    // com.rits.userservice.model.User addUserResponse = userServiceImpl.addUserGroup(addUserRequest, userGroupList);

                    if (ncGroupAdd == null || !ncGroupAdd) {
                        throw new NCCodeException(5001, ncCodeRequest.getNcCode());
                    }
                }
            }
            NCCode ncCode = NCCode.builder()
                    .site(ncCodeRequest.getSite())
                    .handle("NCCodeBO:"+ncCodeRequest.getSite()+","+ncCodeRequest.getNcCode())
                    .ncCode(ncCodeRequest.getNcCode())
                    .description(ncCodeRequest.getDescription())
                    .status(ncCodeRequest.getStatus())
                    .assignNCtoComponent(ncCodeRequest.getAssignNCtoComponent())
                    .ncCategory(ncCodeRequest.getNcCategory())
                    .dpmoCategory(ncCodeRequest.getDpmoCategory())
                    .ncDatatype(ncCodeRequest.getNcDatatype())
                    .collectRequiredNCDataonNC(ncCodeRequest.getCollectRequiredNCDataonNC())
                    .messageType(ncCodeRequest.getMessageType())
                    .ncPriority(ncCodeRequest.getNcPriority())
                    .maximumNCLimit(ncCodeRequest.getMaximumNCLimit())
                    .ncSeverity(ncCodeRequest.getNcSeverity())
                    .secondaryCodeSpecialInstruction(ncCodeRequest.getSecondaryCodeSpecialInstruction())
                    .canBePrimaryCode(ncCodeRequest.isCanBePrimaryCode())
                    .closureRequired(ncCodeRequest.isClosureRequired())
                    .autoCloseIncident(ncCodeRequest.isAutoCloseIncident())
                    .autoClosePrimaryNC(ncCodeRequest.isAutoClosePrimaryNC())
                    .secondaryRequiredForClosure(ncCodeRequest.isSecondaryRequiredForClosure())
                    .erpQNCode(ncCodeRequest.isErpQNCode())
                    .erpCode(ncCodeRequest.getErpCode())
                    .erpCodeGroup(ncCodeRequest.getErpCodeGroup())
                    .erpCatalog(ncCodeRequest.getErpCatalog())
                    .oeeQualityKPIRelevant(ncCodeRequest.isOeeQualityKPIRelevant())
                    .dispositionRoutingsList(ncCodeRequest.getDispositionRoutingsList())
                    .operationGroupsList(ncCodeRequest.getOperationGroupsList())
                    .ncGroupsList(ncCodeRequest.getNcGroupsList())
                    .secondariesGroupsList(ncCodeRequest.getSecondariesGroupsList())
                    .activityHookList(ncCodeRequest.getActivityHookList())
                    .customDataList(ncCodeRequest.getCustomDataList())
                    .active(1)
                    .modifiedDateTime(LocalDateTime.now())
                    .createdDateTime(LocalDateTime.now())
                    .build();
            AuditLogRequest activityLog = AuditLogRequest.builder()
                    .site(ncCodeRequest.getSite())
                    .change_stamp("Create")
                    .action_code("NCCODE-CREATE")
                    .action_detail("NcCode Created "+ncCodeRequest.getNcCode())
                    .action_detail_handle("ActionDetailBO:"+ncCodeRequest.getSite()+","+"NCCODE-CREATE"+","+ncCodeRequest.getUserId()+":"+"com.rits.nccodeservice.service")
                    .date_time(String.valueOf(LocalDateTime.now()))
                    .txnId("NCCODE-CREATE"+String.valueOf(LocalDateTime.now())+ncCodeRequest.getUserId())
                    .created_date_time(String.valueOf(LocalDateTime.now()))
                    .category("NC_CODE")
                    .build();

            webClientBuilder.build()
                    .post()
                    .uri(auditlogUrl)
                    .bodyValue(activityLog)
                    .retrieve()
                    .bodyToMono(AuditLogRequest.class)
                    .block();

            return MessageModel.builder().message_details(new MessageDetails("Created SuccessFull","S")).response(ncCodeRepository.save(ncCode)).build();
        }
    }

    @Override
    public MessageModel updateNCCode(NCCodeRequest ncCodeRequest) throws Exception {
        NCCode ncCode = ncCodeRepository.findByNcCodeAndActiveAndSite(ncCodeRequest.getNcCode(), 1, ncCodeRequest.getSite());
        if(ncCodeRequest.getDescription()==null || ncCodeRequest.getDescription().isEmpty()){
            ncCodeRequest.setDescription(ncCodeRequest.getNcCode());
        }
        List<NCGroups> updatedNcGroupList = ncCodeRequest.getNcGroupsList();
        List<NCGroups> existingNcGroupList = ncCode.getNcGroupsList();

        if (existingNcGroupList != null && !existingNcGroupList.isEmpty()) {

            List<String> NcGroupToAdd = new ArrayList<>();
            List<String> NcGroupToRemove = new ArrayList();

            // Compare the updated user list with the existing list and perform necessary operations
            for (NCGroups updatedNcGroup : updatedNcGroupList) {
                boolean alreadyExists = existingNcGroupList.stream().anyMatch(user -> user.getNcGroup().equals(updatedNcGroup.getNcGroup()));
                if (!alreadyExists) {
                    NcGroupToAdd.add(updatedNcGroup.getNcGroup());
                }
            }

            for (NCGroups existNcGroup : existingNcGroupList) {
                boolean isRemoved = updatedNcGroupList.stream().noneMatch(user -> user.getNcGroup().equals(existNcGroup.getNcGroup()));
                if (isRemoved) {
                    NcGroupToRemove.add(existNcGroup.getNcGroup());
                }
            }

            List<NcCodeDPMOCategory> ncGroupAddList = new ArrayList<>();
            NcCodeDPMOCategory ncCodeDPMOAddReq = new NcCodeDPMOCategory(ncCodeRequest.getNcCode());
            ncGroupAddList.add(ncCodeDPMOAddReq);

            if (!NcGroupToAdd.isEmpty()) {
                for (String AddncGroup : NcGroupToAdd) {
                    // Create a UserRequest for adding a user to the user group
                    NcGroupRequests addncGroupRequest =   NcGroupRequests.builder()
                            .site(ncCodeRequest.getSite())
                            .ncGroup(AddncGroup)
                            .ncCodeDPMOCategoryList(ncGroupAddList)
                            .build();

                    Boolean ncGroupAdd = webClientBuilder.build()
                            .post()
                            .uri(assignNcCode)
                            .bodyValue(addncGroupRequest)
                            .retrieve()
                            .bodyToMono(Boolean.class)
                            .block();

                    // com.rits.userservice.model.User addUserResponse = userServiceImpl.addUserGroup(addUserRequest, userGroupList);

                    if (ncGroupAdd == null || !ncGroupAdd) {
                        throw new NCCodeException(5001, ncCodeRequest.getNcCode());
                    }
                }
            }

            List<NcCodeDPMOCategory> ncGroupRemovList = new ArrayList<>();
            NcCodeDPMOCategory ncCodeDPMORemReq = new NcCodeDPMOCategory(ncCodeRequest.getNcCode());
            ncGroupRemovList.add(ncCodeDPMORemReq);

            if (!NcGroupToRemove.isEmpty()) {
                for (String RemovncGroup : NcGroupToRemove) {

                    NcGroupRequests addncGroupRequest =   NcGroupRequests.builder()
                            .site(ncCodeRequest.getSite())
                            .ncGroup(RemovncGroup)
                            .ncCodeDPMOCategoryList(ncGroupRemovList)
                            .build();

                    Boolean ncGroupRemov = webClientBuilder.build()
                            .post()
                            .uri(removeNcCode)
                            .bodyValue(addncGroupRequest)
                            .retrieve()
                            .bodyToMono(Boolean.class)
                            .block();

                    //com.rits.userservice.model.User removeUserResponse = userServiceImpl.removeUserGroup(removeUserRequest, userGroupRemovList);

                    if (ncGroupRemov == null || !ncGroupRemov) {
                        throw new NCCodeException(5001, ncCodeRequest.getNcCode());
                    }
                }
            }
        }

        else {

            List<com.rits.ncgroupservice.model.NcCodeDPMOCategory>  ncCodeList = new ArrayList<>();
            com.rits.ncgroupservice.model.NcCodeDPMOCategory NCCodeReq =  com.rits.ncgroupservice.model.NcCodeDPMOCategory.builder().ncCode(ncCodeRequest.getNcCode()).build();
            ncCodeList.add(NCCodeReq);

            if (!ncCodeRequest.getNcCode().isEmpty()) {
                for (NCGroups NcGroup : ncCodeRequest.getNcGroupsList()) {
                    List<NcCodeDPMOCategory> ncCodeDPMOCategoryLists = new ArrayList<>();
                    NcCodeDPMOCategory ncCodeDPMOAddReq = new NcCodeDPMOCategory(ncCodeRequest.getNcCode());
                    ncCodeDPMOCategoryLists.add(ncCodeDPMOAddReq);

                    NcGroupRequests addncGroupRequest =   NcGroupRequests.builder()
                            .site(ncCodeRequest.getSite())
                            .ncGroup(NcGroup.getNcGroup())
                            .ncCodeDPMOCategoryList(ncCodeDPMOCategoryLists)
                            .build();


                    Boolean ncGroupAdd = webClientBuilder.build()
                            .post()
                            .uri(assignNcCode)
                            .bodyValue(addncGroupRequest)
                            .retrieve()
                            .bodyToMono(Boolean.class)
                            .block();

                    // com.rits.userservice.model.User addUserResponse = userServiceImpl.addUserGroup(addUserRequest, userGroupList);

                    if (ncGroupAdd == null || !ncGroupAdd) {
                        throw new NCCodeException(5001, ncCodeRequest.getNcCode());
                    }
                }
            }

        }
        if (ncCode != null) {
            ncCode = NCCode.builder()
                    .id(ncCode.getId())
                    .site(ncCode.getSite())
                    .handle("NCCodeBO:" + ncCode.getSite() + "," + ncCode.getNcCode())
                    .ncCode(ncCode.getNcCode())
                    .description(ncCodeRequest.getDescription())
                    .status(ncCodeRequest.getStatus())
                    .assignNCtoComponent(ncCodeRequest.getAssignNCtoComponent())
                    .ncCategory(ncCodeRequest.getNcCategory())
                    .dpmoCategory(ncCodeRequest.getDpmoCategory())
                    .ncDatatype(ncCodeRequest.getNcDatatype())
                    .collectRequiredNCDataonNC(ncCodeRequest.getCollectRequiredNCDataonNC())
                    .messageType(ncCodeRequest.getMessageType())
                    .ncPriority(ncCodeRequest.getNcPriority())
                    .maximumNCLimit(ncCodeRequest.getMaximumNCLimit())
                    .ncSeverity(ncCodeRequest.getNcSeverity())
                    .secondaryCodeSpecialInstruction(ncCodeRequest.getSecondaryCodeSpecialInstruction())
                    .canBePrimaryCode(ncCodeRequest.isCanBePrimaryCode())
                    .closureRequired(ncCodeRequest.isClosureRequired())
                    .autoCloseIncident(ncCodeRequest.isAutoCloseIncident())
                    .autoClosePrimaryNC(ncCodeRequest.isAutoClosePrimaryNC())
                    .secondaryRequiredForClosure(ncCodeRequest.isSecondaryRequiredForClosure())
                    .erpQNCode(ncCodeRequest.isErpQNCode())
                    .erpCode(ncCodeRequest.getErpCode())
                    .erpCodeGroup(ncCodeRequest.getErpCodeGroup())
                    .erpCatalog(ncCodeRequest.getErpCatalog())
                    .oeeQualityKPIRelevant(ncCodeRequest.isOeeQualityKPIRelevant())
                    .dispositionRoutingsList(ncCodeRequest.getDispositionRoutingsList())
                    .operationGroupsList(ncCodeRequest.getOperationGroupsList())
                    .ncGroupsList(ncCodeRequest.getNcGroupsList())
                    .secondariesGroupsList(ncCodeRequest.getSecondariesGroupsList())
                    .activityHookList(ncCodeRequest.getActivityHookList())
                    .customDataList(ncCodeRequest.getCustomDataList())
                    .active(ncCode.getActive())
                    .modifiedDateTime(LocalDateTime.now())
                    .createdDateTime(ncCode.getCreatedDateTime())
                    .build();
            AuditLogRequest activityLog = AuditLogRequest.builder()
                    .site(ncCodeRequest.getSite())
                    .change_stamp("Update")
                    .action_code("NCCODE-UPDATE")
                    .action_detail("NcCode Updated "+ncCodeRequest.getNcCode())
                    .action_detail_handle("ActionDetailBO:"+ncCodeRequest.getSite()+","+"NCCODE-UPDATE"+","+ncCodeRequest.getUserId()+":"+"com.rits.nccodeservice.service")
                    .date_time(String.valueOf(LocalDateTime.now()))
                    .txnId("NCCODE-UPDATE"+String.valueOf(LocalDateTime.now())+ncCodeRequest.getUserId())
                    .created_date_time(String.valueOf(LocalDateTime.now()))
                    .category("NC_CODE")
                    .build();

            webClientBuilder.build()
                    .post()
                    .uri(auditlogUrl)
                    .bodyValue(activityLog)
                    .retrieve()
                    .bodyToMono(AuditLogRequest.class)
                    .block();

            return MessageModel.builder().message_details(new MessageDetails("Updated SuccessFull","S")).response(ncCodeRepository.save(ncCode)).build();
        }
        throw new NCCodeException(5001, ncCodeRequest.getNcCode());
    }

    @Override
    public NCCodeResponseList getNCCodeListByCreationDate(NCCodeRequest ncCodeRequest) throws Exception {
        List<NCCodeResponse> ncCodeResponseList = ncCodeRepository.findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(1, ncCodeRequest.getSite());
            return NCCodeResponseList.builder().ncCodeList(ncCodeResponseList).build();

    }

    @Override
    public NCCodeResponseList getNCCodeList(NCCodeRequest ncCodeRequest) throws Exception {
        if (ncCodeRequest.getNcCode() == null || ncCodeRequest.getNcCode().isEmpty()) {
            return  getNCCodeListByCreationDate(ncCodeRequest);
        } else {
            List<NCCodeResponse> ncCodeResponseList = ncCodeRepository.findByNcCodeContainingIgnoreCaseAndSiteAndActive(ncCodeRequest.getNcCode(), ncCodeRequest.getSite(), 1);
            if (ncCodeResponseList != null && !ncCodeResponseList.isEmpty()) {
                return NCCodeResponseList.builder().ncCodeList(ncCodeResponseList).build();
            } else {
                throw new NCCodeException(5001, ncCodeRequest.getNcCode());
            }
        }
    }

    @Override
    public NCCode retrieveNCCode(NCCodeRequest ncCodeRequest) throws Exception {
        NCCode ncCode = ncCodeRepository.findByNcCodeAndActiveAndSite(ncCodeRequest.getNcCode(),  1,ncCodeRequest.getSite());
        if (ncCode != null ) {
            return ncCode;
        } else {
            throw new NCCodeException(5001, ncCodeRequest.getNcCode());
        }
    }
    @Override
    public MessageModel deleteNCCode(NCCodeRequest ncCodeRequest) throws Exception {
        if (ncCodeRepository.existsByNcCodeAndSiteAndActive(ncCodeRequest.getNcCode(), ncCodeRequest.getSite(), 1)) {
            NCCode existingNCCode = ncCodeRepository.findByNcCodeAndActive(ncCodeRequest.getNcCode(),1);
            if(existingNCCode != null && existingNCCode.getNcGroupsList() != null && !existingNCCode.getNcGroupsList().isEmpty())
            {
                List<NcCodeDPMOCategory> ncGroupRemovList = new ArrayList<>();
                NcCodeDPMOCategory ncCodeDPMORemReq = new NcCodeDPMOCategory(ncCodeRequest.getNcCode());
                ncGroupRemovList.add(ncCodeDPMORemReq);
                for(NCGroups ncGroup : existingNCCode.getNcGroupsList())
                {
                    NcGroupRequests addncGroupRequest =   NcGroupRequests.builder()
                            .site(ncCodeRequest.getSite())
                            .ncGroup(ncGroup.getNcGroup())
                            .ncCodeDPMOCategoryList(ncGroupRemovList)
                            .build();

                    Boolean ncGroupRemove = webClientBuilder.build()
                            .post()
                            .uri(removeNcCode)
                            .bodyValue(addncGroupRequest)
                            .retrieve()
                            .bodyToMono(Boolean.class)
                            .block();
                }
            }
            existingNCCode.setActive(0);
            existingNCCode.setModifiedDateTime(LocalDateTime.now());
            ncCodeRepository.save(existingNCCode);
            AuditLogRequest activityLog = AuditLogRequest.builder()
                    .site(ncCodeRequest.getSite())
                    .change_stamp("Delete")
                    .action_code("NCCODE-DELETE")
                    .action_detail("NcCode Deleted "+ncCodeRequest.getNcCode())
                    .action_detail_handle("ActionDetailBO:"+ncCodeRequest.getSite()+","+"NCCODE-DELETE"+","+ncCodeRequest.getUserId()+":"+"com.rits.nccodeservice.service")
                    .date_time(String.valueOf(LocalDateTime.now()))
                    .txnId("NCCODE-DELETE"+String.valueOf(LocalDateTime.now())+ncCodeRequest.getUserId())
                    .created_date_time(String.valueOf(LocalDateTime.now()))
                    .category("NC_CODE")
                    .build();

            webClientBuilder.build()
                    .post()
                    .uri(auditlogUrl)
                    .bodyValue(activityLog)
                    .retrieve()
                    .bodyToMono(AuditLogRequest.class)
                    .block();
            return MessageModel.builder().message_details(new MessageDetails(ncCodeRequest.getNcCode()+" Deleted SuccessFull","S")).build();
        } else {
            throw new NCCodeException(5001, ncCodeRequest.getNcCode());
        }
    }

    @Override
    public Boolean isNCCodeExist(NCCodeRequest ncCodeRequest) throws Exception {
        return ncCodeRepository.existsByNcCodeAndSiteAndActive(ncCodeRequest.getNcCode(),ncCodeRequest.getSite() ,1);

    }

    @Override
    public List<NCCodeResponse> retrieveAllBySite(NCCodeRequest ncCodeRequest){
        List<NCCodeResponse> ncCodeResponseList=ncCodeRepository.findBySiteAndActive(ncCodeRequest.getSite(),1);
        return ncCodeResponseList;

    }

    @Override
    public NCCode associateRoutingToDispositionRoutingList(DispositionRoutingListRequest dispositionRoutingListRequest) throws Exception{
        NCCode ncCode = ncCodeRepository.findByNcCodeAndActiveAndSite(dispositionRoutingListRequest.getNcCode(),  1,dispositionRoutingListRequest.getSite());
        if (ncCode == null) {
            throw new NCCodeException(5001, dispositionRoutingListRequest.getNcCode());
        }
        List<DispositionRoutings> dispositionRoutingsList = ncCode.getDispositionRoutingsList();
        for (String routing : dispositionRoutingListRequest.getRoutingBO()) {
            boolean alreadyExists = dispositionRoutingsList.stream().anyMatch(member -> member.getRoutingBO().equals(routing));
            if (!alreadyExists) {
                DispositionRoutings newMember  = DispositionRoutings.builder().routingBO(routing).build();
                dispositionRoutingsList.add(newMember);
            }
        }
        ncCode.setDispositionRoutingsList(dispositionRoutingsList);
        ncCode.setModifiedDateTime(LocalDateTime.now());
        return ncCodeRepository.save(ncCode);
    }


    @Override
    public NCCode removeRoutingFromDispositionRoutingList(DispositionRoutingListRequest dispositionRoutingListRequest) throws Exception{
        NCCode ncCode = ncCodeRepository.findByNcCodeAndActiveAndSite(dispositionRoutingListRequest.getNcCode(),  1,dispositionRoutingListRequest.getSite());
        if (ncCode != null) {
            for (String routing : dispositionRoutingListRequest.getRoutingBO()) {
                if (ncCode.getDispositionRoutingsList().removeIf(dispositionRoutings -> dispositionRoutings.getRoutingBO().equals(routing))) {
                    ncCode.setModifiedDateTime(LocalDateTime.now());
                }
            }
        } else {
            throw new NCCodeException(5001, dispositionRoutingListRequest.getNcCode());
        }
        return ncCodeRepository.save(ncCode);
    }
    @Override
    public AvailableRoutingList getAvailableRoutings(NCCodeRequest ncCodeRequest) throws Exception{
        if( ncCodeRequest.getNcCode()!=null && !ncCodeRequest.getSite().isEmpty() ) {
            List<NCCode> ncCodes = ncCodeRepository.findBySiteAndNcCodeAndActive(ncCodeRequest.getSite(), ncCodeRequest.getNcCode(), 1);
            if (ncCodes != null && !ncCodes.isEmpty()) {
                List<String> routings = new ArrayList<>();
                for (NCCode ncCode : ncCodes) {
                    List<DispositionRoutings> dispositionRoutings = ncCode.getDispositionRoutingsList();
                    if (dispositionRoutings != null && !dispositionRoutings.isEmpty()) {
                        routings.addAll(dispositionRoutings.stream()
                                .map(DispositionRoutings::getRoutingBO)
                                .collect(Collectors.toList()));
                    }
                }

                SiteRequest request= new SiteRequest();
                request.setSite(ncCodeRequest.getSite());
                List<DispositionRoutingsResponse> availableRoutings = webClientBuilder
                        .build()
                        .post()
                        .uri(routingServiceUrl)
                        .body(BodyInserters.fromValue(request))
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<List<DispositionRoutingsResponse>>() {
                        })
                        .block();

                if (availableRoutings != null) {
                    availableRoutings.removeIf(dispositionRoutingsResponse -> routings.contains(dispositionRoutingsResponse.getRouting()));
                }
                return AvailableRoutingList.builder().availableRoutingList(availableRoutings).build();
            }
            throw new NCCodeException(5001, ncCodeRequest.getNcCode());
        }
        SiteRequest request= new SiteRequest();
        request.setSite(ncCodeRequest.getSite());

        List<DispositionRoutingsResponse> availableRoutings = webClientBuilder
                .build()
                .post()
                .uri(routingServiceUrl)
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<DispositionRoutingsResponse>>() {
                })
                .block();
        return AvailableRoutingList.builder().availableRoutingList(availableRoutings).build();
    }
    @Override
    public NCCode associateSecondariesToSecondariesGroupsList(SecondariesGroupsListRequest secondariesGroupsListRequest) throws Exception {
        NCCode ncCode = ncCodeRepository.findByNcCodeAndActiveAndSite(secondariesGroupsListRequest.getNcCode(), 1, secondariesGroupsListRequest.getSite());
        if (ncCode == null) {
            throw new NCCodeException(5001, secondariesGroupsListRequest.getNcCode());
        }
        List<SecondariesGroups> secondariesGroupsList = ncCode.getSecondariesGroupsList();
        for (String secondaries : secondariesGroupsListRequest.getSecondaries()) {
            boolean alreadyExists = secondariesGroupsList.stream().anyMatch(member -> member.getSecondaries().equals(secondaries));
            if (!alreadyExists) {
                SecondariesGroups newMember = SecondariesGroups.builder().secondaries(secondaries).build();
                secondariesGroupsList.add(newMember);
            }
        }
        ncCode.setSecondariesGroupsList(secondariesGroupsList);
        ncCode.setModifiedDateTime(LocalDateTime.now());
        return ncCodeRepository.save(ncCode);
    }

    @Override
    public NCCode removeSecondariesFromSecondariesGroupsList(SecondariesGroupsListRequest secondariesGroupsListRequest) throws Exception{
        NCCode ncCode = ncCodeRepository.findByNcCodeAndActiveAndSite(secondariesGroupsListRequest.getNcCode(),  1,secondariesGroupsListRequest.getSite());
        if (ncCode != null) {
            for (String secondaries : secondariesGroupsListRequest.getSecondaries()) {
                if (ncCode.getSecondariesGroupsList().removeIf(secondariesGroupsList -> secondariesGroupsList.getSecondaries().equals(secondaries))) {
                    ncCode.setModifiedDateTime(LocalDateTime.now());
                }
            }
        } else {
            throw new NCCodeException(5001, secondariesGroupsListRequest.getNcCode());
        }
        return ncCodeRepository.save(ncCode);
    }
    @Override
    public AvailableSecondariesGroupsList getAvailableSecondaries(NCCodeRequest ncCodeRequest) throws Exception {
        List<NCCode> retrieveAllNcCode = ncCodeRepository.findByActiveAndSite(1,ncCodeRequest.getSite());
        List<SecondariesGroupsResponse> availableSecondaries = new ArrayList<>();
        for(NCCode ncs : retrieveAllNcCode) {
                availableSecondaries.add(SecondariesGroupsResponse.builder().secondaries(ncs.getNcCode()).build());
        }
        if (ncCodeRequest.getNcCode() != null && !ncCodeRequest.getSite().isEmpty()) {
            List<NCCode> ncCodes = ncCodeRepository.findBySiteAndNcCodeAndActive(ncCodeRequest.getSite(), ncCodeRequest.getNcCode(), 1);
            if (ncCodes != null && !ncCodes.isEmpty()) {
                List<String> secondaries = new ArrayList<>();
                for (NCCode ncCode : ncCodes) {
                    List<SecondariesGroups> secondariesGroupsList = ncCode.getSecondariesGroupsList();
                    if (secondariesGroupsList != null && !secondariesGroupsList.isEmpty()) {
                        secondaries.addAll(secondariesGroupsList.stream()
                                .map(SecondariesGroups::getSecondaries)
                                .collect(Collectors.toList()));
                    }
                }
                if (availableSecondaries != null) {
                            availableSecondaries.removeIf(secondariesGroupsResponse -> secondaries.contains(secondariesGroupsResponse.getSecondaries()));
                        }
                return AvailableSecondariesGroupsList.builder().availableSecondariesList(availableSecondaries).build();
            }
            throw new NCCodeException(5001, ncCodeRequest.getNcCode());
        }

        return AvailableSecondariesGroupsList.builder().availableSecondariesList(availableSecondaries).build();
    }

    @Override
    public Boolean associateNCGroupsToNCGroupsList(NCGroupsListRequest ncGroupsListRequest) throws Exception{
        NCCode ncCode = ncCodeRepository.findByNcCodeAndActiveAndSite(ncGroupsListRequest.getNcCode(),  1,ncGroupsListRequest.getSite());
        if (ncCode == null) {
            throw new NCCodeException(5001, ncGroupsListRequest.getNcCode());
        }
        List<NCGroups> ncGroupsList = ncCode.getNcGroupsList();
        for (String ncGroups : ncGroupsListRequest.getNcGroup()) {
            boolean alreadyExists = ncGroupsList.stream().anyMatch(member -> member.getNcGroup().equals(ncGroups));
            if (!alreadyExists) {
                NCGroups newMember = NCGroups.builder().ncGroup(ncGroups).build();
                ncGroupsList.add(newMember);
            }
        }
        ncCode.setNcGroupsList(ncGroupsList);
        ncCode.setModifiedDateTime(LocalDateTime.now());
       ncCodeRepository.save(ncCode);
       return true;
    }

    @Override
    public Boolean removeNCGroupsToNCGroupsList(NCGroupsListRequest ncGroupsListRequest) throws Exception{
        NCCode ncCode = ncCodeRepository.findByNcCodeAndActiveAndSite(ncGroupsListRequest.getNcCode(),  1,ncGroupsListRequest.getSite());
        if (ncCode != null) {
            for (String ncGroups : ncGroupsListRequest.getNcGroup()) {
                if (ncCode.getNcGroupsList().removeIf(ncGroupsList -> ncGroupsList.getNcGroup().equals(ncGroups))) {
                    ncCode.setModifiedDateTime(LocalDateTime.now());
                }
            }
        } else {
            throw new NCCodeException(5001, ncGroupsListRequest.getNcCode());
        }
       ncCodeRepository.save(ncCode);
        return true;
    }

    @Override
    public AvailableNCGroupsList getAvailableNCGroups(NCCodeRequest ncCodeRequest) throws Exception{
        if( ncCodeRequest.getNcCode()!=null && !ncCodeRequest.getSite().isEmpty() ) {
            List<NCCode> ncCodes = ncCodeRepository.findBySiteAndNcCodeAndActive(ncCodeRequest.getSite(), ncCodeRequest.getNcCode(), 1);
            if (ncCodes != null && !ncCodes.isEmpty()) {
                List<String> ncGroups = new ArrayList<>();
                for (NCCode ncCode : ncCodes) {
                    List<NCGroups> ncGroupsList = ncCode.getNcGroupsList();
                    if (ncGroupsList != null && !ncGroupsList.isEmpty()) {
                        ncGroups.addAll(ncGroupsList.stream()
                                .map(NCGroups::getNcGroup)
                                .collect(Collectors.toList()));
                    }
                }
                SiteRequest request= new SiteRequest();
                request.setSite(ncCodeRequest.getSite());
                List<NCGroupsResponse> availableNCGroups = webClientBuilder
                        .build()
                        .post()
                        .uri(ncgroupServiceUrl)
                        .body(BodyInserters.fromValue(request))
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<List<NCGroupsResponse>>() {
                        })
                        .block();

                if (availableNCGroups != null) {
                    availableNCGroups.removeIf(ncGroupsResponse -> ncGroups.contains(ncGroupsResponse.getNcGroup()));
                }
                return AvailableNCGroupsList.builder().availableNCGroupsList(availableNCGroups).build();
            }
            throw new NCCodeException(5001, ncCodeRequest.getNcCode());
            }
            SiteRequest request= new SiteRequest();
                        request.setSite(ncCodeRequest.getSite());
            List<NCGroupsResponse> availableNCGroups = webClientBuilder
                    .build()
                    .post()
                    .uri(ncgroupServiceUrl)
                    .body(BodyInserters.fromValue(request))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<NCGroupsResponse>>() {
                    })
                    .block();

                 return AvailableNCGroupsList.builder().availableNCGroupsList(availableNCGroups).build();
        }

    @Override
    public String callExtension(Extension extension) {
        String extensionResponse = webClientBuilder.build()
                .post()
                .uri(extensionUrl)
                .bodyValue(extension)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        if (extensionResponse==null) {
            throw new NCCodeException(800);
        }
        return extensionResponse;
    }

    @Override
    public List<DispositionRoutings> getAllDispositionRouting(NCCodeRequest ncCodeRequest){
        List<DispositionRoutings> dispositionRoutings=new ArrayList<DispositionRoutings>();
        List<DispositionRoutings> tempList=new ArrayList<DispositionRoutings>();
        Set<String> uniqueRoutingBOs = new HashSet<>();
        List<String> ncodelst=ncCodeRequest.getNcCodeList();
        for(String obj:ncodelst){
            tempList=new ArrayList<>();
            NCCode ncdata= ncCodeRepository.findByNcCodeAndSiteAndActive(obj , ncCodeRequest.getSite(),1);
            tempList=ncdata.getDispositionRoutingsList();
           tempList.stream()
                    .map(DispositionRoutings::getRoutingBO)
                    .forEach(uniqueRoutingBOs::add);

            dispositionRoutings.addAll(tempList);
        }
        uniqueRoutingBOs.forEach(routingBO -> {
            DispositionRoutings dispositionRouting = new DispositionRoutings();
            dispositionRouting.setRoutingBO(routingBO);
            dispositionRoutings.add(dispositionRouting);
        });

        return dispositionRoutings;
    }
}
