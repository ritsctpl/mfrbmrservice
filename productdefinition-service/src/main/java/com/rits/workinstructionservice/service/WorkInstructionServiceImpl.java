package com.rits.workinstructionservice.service;

import com.rits.workinstructionservice.model.Attachment;
import com.rits.workinstructionservice.dto.*;
import com.rits.workinstructionservice.exception.WorkInstructionException;
import com.rits.workinstructionservice.model.MessageDetails;
import com.rits.workinstructionservice.model.WIMessageModel;
import com.rits.workinstructionservice.model.WorkInstruction;
import com.rits.workinstructionservice.repository.WorkInstructionRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class WorkInstructionServiceImpl implements WorkInstructionService{
    private final WorkInstructionRepository workInstructionRepository;
    private final MongoTemplate mongoTemplate;
    private final WebClient.Builder webClientBuilder;
    private final MessageSource localMessageSource;
    private final ApplicationEventPublisher eventPublisher;
    @Value("${listmaintenance-service.url}/retrieve")
    private String listMaintenanceRetrieveUrl;
    @Value("${pcurouterheader-service.uri}/retrieve")
    private String pcuRouterHeaderRetrieveUrl;
    @Value("${productionlog-service.url}/save")
    private String productionLogUrl;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }

    @Override
    public WIMessageModel createWorkInstruction(WorkInstructionRequest workInstructionRequest) throws Exception
    {
        if(isWorkInstructionExists(workInstructionRequest))
        {
            List<WorkInstruction> retrievedList = workInstructionRepository.findByWorkInstructionAndSiteAndActive(workInstructionRequest.getWorkInstruction(),workInstructionRequest.getSite(),1);
            for(WorkInstruction retrieved : retrievedList)
                if(retrieved.getRevision().equals(workInstructionRequest.getRevision()))
                {
                    throw new WorkInstructionException(2000,workInstructionRequest.getWorkInstruction());
                }
                else {
                    retrieved.setCurrentVersion(false);
                    workInstructionRepository.save(retrieved);
                }
        }
        if(workInstructionRequest.getDescription()==null || workInstructionRequest.getDescription().isEmpty()) {
            workInstructionRequest.setDescription(workInstructionRequest.getWorkInstruction());
        }
        List<String>  tags = new ArrayList<>();
        if(workInstructionRequest.getAttachmentList() != null && !workInstructionRequest.getAttachmentList().isEmpty())
        {
            tags = getAttachment(workInstructionRequest.getAttachmentList());
        }
        WorkInstruction newWorkInstruction = workInstructionBuilder(workInstructionRequest, tags);
        newWorkInstruction.setHandle("WorkInstructionBO:" + workInstructionRequest.getSite() + "," + workInstructionRequest.getWorkInstruction() + workInstructionRequest.getRevision());
        newWorkInstruction.setCreatedBy(workInstructionRequest.getUserId());
        newWorkInstruction.setCreatedDateTime(LocalDateTime.now());

        String createdMessage = getFormattedMessage(4, workInstructionRequest.getWorkInstruction(),workInstructionRequest.getRevision());
        MessageDetails message = MessageDetails.builder().msg_type("S").msg(createdMessage).build();
        return WIMessageModel.builder().message_details(message).response(workInstructionRepository.save(newWorkInstruction)).build();
    }

    public static Map<String, String> createFields(Attachment attachment) {
        Map<String, String> attachmentList = new LinkedHashMap<>();

        if (attachment == null) {
            return attachmentList;
        }

        if (attachment.getItem() != null) attachmentList.put("item", attachment.getItem());
        if (attachment.getItemGroup() != null) attachmentList.put("itemGroup", attachment.getItemGroup());
        if (attachment.getItemVersion() != null) attachmentList.put("itemVersion", attachment.getItemVersion());
        if (attachment.getRouting() != null) attachmentList.put("routing", attachment.getRouting());
        if (attachment.getRoutingVersion() != null) attachmentList.put("routingVersion", attachment.getRoutingVersion());
        if (attachment.getOperation() != null) attachmentList.put("operation", attachment.getOperation());
        if (attachment.getWorkCenter() != null) attachmentList.put("workCenter", attachment.getWorkCenter());
        if (attachment.getResource() != null) attachmentList.put("resource", attachment.getResource());
        if (attachment.getResourceType() != null) attachmentList.put("resourceType", attachment.getResourceType());
        if (attachment.getCustomerOrder() != null) attachmentList.put("customerOrder", attachment.getCustomerOrder());
        if (attachment.getShopOrder() != null) attachmentList.put("shopOrder", attachment.getShopOrder());
        if (attachment.getPcu() != null) attachmentList.put("pcu", attachment.getPcu());
        if (attachment.getBom() != null) attachmentList.put("bom", attachment.getBom());
        if (attachment.getBomVersion() != null) attachmentList.put("bomVersion", attachment.getBomVersion());
        if (attachment.getComponent() != null) attachmentList.put("component", attachment.getComponent());
        if (attachment.getComponentVersion() != null) attachmentList.put("componentVersion", attachment.getComponentVersion());
        if (attachment.getBatchNo() != null) attachmentList.put("batchNo", attachment.getBatchNo());
        if (attachment.getOrderNo() != null) attachmentList.put("orderNo", attachment.getOrderNo());
        if (attachment.getOperation() != null) attachmentList.put("operation", attachment.getOperation());
        if (attachment.getPhase() != null) attachmentList.put("phase", attachment.getPhase());

        return attachmentList;
    }

    @Override
    public WorkInstruction uploadFile(WorkInstructionRequest workInstructionRequest) throws Exception{
        WorkInstruction existingWorkInstruction = workInstructionRepository.findByWorkInstructionAndRevisionAndSiteAndActiveEquals(workInstructionRequest.getWorkInstruction(),workInstructionRequest.getRevision(),workInstructionRequest.getSite(),1);
           if(existingWorkInstruction!=null){
               if(existingWorkInstruction.getInstructionType().equalsIgnoreCase("file")) {
                   existingWorkInstruction.setFile(workInstructionRequest.getFile());
                   existingWorkInstruction.setFileName(workInstructionRequest.getFileName());
               }
               return workInstructionRepository.save(existingWorkInstruction);
           }
        throw new WorkInstructionException(2001,workInstructionRequest.getWorkInstruction());
    }



    @Override
    public String getFileContent(WorkInstructionRequest workInstructionRequest) throws Exception {
        WorkInstruction existingWorkInstruction = workInstructionRepository.findByWorkInstructionAndRevisionAndFileNameAndActiveAndSite(workInstructionRequest.getWorkInstruction(),workInstructionRequest.getRevision(), workInstructionRequest.getFileName(), 1, workInstructionRequest.getSite());
        if(existingWorkInstruction!=null) {
            byte[] fileContent = existingWorkInstruction.getFile().getBytes(StandardCharsets.UTF_8);

            if (fileContent != null) {
                String content = Base64.getEncoder().encodeToString(fileContent);
                return "content";
            }
        }

            throw new WorkInstructionException(2001, workInstructionRequest.getWorkInstruction());
        }


    @Override
    public  Boolean isWorkInstructionExists(WorkInstructionRequest workInstructionRequest) throws Exception
    {
        return workInstructionRepository.existsByWorkInstructionAndSiteAndActiveEquals(workInstructionRequest.getWorkInstruction(),workInstructionRequest.getSite(),1);
    }

    @Override
    public boolean workInstructionExistsByRevision(WorkInstructionRequest workInstructionRequest) throws Exception
    {
        return workInstructionRepository.existsByWorkInstructionAndRevisionAndSiteAndActiveEquals(workInstructionRequest.getWorkInstruction(),workInstructionRequest.getRevision(),workInstructionRequest.getSite(),1);
    }

    @Override
    public WIMessageModel updateWorkInstruction(WorkInstructionRequest workInstructionRequest) throws Exception
    {
        if(workInstructionExistsByRevision(workInstructionRequest))
        {
            WorkInstruction existingWorkInstruction = workInstructionRepository.findByWorkInstructionAndRevisionAndSiteAndActiveEquals(workInstructionRequest.getWorkInstruction(),workInstructionRequest.getRevision(),workInstructionRequest.getSite(),1);
            List<String>  tags = new ArrayList<>();
            if(workInstructionRequest.getAttachmentList() != null && !workInstructionRequest.getAttachmentList().isEmpty())
            {
                tags = getAttachment(workInstructionRequest.getAttachmentList());
            }
            WorkInstruction updatedWorkInstruction = workInstructionBuilder(workInstructionRequest, tags);
            updatedWorkInstruction.setSite(existingWorkInstruction.getSite());
            updatedWorkInstruction.setWorkInstruction(existingWorkInstruction.getWorkInstruction());
            updatedWorkInstruction.setHandle(existingWorkInstruction.getHandle());
            updatedWorkInstruction.setCreatedBy(existingWorkInstruction.getCreatedBy());
            updatedWorkInstruction.setCreatedDateTime(existingWorkInstruction.getCreatedDateTime());
            updatedWorkInstruction.setRevision(existingWorkInstruction.getRevision());
            updatedWorkInstruction.setModifiedDateTime(LocalDateTime.now());
            updatedWorkInstruction.setModifiedBy(workInstructionRequest.getUserId());

            String createdMessage = getFormattedMessage(5, existingWorkInstruction.getWorkInstruction() ,existingWorkInstruction.getRevision());
            MessageDetails message = MessageDetails.builder().msg_type("S").msg(createdMessage).build();
            return WIMessageModel.builder().message_details(message).response(workInstructionRepository.save(updatedWorkInstruction)).build();
        }
        throw new WorkInstructionException(2001,workInstructionRequest.getWorkInstruction());
    }

    @Override
    public WIMessageModel deleteWorkInstruction(WorkInstructionRequest workInstructionRequest) throws Exception
    {
        if(isWorkInstructionExists(workInstructionRequest))
        {
            WorkInstruction existingWorkInstruction = workInstructionRepository.findByWorkInstructionAndRevisionAndSiteAndActiveEquals(workInstructionRequest.getWorkInstruction(),workInstructionRequest.getRevision(),workInstructionRequest.getSite(),1);
            existingWorkInstruction.setActive(0);
            workInstructionRepository.save(existingWorkInstruction);
            String createdMessage = getFormattedMessage(6, existingWorkInstruction.getWorkInstruction() ,existingWorkInstruction.getRevision());
            MessageDetails message = MessageDetails.builder().msg_type("S").msg(createdMessage).build();
            return WIMessageModel.builder().message_details(message).build();
        }
        throw new WorkInstructionException(2001,workInstructionRequest.getWorkInstruction());
    }

    @Override
    public WorkInstructionListResponse retrieveTop50(WorkInstructionRequest workInstructionRequest) throws Exception
    {
        List<WorkInstructionList> workInstructionList = workInstructionRepository.findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(1,workInstructionRequest.getSite());
        if(workInstructionList.isEmpty())
        {
            throw new WorkInstructionException(2004,"");
        }
        WorkInstructionListResponse workInstructionListResponse=new WorkInstructionListResponse(workInstructionList);
        return workInstructionListResponse;
    }
    @Override
    public WorkInstructionListResponse retrieveAllByWorkInstruction(WorkInstructionRequest workInstructionRequest) throws Exception
    {
        if(workInstructionRequest.getWorkInstruction() != null && !workInstructionRequest.getWorkInstruction().isEmpty()) {
            List<WorkInstructionList> workInstructionList = workInstructionRepository.findBySiteAndWorkInstructionContainingIgnoreCaseAndActiveEquals(workInstructionRequest.getSite(),workInstructionRequest.getWorkInstruction(),1);
            if (workInstructionList.isEmpty()) {
                throw new WorkInstructionException(2004, workInstructionRequest.getWorkInstruction());
            }
            WorkInstructionListResponse workInstructionListResponse = new WorkInstructionListResponse(workInstructionList);
            return workInstructionListResponse;
        }
        return retrieveTop50(workInstructionRequest);
    }

    @Override
    public WorkInstruction retrieveByWorkInstruction(WorkInstructionRequest workInstructionRequest) throws Exception
    {
        if(isWorkInstructionExists(workInstructionRequest))
        {
            if(workInstructionRequest.getRevision()!=null&& !workInstructionRequest.getRevision().isEmpty()) {
                WorkInstruction existingWorkInstruction = workInstructionRepository.findByWorkInstructionAndRevisionAndSiteAndActiveEquals(workInstructionRequest.getWorkInstruction(), workInstructionRequest.getRevision(), workInstructionRequest.getSite(), 1);
                if(existingWorkInstruction != null && existingWorkInstruction.getWorkInstruction()!=null)
                {
                    workInstructionRequest.setInstructionType(existingWorkInstruction.getInstructionType());
                    workInstructionRequest.setDescription(existingWorkInstruction.getDescription());
                }
//                Boolean productionLogged = logProductionRecord(workInstructionRequest);
                return existingWorkInstruction;
            }else{
                WorkInstruction retrievedWorkInstruction = workInstructionRepository.findByWorkInstructionAndCurrentVersionAndSiteAndActive(workInstructionRequest.getWorkInstruction(),true,workInstructionRequest.getSite(),1);
                if(retrievedWorkInstruction != null && retrievedWorkInstruction.getWorkInstruction()!=null)
                {
                    workInstructionRequest.setInstructionType(retrievedWorkInstruction.getInstructionType());
                    workInstructionRequest.setDescription(retrievedWorkInstruction.getDescription());
                }
//                Boolean productionLogged = logProductionRecord(workInstructionRequest);
                return retrievedWorkInstruction;
            }
        }
        throw new WorkInstructionException(2001,workInstructionRequest.getWorkInstruction());
    }
    @Override
    public WorkInstructionResponseList retrieveWorkInstructionByItem(AttachmentListRequest attachmentListRequest) throws Exception
    {
        if(attachmentListRequest.getItem()!=null && !attachmentListRequest.getItem().isEmpty())
        {
            List<WorkInstructionResponse> workInstructionList = workInstructionRepository.findByActiveAndSiteAndAttachmentListItem(1,attachmentListRequest.getSite(),attachmentListRequest.getItem());
            WorkInstructionResponseList workInstructionListResponse = new WorkInstructionResponseList(workInstructionList);
            return workInstructionListResponse;
        }
        throw new WorkInstructionException(2005,attachmentListRequest.getItem());
    }
    @Override
    public WorkInstructionResponseList retrieveByItemOperatrion(AttachmentListRequest attachmentListRequest) throws Exception
    {
        if(attachmentListRequest.getItem()!=null && !attachmentListRequest.getItem().isEmpty() && attachmentListRequest.getOperation()!=null && !attachmentListRequest.getOperation().isEmpty())
        {
            List<WorkInstructionResponse> workInstructionList = workInstructionRepository.findByActiveAndSiteAndAttachmentListItemAndAttachmentListOperation(1,attachmentListRequest.getSite(),attachmentListRequest.getItem(),attachmentListRequest.getOperation());
            WorkInstructionResponseList workInstructionListResponse = new WorkInstructionResponseList(workInstructionList);
            return workInstructionListResponse;
        }
        throw new WorkInstructionException(2006," ");
    }

    @Override
    public WorkInstructionResponseList retrieveByPcuOperation(AttachmentListRequest attachmentListRequest) throws Exception
    {
        if(attachmentListRequest.getPcu()!=null && !attachmentListRequest.getPcu().isEmpty() && attachmentListRequest.getOperation()!=null && !attachmentListRequest.getOperation().isEmpty())
        {
            List<WorkInstructionResponse> workInstructionList = workInstructionRepository.findByActiveAndSiteAndAttachmentListPcuAndAttachmentListOperation(1,attachmentListRequest.getSite(),attachmentListRequest.getPcu(),attachmentListRequest.getOperation());
            WorkInstructionResponseList workInstructionListResponse = new WorkInstructionResponseList(workInstructionList);
            return workInstructionListResponse;
        }
        throw new WorkInstructionException(2007," ");
    }

    @Override
    public WorkInstructionResponseList retrieveByResourceOperation(AttachmentListRequest attachmentListRequest) throws Exception {
        if(attachmentListRequest.getResource()!=null && !attachmentListRequest.getResource().isEmpty() && attachmentListRequest.getOperation()!=null && !attachmentListRequest.getOperation().isEmpty())
        {
            List<WorkInstructionResponse> workInstructionList = workInstructionRepository.findByActiveAndSiteAndAttachmentListResourceAndAttachmentListOperation(1,attachmentListRequest.getSite(),attachmentListRequest.getResource(),attachmentListRequest.getOperation());
            WorkInstructionResponseList workInstructionListResponse = new WorkInstructionResponseList(workInstructionList);
            return workInstructionListResponse;
        }
        throw new WorkInstructionException(2007," ");
    }

    @Override
    public WorkInstructionResponseList retrieveByItemOperationRouting(AttachmentListRequest attachmentListRequest) throws Exception
    {
        if(attachmentListRequest.getItem()!=null && !attachmentListRequest.getItem().isEmpty() && attachmentListRequest.getOperation()!=null && !attachmentListRequest.getOperation().isEmpty() && attachmentListRequest.getRouting()!=null && !attachmentListRequest.getRouting().isEmpty())
        {
            List<WorkInstructionResponse> workInstructionList = workInstructionRepository.findByActiveAndSiteAndAttachmentListItemAndAttachmentListOperationAndAttachmentListRouting(1,attachmentListRequest.getSite(),attachmentListRequest.getItem(),attachmentListRequest.getOperation(),attachmentListRequest.getRouting());
            WorkInstructionResponseList workInstructionListResponse = new WorkInstructionResponseList(workInstructionList);
            return workInstructionListResponse;
        }
        throw new WorkInstructionException(2008," ");
    }
    @Override
    public WorkInstructionResponseList retrieveByResource(AttachmentListRequest attachmentListRequest) throws Exception
    {
        if(attachmentListRequest.getResource()!=null && !attachmentListRequest.getResource().isEmpty())
        {
            List<WorkInstructionResponse> workInstructionList = workInstructionRepository.findByActiveAndSiteAndAttachmentListResource(1,attachmentListRequest.getSite(),attachmentListRequest.getResource());
            WorkInstructionResponseList workInstructionListResponse = new WorkInstructionResponseList(workInstructionList);
            return workInstructionListResponse;
        }
        throw new WorkInstructionException(2009," ");
    }
    @Override
    public WorkInstructionResponseList retrieveByResourcePcu(AttachmentListRequest attachmentListRequest) throws Exception
    {
        if(attachmentListRequest.getResource()!=null && !attachmentListRequest.getResource().isEmpty() && attachmentListRequest.getPcu()!=null && !attachmentListRequest.getPcu().isEmpty())
        {
            List<WorkInstructionResponse> workInstructionList = workInstructionRepository.findByActiveAndSiteAndAttachmentListResourceAndAttachmentListPcu(1,attachmentListRequest.getSite(),attachmentListRequest.getResource(),attachmentListRequest.getPcu());
            WorkInstructionResponseList workInstructionListResponse = new WorkInstructionResponseList(workInstructionList);
            return workInstructionListResponse;
        }
        throw new WorkInstructionException(2010," ");
    }
    @Override
    public WorkInstructionResponseList retrieveByShopOrderOperation(AttachmentListRequest attachmentListRequest) throws Exception
    {
        if(attachmentListRequest.getShopOrder()!=null && !attachmentListRequest.getShopOrder().isEmpty() && attachmentListRequest.getOperation()!=null && !attachmentListRequest.getOperation().isEmpty())
        {
            List<WorkInstructionResponse> workInstructionList = workInstructionRepository.findByActiveAndSiteAndAttachmentListShopOrderAndAttachmentListOperation(1,attachmentListRequest.getSite(),attachmentListRequest.getShopOrder(),attachmentListRequest.getOperation());
            WorkInstructionResponseList workInstructionListResponse = new WorkInstructionResponseList(workInstructionList);
            return workInstructionListResponse;
        }
        throw new WorkInstructionException(2011," ");
    }


    @Override
    public WorkInstructionResponseList retrieveByPcuAndMergeItem(AttachmentListRequest attachmentListRequest) throws Exception
    {
        if(attachmentListRequest.getPcu()!=null && !attachmentListRequest.getPcu().isEmpty())
        {
            List<WorkInstructionResponse> workInstructionListByPcu = workInstructionRepository.findByActiveAndSiteAndAttachmentListPcu(1,attachmentListRequest.getSite(),attachmentListRequest.getPcu());

            List<WorkInstructionResponse> workInstructionListByItem =  workInstructionRepository.findByActiveAndSiteAndAttachmentListItem(1,attachmentListRequest.getSite(),attachmentListRequest.getItem());
            for(int i=0;i<workInstructionListByItem.size();i++)
            {
                if(!workInstructionListByPcu.contains(workInstructionListByItem.get(i)))
                {
                    workInstructionListByPcu.add(workInstructionListByItem.get(i));
                }
            }
            WorkInstructionResponseList workInstructionListResponse = new WorkInstructionResponseList(workInstructionListByPcu);

            return workInstructionListResponse;
        }
        throw new WorkInstructionException(2012," ");
    }

    @Override
    public WorkInstructionResponseList retrieveByOperationRoutingPcuAndMergeItem(AttachmentListRequest attachmentListRequest) throws Exception
    {
        if(attachmentListRequest.getOperation()!=null && !attachmentListRequest.getOperation().isEmpty() && attachmentListRequest.getRouting()!=null && !attachmentListRequest.getRouting().isEmpty() && attachmentListRequest.getPcu()!=null && !attachmentListRequest.getPcu().isEmpty())
        {
            List<WorkInstructionResponse> workInstructionListByOperationRoutingPcu = workInstructionRepository.findByActiveAndSiteAndAttachmentListOperationAndAttachmentListRoutingAndAttachmentListPcu(1,attachmentListRequest.getSite(),attachmentListRequest.getOperation(),attachmentListRequest.getRouting(),attachmentListRequest.getPcu());

            List<WorkInstructionResponse> workInstructionListByItem =  workInstructionRepository.findByActiveAndSiteAndAttachmentListItem(1,attachmentListRequest.getSite(),attachmentListRequest.getItem());
            for(int i=0;i<workInstructionListByItem.size();i++)
            {
                if(!workInstructionListByOperationRoutingPcu.contains(workInstructionListByItem.get(i)))
                {
                    workInstructionListByOperationRoutingPcu.add(workInstructionListByItem.get(i));
                }
            }
            WorkInstructionResponseList workInstructionListResponse = new WorkInstructionResponseList(workInstructionListByOperationRoutingPcu);

            return workInstructionListResponse;
        }
        throw new WorkInstructionException(2013," ");
    }

    @Override
    public WorkInstructionResponseList retrieveByShopOrderAndMergeItem(AttachmentListRequest attachmentListRequest) throws Exception
    {
        if(attachmentListRequest.getShopOrder()!=null && !attachmentListRequest.getShopOrder().isEmpty() && attachmentListRequest.getItem()!=null && !attachmentListRequest.getItem().isEmpty())
        {
            List<WorkInstructionResponse> workInstructionListByShopOrder = workInstructionRepository.findByActiveAndSiteAndAttachmentListShopOrder(1,attachmentListRequest.getSite(),attachmentListRequest.getShopOrder());

            List<WorkInstructionResponse> workInstructionListByItem =  workInstructionRepository.findByActiveAndSiteAndAttachmentListItem(1,attachmentListRequest.getSite(),attachmentListRequest.getItem());
            for(int i=0;i<workInstructionListByItem.size();i++)
            {
                if(!workInstructionListByShopOrder.contains(workInstructionListByItem.get(i)))
                {
                    workInstructionListByShopOrder.add(workInstructionListByItem.get(i));
                }
            }
            WorkInstructionResponseList workInstructionListResponse = new WorkInstructionResponseList(workInstructionListByShopOrder);

            return workInstructionListResponse;
        }
        throw new WorkInstructionException(2014," ");
    }

    @Override
    public WorkInstructionResponseList retrieveByWorkCenterAndMergeResource(AttachmentListRequest attachmentListRequest) throws Exception
    {
        if(attachmentListRequest.getWorkCenter()!=null && !attachmentListRequest.getWorkCenter().isEmpty() && attachmentListRequest.getResource()!=null && !attachmentListRequest.getResource().isEmpty())
        {
            List<WorkInstructionResponse> workInstructionListByWorkCenter = workInstructionRepository.findByActiveAndSiteAndAttachmentListWorkCenter(1,attachmentListRequest.getSite(),attachmentListRequest.getWorkCenter());

            List<WorkInstructionResponse> workInstructionListByResource =  workInstructionRepository.findByActiveAndSiteAndAttachmentListResource(1,attachmentListRequest.getSite(),attachmentListRequest.getResource());
            for(int i=0;i<workInstructionListByResource.size();i++)
            {
                if(!workInstructionListByWorkCenter.contains(workInstructionListByResource.get(i)))
                {
                    workInstructionListByWorkCenter.add(workInstructionListByResource.get(i));
                }
            }
            WorkInstructionResponseList workInstructionListResponse = new WorkInstructionResponseList(workInstructionListByWorkCenter);

            return workInstructionListResponse;
        }
        throw new WorkInstructionException(2015," ");
    }

    @Override
    public WorkInstructionResponseList retrieveWorkInstructionByBom(AttachmentListRequest attachmentListRequest) throws Exception {
        if (attachmentListRequest.getBom() != null && !attachmentListRequest.getBom().isEmpty()) {
            List<WorkInstructionResponse> workInstructionList = workInstructionRepository.findByActiveAndSiteAndAttachmentListBom(1, attachmentListRequest.getSite(), attachmentListRequest.getBom());
            WorkInstructionResponseList workInstructionListResponse = new WorkInstructionResponseList(workInstructionList);
            return workInstructionListResponse;
        }
        throw new WorkInstructionException(2016, attachmentListRequest.getBom());
    }
    @Override
    public WorkInstructionResponseList retrieveWorkInstructionByBomVersion(AttachmentListRequest attachmentListRequest) throws Exception {
        if (attachmentListRequest.getBomVersion() != null && !attachmentListRequest.getBomVersion().isEmpty()) {
            List<WorkInstructionResponse> workInstructionList = workInstructionRepository.findByActiveAndSiteAndAttachmentListBomVersion(1, attachmentListRequest.getSite(), attachmentListRequest.getBomVersion());
            WorkInstructionResponseList workInstructionListResponse = new WorkInstructionResponseList(workInstructionList);
            return workInstructionListResponse;
        }
        throw new WorkInstructionException(2016, attachmentListRequest.getBomVersion());
    }

    @Override
    public WorkInstructionResponseList retrieveWorkInstructionByComponent(AttachmentListRequest attachmentListRequest) throws Exception {
        if (attachmentListRequest.getComponent() != null && !attachmentListRequest.getComponent().isEmpty()) {
            List<WorkInstructionResponse> workInstructionList = workInstructionRepository.findByActiveAndSiteAndAttachmentListComponent(1, attachmentListRequest.getSite(), attachmentListRequest.getComponent());
            WorkInstructionResponseList workInstructionListResponse = new WorkInstructionResponseList(workInstructionList);
            return workInstructionListResponse;
        }
        throw new WorkInstructionException(2017, attachmentListRequest.getComponent());
    }

    @Override
    public WorkInstructionResponseList retrieveWorkInstructionByComponentVersion(AttachmentListRequest attachmentListRequest) throws Exception {
        if (attachmentListRequest.getComponentVersion() != null && !attachmentListRequest.getComponentVersion().isEmpty()) {
            List<WorkInstructionResponse> workInstructionList = workInstructionRepository.findByActiveAndSiteAndAttachmentListComponentVersion(1, attachmentListRequest.getSite(), attachmentListRequest.getComponentVersion());
            WorkInstructionResponseList workInstructionListResponse = new WorkInstructionResponseList(workInstructionList);
            return workInstructionListResponse;
        }
        throw new WorkInstructionException(2017, attachmentListRequest.getComponentVersion());
    }



    @Override
    public List<String> generateCombinations(List<String> elements) {
        List<String> result = new ArrayList<>();
        generateCombinationsHelper(elements, "", 0, result);
        return result;
    }
    @Override
    public void generateCombinationsHelper(List<String> elements, String prefix, int startIndex, List<String> result) {
        result.add(prefix);
        for (int i = startIndex; i < elements.size(); i++) {
            generateCombinationsHelper(elements, prefix.isEmpty() ? elements.get(i) : prefix + "_" + elements.get(i), i + 1, result);
        }
    }

    @Override
    public List<WorkInstruction> getWorkInstructionByAttachment(List<Attachment> attachmentList) throws Exception
    {
        List<Attachment> attachmentPointsmodified = new ArrayList<>();
        for (Attachment obj : attachmentList) {
            obj.setWorkCenter(obj.getWorkCenter() == null ? "" : obj.getWorkCenter());
            obj.setResourceType(obj.getResourceType() == null ? "" : obj.getResourceType());
            obj.setCustomerOrder(obj.getCustomerOrder() == null ? "" : obj.getCustomerOrder());
            obj.setShopOrder(obj.getShopOrder() == null ? "" : obj.getShopOrder());
            obj.setItem(obj.getItem() == null ? "" : obj.getItem());
            obj.setItemVersion(obj.getItemVersion() == null ? "" : obj.getItemVersion());
            obj.setRoutingVersion(obj.getRoutingVersion() == null ? "" : obj.getRoutingVersion());
            obj.setRouting(obj.getRouting() == null ? "" : obj.getRouting());
            obj.setItemGroup(obj.getItemGroup() == null ? "" : obj.getItemGroup());
            obj.setResource(obj.getResource() == null ? "" : obj.getResource());
            obj.setBom(obj.getBom() == null ? "" : obj.getBom());
            obj.setBomVersion(obj.getBomVersion() == null ? "" : obj.getBomVersion());
            obj.setComponent(obj.getComponent() == null ? "" : obj.getComponent());
            obj.setComponentVersion(obj.getComponentVersion() == null ? "" : obj.getComponentVersion());
            obj.setOperation(obj.getOperation() == null ? "" : obj.getOperation());
            obj.setOrderNo(obj.getOrderNo() == null ? "" : obj.getOrderNo());
            obj.setBatchNo(obj.getBatchNo() == null ? "" : obj.getBatchNo());
            obj.setPhase(obj.getPhase() == null ? "" : obj.getPhase());
            attachmentPointsmodified.add(obj);
        }
        List<AttachmentPoint> attachmentPoints = new ArrayList<>();
        for(Attachment attachments : attachmentPointsmodified)
        {
            AttachmentPoint attachmentPoint = AttachmentPoint.builder().attachmentList(createFields(attachments)).build();
            attachmentPoints.add(attachmentPoint);
        }


        List<String> selectedFields = attachmentPoints.stream()
                .flatMap(attachmentPoint -> attachmentPoint.getAttachmentList().entrySet().stream()
                        .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                        .map(entry -> entry.getKey() + "_" + entry.getValue()))
                .collect(Collectors.toList());

        List<String> combinations = generateCombinations(selectedFields);
        combinations.remove(0);

        Query query = new Query();
        query.addCriteria(Criteria.where("tags").in(combinations).and("active").is(1));
        List<WorkInstruction> workInstructionList = mongoTemplate.find(query, WorkInstruction.class);
        return workInstructionList;
    }

    ListMaintenance listResponse=null;

    @Override
    public List<WorkListResponse> getWorkInstructionList(WorkListRequest workListRequest) throws Exception {
        List<WorkListResponse> workLists = new ArrayList<>();

        listResponse = webClientBuilder.build()
                .post()
                .uri(listMaintenanceRetrieveUrl)
                .bodyValue(workListRequest)
                .retrieve()
                .bodyToMono(ListMaintenance.class)
                .block();

        if (listResponse == null||listResponse.getHandle()==null) {
            throw new WorkInstructionException(3702, workListRequest.getList());
        }
        List<String> listColumnName = new ArrayList<>();
        for (Column column : listResponse.getColumnList()) {
            listColumnName.add(column.getColumnName());

        }
        if(workListRequest.getPcu()!=null&& !workListRequest.getPcu().isEmpty()){
            PcuRouterHeaderRequest pcuRouterHeaderRequest= PcuRouterHeaderRequest.builder().site(workListRequest.getSite()).pcuBo("PcuBO:"+workListRequest.getSite()+","+workListRequest.getPcu()).build();
            PcuRouterHeader pcuRouterHeader = retrievePcuRouterHeader(pcuRouterHeaderRequest);

            if (pcuRouterHeader == null||pcuRouterHeader.getHandle()==null) {
                throw new WorkInstructionException(3703, workListRequest.getList());
            }

            if(workListRequest.getOperationBO()!=null&&!workListRequest.getOperationBO().isEmpty() && workListRequest.getResource()!=null&& !workListRequest.getResource().isEmpty()){
                List<Attachment> attachmentList = new ArrayList<>();

                if(workListRequest.getItem()!=null||workListRequest.getItem()!="") {
                    String itemItemVersion = workListRequest.getItem() + "/" + workListRequest.getItemVersion();
                    workListRequest.setItem(itemItemVersion);
                }
                if(workListRequest.getRouting()!=null||workListRequest.getRouting()!="") {
                    String routingRoutingVersion = workListRequest.getRouting() + "/" + workListRequest.getRoutingVersion();
                    workListRequest.setRouting(routingRoutingVersion);
                }
                Attachment newAttachment =Attachment.builder().item(workListRequest.getItem()).itemGroup(workListRequest.getItemGroup()).routing(workListRequest.getRouting()).operation(workListRequest.getOperationBO()).workCenter(workListRequest.getWorkCenterBO()).resource(workListRequest.getResource()).resourceType(workListRequest.getResourceType()).customerOrder(workListRequest.getCustomerOrder()).shopOrder(workListRequest.getShopOrder()).pcu(workListRequest.getPcu()).component(workListRequest.getComponent()).bom(workListRequest.getBom()).componentVersion(workListRequest.getComponentVersion()).bomVersion(workListRequest.getBomVersion()).build();
                attachmentList.add(newAttachment);

                List<WorkInstruction> workInstructionListResponses =getWorkInstructionByAttachment(attachmentList);

                if (workInstructionListResponses == null) {
                    throw new WorkInstructionException(3703, workListRequest.getList());
                }
                workLists=setValuesOfWorkInstruction(listColumnName,workInstructionListResponses,pcuRouterHeader.getRouter().get(0).getR_route().get(0).getRoutingStepList());
                for(WorkListResponse workList:workLists){
                    List<ColumnList> columnLists=new ArrayList<>();
                    if(listColumnName.contains("Operation_StepID")){
                        String getOperationStepID= getOperationStep(workListRequest.getOperationBO(),pcuRouterHeader.getRouter().get(0).getR_route().get(0).getRoutingStepList());
                        ColumnList columnList=new ColumnList("Operation_StepId",getOperationStepID);
                        columnLists.add(columnList);
                    }
                    if(listColumnName.contains("StepID")){
                        String getOperationStepID= getOperationStep(workListRequest.getOperationBO(),pcuRouterHeader.getRouter().get(0).getR_route().get(0).getRoutingStepList());
                        if(!getOperationStepID.isEmpty()){
                            String stepId[]=getOperationStepID.split("/");
                            getOperationStepID=stepId[1];
                        }
                        ColumnList columnList=new ColumnList("StepId",getOperationStepID);
                        columnLists.add(columnList);
                    }
                    workList.getColumnLists().addAll(columnLists);
                    workList.setColumnLists(workList.getColumnLists());
                }

            }
        }

        return workLists;
    }

    public List<BatchRecipeInstruction> getBatchRecipeWorkInstructionList(WorkListRequest workListRequest) throws Exception {

        List<Attachment> attachmentList = new ArrayList<>();

        Attachment newAttachment =Attachment.builder().item(workListRequest.getItem()).itemVersion(workListRequest.getItemVersion()).orderNo(workListRequest.getOrderNo())
                .batchNo(workListRequest.getBatchNo()).operation(workListRequest.getOperationId()).phase(workListRequest.getPhaseId()).build();
        attachmentList.add(newAttachment);

        List<WorkInstruction> workInstructionListResponses = getWorkInstructionByAttachment(attachmentList);
        List<BatchRecipeInstruction> batchRecipeInstructions = new ArrayList<>();
        BatchRecipeInstruction batchRecipeInstruction = new BatchRecipeInstruction();
        for(WorkInstruction workInstruction : workInstructionListResponses){
            batchRecipeInstruction = new BatchRecipeInstruction();
            batchRecipeInstruction.setText(workInstruction.getText());
            batchRecipeInstruction.setUrl(workInstruction.getUrl());

            batchRecipeInstructions.add(batchRecipeInstruction);
        }

        if (batchRecipeInstructions.isEmpty()) {
            throw new WorkInstructionException(3704);
        }

        return batchRecipeInstructions;
    }

    private String getOperationStep(String operationBO, List<RoutingStep> routingStepList) {
        String operationStepID="";
        for(RoutingStep routingStep: routingStepList){
            if(routingStep.getStepType().equalsIgnoreCase("operation")){
                if(routingStep.getOperation().equals(operationBO)){
                    operationStepID=routingStep.getOperation()+"/"+routingStep.getStepId();
                }
            }else{
                for(RoutingStep step:routingStep.getRouterDetails().get(0).getRoutingStepList()){
                    if(step.getStepType().equalsIgnoreCase("operation")){
                        if(step.getOperation().equals(operationBO)){
                            operationStepID=step.getOperation()+"/"+step.getStepId();
                        }
                    }
                }
            }
        }
        return operationStepID;
    }

    private List<WorkListResponse> setValuesOfWorkInstruction(List<String> listColumnName, List<WorkInstruction> workInstructionResponseRespons, List<RoutingStep> routingStepList) {

        List<WorkListResponse> workLists = new ArrayList<>();

        for (WorkInstruction workInstruction : workInstructionResponseRespons) {
            List<ColumnList> columnLists = new ArrayList<>();

            for (String columnName : workInstruction.getFieldNames()) {
                if (columnExistsInListResponse(columnName)) {
                    try {
                        Field field = WorkInstruction.class.getDeclaredField(columnName);
                        field.setAccessible(true);
                        Object fieldValue = field.get(workInstruction);

                        if (fieldValue != null) {
                            ColumnList columnList = new ColumnList();
                            columnList.setDataField(columnName);
                            columnList.setDataAttribute(fieldValue.toString());
                            columnLists.add(columnList);
                        }
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            if(listColumnName.contains("WorkInstruction_Description")){

                ColumnList columnList = new ColumnList();
                columnList.setDataField("WorkInstruction_Description");
                columnList.setDataAttribute(workInstruction.getDescription());
                columnLists.add(columnList);

            }
            if(listColumnName.contains("WorkInstructionID_Revision")){

                ColumnList columnList = new ColumnList();
                columnList.setDataField("WorkInstructionID_Revision");
                columnList.setDataAttribute(workInstruction.getWorkInstruction()+"/"+workInstruction.getRevision());
                columnLists.add(columnList);
            }
            WorkListResponse workListResponse = new WorkListResponse();
            workListResponse.setColumnLists(columnLists);
            workLists.add(workListResponse);
        }
        return workLists;
    }
    private boolean columnExistsInListResponse(String columnName) {
        if (listResponse != null) {
            for (Column column : listResponse.getColumnList()) {
                String listColumn=column.getColumnName();
                if (columnName.toLowerCase().contains(listColumn.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }
    public PcuRouterHeader retrievePcuRouterHeader( PcuRouterHeaderRequest pcuRouterHeaderRequest)
    {
        PcuRouterHeader pcuRouterHeader = webClientBuilder.build()
                .post()
                .uri(pcuRouterHeaderRetrieveUrl)
                .bodyValue(pcuRouterHeaderRequest)
                .retrieve()
                .bodyToMono(PcuRouterHeader.class)
                .block();
        return pcuRouterHeader;
    }

    public PcuRouterHeader retrieveBatchRecipeHeader( PcuRouterHeaderRequest pcuRouterHeaderRequest)
    {
        PcuRouterHeader pcuRouterHeader = webClientBuilder.build()
                .post()
                .uri(pcuRouterHeaderRetrieveUrl)
                .bodyValue(pcuRouterHeaderRequest)
                .retrieve()
                .bodyToMono(PcuRouterHeader.class)
                .block();
        return pcuRouterHeader;
    }

    public Boolean logProductionRecord(WorkInstructionRequest workInstructionRequest)
    {
        ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                .eventType("WORK_INSTRUCTION")
                .userId(workInstructionRequest.getUserId())
                .site(workInstructionRequest.getSite())
                .workinstruction_BO("WorkInstructionBO:"+workInstructionRequest.getSite()+","+workInstructionRequest.getWorkInstruction()+","+workInstructionRequest.getRevision())
                .pcuBO(workInstructionRequest.getPcuBO())
                .shopOrderBO(workInstructionRequest.getShopOrderBO())
                .operation_bo(workInstructionRequest.getOperationBO())
//                .routerBO(workInstructionRequest.getRouterBO())
                .resourceBO(workInstructionRequest.getResourceBO())
                .instructionType(workInstructionRequest.getInstructionType())
                .description(workInstructionRequest.getDescription())
//                .itemBO(workInstructionRequest.getItemBO())
                .topic("production-log")
                .status("Active")
                .eventData(workInstructionRequest.getWorkInstruction()+" opened successfully")
                .build();

        Boolean productionLogged = webClientBuilder.build()
                .post()
                .uri(productionLogUrl)
                .bodyValue(productionLogRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
//        eventPublisher.publishEvent(new ProductionLogProducer(productionLogRequest));
        return true;
    }

    public WorkInstruction workInstructionBuilder(WorkInstructionRequest workInstructionRequest,List<String> tags)
    {
        WorkInstruction newWorkInstruction = WorkInstruction.builder()
                .site(workInstructionRequest.getSite())
                .workInstruction(workInstructionRequest.getWorkInstruction())
                .revision(workInstructionRequest.getRevision())
                .description(workInstructionRequest.getWorkInstruction())
                .status(workInstructionRequest.getStatus())
                .required(workInstructionRequest.isRequired())
                .currentVersion(workInstructionRequest.isCurrentVersion())
                .alwaysShowInNewWindow(workInstructionRequest.isAlwaysShowInNewWindow())
                .logViewing(workInstructionRequest.isLogViewing())
                .changeAlert(workInstructionRequest.isChangeAlert())
                .erpWi(workInstructionRequest.isErpWi())
                .instructionType(workInstructionRequest.getInstructionType())
                .erpFilename(workInstructionRequest.getErpFilename())
                .fileName(workInstructionRequest.getFileName())
                .file(workInstructionRequest.getFile())
                .tags(tags)
                .url(workInstructionRequest.getUrl())
                .text(workInstructionRequest.getText())
                .attachmentList(workInstructionRequest.getAttachmentList())
                .customDataList(workInstructionRequest.getCustomDataList())
                .active(1)
                .build();
        return newWorkInstruction;
    }
    private List<String> getAttachment(List<Attachment> attachmentList)
    {
        List<String> listOfAttachments = new ArrayList<>();
        for(Attachment attachment : attachmentList){
            List<String> listToBeConcatenated = new ArrayList<>();
            if(StringUtils.isNotBlank(attachment.getItemGroup()))
            {
                listToBeConcatenated.add("itemGroup_"+attachment.getItemGroup());
            }
            if(StringUtils.isNotBlank(attachment.getItem()) && StringUtils.isNotBlank(attachment.getItemVersion()))
            {
                listToBeConcatenated.add("item_"+attachment.getItem()+"/"+attachment.getItemVersion());
            }
            if(StringUtils.isNotBlank(attachment.getRouting()) && StringUtils.isNotBlank(attachment.getRoutingVersion()))
            {
                listToBeConcatenated.add("routing_"+attachment.getRouting()+"/"+attachment.getRoutingVersion());
            }
            if(StringUtils.isNotBlank(attachment.getOperation()))
            {
                listToBeConcatenated.add("operation_"+attachment.getOperation());
            }
            if(StringUtils.isNotBlank(attachment.getWorkCenter()))
            {
                listToBeConcatenated.add("workCenter_"+attachment.getWorkCenter());
            }
            if(StringUtils.isNotBlank(attachment.getResource()))
            {
                listToBeConcatenated.add("resource_"+attachment.getResource());
            }
            if(StringUtils.isNotBlank(attachment.getShopOrder()))
            {
                listToBeConcatenated.add("shopOrder_"+attachment.getShopOrder());
            }
            if(StringUtils.isNotBlank(attachment.getPcu()))
            {
                listToBeConcatenated.add("pcu_"+attachment.getPcu());
            }
            if(StringUtils.isNotBlank(attachment.getResourceType()))
            {
                listToBeConcatenated.add("resourceType_"+attachment.getResourceType());
            }
            if(StringUtils.isNotBlank(attachment.getCustomerOrder()))
            {
                listToBeConcatenated.add("customerOrder_"+attachment.getCustomerOrder());
            }
            if(StringUtils.isNotBlank(attachment.getBom()) && StringUtils.isNotBlank(attachment.getBomVersion()))
            {
                listToBeConcatenated.add("bom_"+attachment.getBom()+"/"+attachment.getBomVersion());
            }
            if(StringUtils.isNotBlank(attachment.getComponent()) && StringUtils.isNotBlank(attachment.getComponentVersion()))
            {
                listToBeConcatenated.add("component_"+attachment.getComponent()+"/"+attachment.getComponentVersion());
            }
            if(StringUtils.isNotBlank(attachment.getBatchNo()))
            {
                listToBeConcatenated.add("batchNo_"+attachment.getBatchNo());
            }
            if(StringUtils.isNotBlank(attachment.getOrderNo()))
            {
                listToBeConcatenated.add("orderNo_"+attachment.getOrderNo());
            }
            if(StringUtils.isNotBlank(attachment.getPhase()))
            {
                listToBeConcatenated.add("phase_"+attachment.getPhase());
            }
            String concatenatedAttachment = String.join("_",listToBeConcatenated);
            listOfAttachments.add(concatenatedAttachment);
        }
        return listOfAttachments;
    }
}



