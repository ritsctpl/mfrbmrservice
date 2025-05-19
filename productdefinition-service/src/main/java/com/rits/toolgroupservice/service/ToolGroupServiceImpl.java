package com.rits.toolgroupservice.service;


import com.rits.resourceservice.dto.AvailableResourceType;
import com.rits.toolgroupservice.dto.*;
import com.rits.toolgroupservice.exception.ToolGroupException;
import com.rits.toolgroupservice.model.Attachment;
import com.rits.toolgroupservice.model.MessageDetails;
import com.rits.toolgroupservice.model.ToolGroup;
import com.rits.toolgroupservice.model.ToolGroupMessageModel;
import com.rits.toolgroupservice.repository.ToolGroupRepository;
import com.rits.toolnumberservice.dto.ToolNumberRequest;
import com.rits.toolnumberservice.model.ToolNumber;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class ToolGroupServiceImpl implements ToolGroupService{
       private final ToolGroupRepository toolGroupRepository;

    private final WebClient.Builder webClientBuilder;
    private final MongoTemplate mongoTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final MessageSource localMessageSource;

    @Value("${auditlog-service.url}/producer")
    private String auditlogUrl;

    @Value("${toolnumber-service.url}/retrieveToolNumberByToolGroup")
    private String retrieveToolNumber;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }
    @Override
    public ToolGroupMessageModel createToolGroup(ToolGroupRequest toolGroupRequest) throws Exception{
        if(toolGroupRequest.getToolGroup().isEmpty() || toolGroupRequest.getToolGroup() == null)
        {
            throw new ToolGroupException(1205);
        }
        long recordPresent = toolGroupRepository.countByToolGroupAndSiteAndActive(toolGroupRequest.getToolGroup(), toolGroupRequest.getSite(), 1);
        if (recordPresent > 0) {
            throw new ToolGroupException(1200, toolGroupRequest.getToolGroup());
        }
        if(toolGroupRequest.getDescription() == null || toolGroupRequest.getDescription().isEmpty())
        {
            toolGroupRequest.setDescription(toolGroupRequest.getToolGroup());
        }
        if(toolGroupRequest.getStartCalibrationDate()!=null && !toolGroupRequest.getStartCalibrationDate().isEmpty() && toolGroupRequest.getExpirationDate()!=null && !toolGroupRequest.getExpirationDate().isEmpty()) {
            LocalDate expirationDate = LocalDate.parse(toolGroupRequest.getExpirationDate());
            LocalDate startCalibrationDate = LocalDate.parse(toolGroupRequest.getStartCalibrationDate());

            if (expirationDate .isBefore( startCalibrationDate)){
                throw new ToolGroupException(1107);
            }
        }

        List<String> tags = createTag(toolGroupRequest.getAttachmentList());

        ToolGroup toolGroup = buildToolGroup(toolGroupRequest,tags);
        toolGroup.setHandle("ToolGroupBO:" + toolGroupRequest.getSite() + "," + toolGroupRequest.getToolGroup());
        toolGroup.setToolGroup(toolGroupRequest.getToolGroup());
        toolGroup.setCreatedDateTime(LocalDateTime.now());
//        if(toolGroupRequest.getCalibrationType().equalsIgnoreCase("count")) {
//            toolGroup.setCurrentCalibrationCount(toolGroupRequest.getMaximumCalibrationCount() - toolGroupRequest.getCalibrationCount());
//        }
//        if(toolGroupRequest.getCalibrationType().equalsIgnoreCase("time")) {
//            if(toolGroupRequest.getCalibrationPeriod().contains("Days"))
//            {
//                String remainingString = toolGroupRequest.getCalibrationPeriod().replace("Days", "");
//                toolGroup.setExpirationDate(LocalDateTime.now().plusDays(Integer.parseInt(remainingString))+"");
//            }
//            if(toolGroupRequest.getCalibrationPeriod().contains("Months"))
//            {
//                String remainingString = toolGroupRequest.getCalibrationPeriod().replace("Months", "");
//                toolGroup.setExpirationDate(LocalDateTime.now().plusMonths(Integer.parseInt(remainingString))+"");
//            }
//            if(toolGroupRequest.getCalibrationPeriod().contains("Years"))
//            {
//                String remainingString = toolGroupRequest.getCalibrationPeriod().replace("Years", "");
//                toolGroup.setExpirationDate(LocalDateTime.now().plusYears(Integer.parseInt(remainingString))+"");
//            }
//        }
//        toolGroup.setDurationExpiration(LocalDateTime.now().plusSeconds(toolGroupRequest.getDuration()));
        String createdMessage = getFormattedMessage(47, toolGroupRequest.getToolGroup());
       return ToolGroupMessageModel.builder().message_details(new MessageDetails(createdMessage,"S")).response(toolGroupRepository.save(toolGroup)).build();
    }

    public List<String> createTag(List<Attachment> attachmentList)
    {
        List<String> listOfAttachments = new ArrayList<>();
        for(Attachment attachment : attachmentList)
        {
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
                if(StringUtils.isNotBlank(attachment.getOperation()) && StringUtils.isNotBlank(attachment.getOperationVersion()))
                {
                    listToBeConcatenated.add("operation_"+attachment.getOperation()+"/"+attachment.getOperationVersion());
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
                String concatenatedAttachment = String.join("_",listToBeConcatenated);
                listOfAttachments.add(concatenatedAttachment);
            }
            return listOfAttachments;
    }

    public ToolGroup buildToolGroup(ToolGroupRequest toolGroupRequest,List<String> tagList)
    {
        ToolGroup toolGroup = ToolGroup.builder()
                .toolQty(toolGroupRequest.getToolQty())
                .erpGroup(toolGroupRequest.getErpGroup())
                .active(1)
                .attachmentList(toolGroupRequest.getAttachmentList())
                .calibrationCount(toolGroupRequest.getCalibrationCount())
                .customDataList(toolGroupRequest.getCustomDataList())
                .trackingControl(toolGroupRequest.getTrackingControl())
                .description(toolGroupRequest.getDescription())
                .status(toolGroupRequest.getStatus())
                .location(toolGroupRequest.getLocation())
                .timeBased(toolGroupRequest.getTimeBased())
                .tags(tagList)
                .startCalibrationDate(toolGroupRequest.getStartCalibrationDate())
                .maximumCalibrationCount(toolGroupRequest.getMaximumCalibrationCount())
                .expirationDate(toolGroupRequest.getExpirationDate())
                .calibrationPeriod(toolGroupRequest.getCalibrationPeriod())
                .site(toolGroupRequest.getSite())
                .calibrationType(toolGroupRequest.getCalibrationType())
                .build();
        return toolGroup;
    }
    @Override
    public ToolGroupMessageModel updateToolGroup(ToolGroupRequest toolGroupRequest ) throws Exception {
        ToolGroup retrievedToolGroup = toolGroupRepository.findByToolGroupAndActive(toolGroupRequest.getToolGroup(), 1);
        if (retrievedToolGroup != null) {

            ToolNumberRequest retrieveRequest = ToolNumberRequest.builder().site(toolGroupRequest.getSite()).toolGroup(toolGroupRequest.getToolGroup()).build();
            List<ToolNumber> retrievedToolNumberRecord = webClientBuilder.build()
                    .post()
                    .uri(retrieveToolNumber)
                    .bodyValue(retrieveRequest)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<ToolNumber>>() {
                    })
                    .block();
            if(retrievedToolNumberRecord!=null && !retrievedToolNumberRecord.isEmpty())
            {
                if(!retrievedToolGroup.getTrackingControl().equals(toolGroupRequest.getTrackingControl()))
                {
                    throw new ToolGroupException(1105);
                }
            }
            if(toolGroupRequest.getDescription() == null || toolGroupRequest.getDescription().isEmpty())
            {
                toolGroupRequest.setDescription(toolGroupRequest.getToolGroup());
            }

            if(toolGroupRequest.getStartCalibrationDate()!=null && !toolGroupRequest.getStartCalibrationDate().isEmpty() && toolGroupRequest.getExpirationDate()!=null && !toolGroupRequest.getExpirationDate().isEmpty()) {
                LocalDate expirationDate = LocalDate.parse(toolGroupRequest.getExpirationDate());
                LocalDate startCalibrationDate = LocalDate.parse(toolGroupRequest.getStartCalibrationDate());

                if (expirationDate .isBefore( startCalibrationDate)){
                    throw new ToolGroupException(1107);
                }
            }

            List<String> tags = createTag(toolGroupRequest.getAttachmentList());

            ToolGroup  toolGroup = buildToolGroup(toolGroupRequest,tags);
            toolGroup.setHandle(retrievedToolGroup.getHandle());
            toolGroup.setToolGroup(retrievedToolGroup.getToolGroup());
            toolGroup.setSite(retrievedToolGroup.getSite());
            toolGroup.setCreatedDateTime(retrievedToolGroup.getCreatedDateTime());
            toolGroup.setModifiedDateTime(LocalDateTime.now());
//            if(toolGroupRequest.getCalibrationType().equalsIgnoreCase("count")) {
//                toolGroup.setCurrentCalibrationCount(toolGroupRequest.getMaximumCalibrationCount() - toolGroupRequest.getCalibrationCount());
//            }
//            if(toolGroupRequest.getCalibrationType().equalsIgnoreCase("time")) {
//                if(toolGroupRequest.getCalibrationPeriod().contains("Days"))
//                {
//                    String remainingString = toolGroupRequest.getCalibrationPeriod().replace("Days", "");
//                    toolGroup.setExpirationDate(LocalDateTime.now().plusDays(Integer.parseInt(remainingString))+"");
//                }
//                if(toolGroupRequest.getCalibrationPeriod().contains("Months"))
//                {
//                    String remainingString = toolGroupRequest.getCalibrationPeriod().replace("Months", "");
//                    toolGroup.setExpirationDate(LocalDateTime.now().plusMonths(Integer.parseInt(remainingString))+"");
//                }
//                if(toolGroupRequest.getCalibrationPeriod().contains("Years"))
//                {
//                    String remainingString = toolGroupRequest.getCalibrationPeriod().replace("Years", "");
//                    toolGroup.setExpirationDate(LocalDateTime.now().plusYears(Integer.parseInt(remainingString))+"");
//                }
//            }
//        toolGroup.setDurationExpiration(LocalDateTime.now().plusSeconds(toolGroupRequest.getDuration()));

            String createdMessage = getFormattedMessage(48, toolGroupRequest.getToolGroup());
            return ToolGroupMessageModel.builder().message_details(new MessageDetails(createdMessage,"S")).response(toolGroupRepository.save(toolGroup)).build();
        }
        throw new ToolGroupException(1201, toolGroupRequest.getToolGroup());
    }


    @Override
    public ToolGroupListResponseList getToolGroupListByCreationDate(ToolGroupRequest toolGroupRequest) throws Exception{
        List<ToolGroupListResponse> toolGroupListResponses = toolGroupRepository.findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(1,toolGroupRequest.getSite());
            return ToolGroupListResponseList.builder().toolGroupList(toolGroupListResponses).build();
    }

    @Override
    public ToolGroupListResponseList getToolGroupList(ToolGroupRequest toolGroupRequest) throws Exception{

        if (toolGroupRequest.getToolGroup() == null || toolGroupRequest.getToolGroup().isEmpty()) {
            return getToolGroupListByCreationDate(toolGroupRequest);
        } else {
            List<ToolGroupListResponse> toolGroupListResponses = toolGroupRepository.findByToolGroupContainingIgnoreCaseAndSiteAndActive(toolGroupRequest.getToolGroup(), toolGroupRequest.getSite(), 1);
            if (toolGroupListResponses != null && !toolGroupListResponses.isEmpty()) {
                return ToolGroupListResponseList.builder().toolGroupList(toolGroupListResponses).build();
            } else {
                throw new ToolGroupException(1201, toolGroupRequest.getToolGroup());
            }
        }
    }

    @Override
    public ToolGroup retrieveToolGroup (ToolGroupRequest toolGroupRequest) throws Exception{
        ToolGroup toolGroupList = toolGroupRepository.findByToolGroupAndSiteAndActive(toolGroupRequest.getToolGroup(), toolGroupRequest.getSite(), 1);
        if (toolGroupList != null ) {
            return toolGroupList;
        } else {
            throw new ToolGroupException(1201, toolGroupRequest.getToolGroup());
        }
    }

    @Override
    public ToolGroupMessageModel deleteToolGroup (ToolGroupRequest toolGroupRequest) throws Exception{
        if (toolGroupRepository.existsByToolGroupAndSiteAndActive(toolGroupRequest.getToolGroup(), toolGroupRequest.getSite(), 1)) {
            ToolGroup existingToolGroup = toolGroupRepository.findByToolGroup(toolGroupRequest.getToolGroup());
            existingToolGroup.setActive(0);
            existingToolGroup.setModifiedDateTime(LocalDateTime.now());
            toolGroupRepository.save(existingToolGroup);
            String createdMessage = getFormattedMessage(49, toolGroupRequest.getToolGroup());
            return ToolGroupMessageModel.builder().message_details(new MessageDetails(createdMessage,"S")).build();
        } else {
            throw new ToolGroupException(1201, toolGroupRequest.getToolGroup());
        }
    }
    @Override
    public boolean isToolGroupExist(ToolGroupRequest toolGroupRequest)  throws Exception{
        return toolGroupRepository.existsByToolGroupAndSiteAndActive(toolGroupRequest.getToolGroup(),toolGroupRequest.getSite() ,1);
    }
    @Override
    public AttachmentListResponseList getAttachmentList(ToolGroupRequest toolGroupRequest) throws Exception {
        ToolGroup toolGroup = toolGroupRepository.findByToolGroupAndActiveAndSite(toolGroupRequest.getToolGroup(), 1, toolGroupRequest.getSite());
        if (toolGroup == null) {
            throw new ToolGroupException(1201, toolGroupRequest.getToolGroup());
        }
        else if (toolGroup.getAttachmentList() != null && !toolGroup.getAttachmentList().isEmpty()) {

            List<AttachmentListResponse> attachmentListResponses = toolGroup.getAttachmentList()
                    .stream()
                    .map(toolAttachmentList -> AttachmentListResponse.builder()
                            .item(toolAttachmentList.getItem())
                            .itemVersion(toolAttachmentList.getItemVersion())
                            .operation(toolAttachmentList.getOperation())
                            .resource(toolAttachmentList.getResource())
                            .routing(toolAttachmentList.getRouting())
                            .routingVersion(toolAttachmentList.getRoutingVersion())
                            .sequence(toolAttachmentList.getSequence())
                            .workCenter(toolAttachmentList.getWorkCenter())
                            .shopOrder(toolAttachmentList.getShopOrder())
                            .build())
                    .collect(Collectors.toList());

            return AttachmentListResponseList.builder()
                    .attachmentList(attachmentListResponses)
                    .build();
        } else {
            throw new ToolGroupException(1203, toolGroupRequest.getToolGroup());
        }
    }

    public static Map<String, String> createFields(Attachment attachment) {
        Map<String, String> attachmentList = new LinkedHashMap<>();
        attachmentList.put("itemGroup",attachment.getItemGroup());
        attachmentList.put("item",attachment.getItem());
        attachmentList.put("routing",attachment.getRouting());
        attachmentList.put("pcu",attachment.getPcu());
        attachmentList.put("operation", attachment.getOperation());
        attachmentList.put("workCenter", attachment.getWorkCenter());
        attachmentList.put("resource", attachment.getResource());
        attachmentList.put("shopOrder", attachment.getShopOrder());
        return attachmentList;
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
    public  List<ToolGroup> retrieveByAttachment(List<Attachment> attachmentList) throws Exception
    {
        List<Attachment> attachmentPointsmodified = new ArrayList<>();
        for(Attachment attachment : attachmentList)
        {
            attachment.setSequence(attachment.getSequence()==null ? "" : attachment.getSequence());
            attachment.setItemGroup(attachment.getItemGroup()==null?"":attachment.getItemGroup());
            attachment.setItem(attachment.getItem()==null?"":attachment.getItem());
            attachment.setItemVersion(attachment.getItemVersion()==null?"":attachment.getItemVersion());
            attachment.setRouting(attachment.getRouting()==null?"":attachment.getRouting());
            attachment.setRoutingVersion(attachment.getRoutingVersion()==null?"":attachment.getRoutingVersion());
            attachment.setOperation(attachment.getOperation()==null?"":attachment.getOperation());
            attachment.setOperationVersion(attachment.getOperationVersion()==null?"":attachment.getOperationVersion());
            attachment.setWorkCenter(attachment.getWorkCenter()==null?"":attachment.getWorkCenter());
            attachment.setResource(attachment.getResource()==null?"":attachment.getResource());
            attachment.setShopOrder(attachment.getShopOrder()==null?"":attachment.getShopOrder());
            attachment.setPcu(attachment.getPcu()==null?"":attachment.getPcu());
            if(!attachment.getItem().isEmpty())
            {
                if(!attachment.getItemVersion().isEmpty())
                {
                    attachment.setItem(attachment.getItem()+"/"+attachment.getItemVersion());
                }
            }
            if(!attachment.getRouting().isEmpty())
            {
                if(!attachment.getRoutingVersion().isEmpty())
                {
                    attachment.setRouting(attachment.getRouting()+"/"+attachment.getRoutingVersion());
                }
            }
            if(!attachment.getOperation().isEmpty())
            {
                if(!attachment.getOperationVersion().isEmpty())
                {
                    attachment.setOperation(attachment.getOperation()+"/"+attachment.getOperationVersion());
                }
            }
            attachmentPointsmodified.add(attachment);
        }
        List<AttachmentPoint> attachmentPoints = new ArrayList<>();
        for(Attachment attachments : attachmentPointsmodified)
        {
            AttachmentPoint attachmentPoint = AttachmentPoint.builder().attachmentList(createFields(attachments)).build();
            attachmentPoints.add(attachmentPoint);
        }
        List<String> selectedFields = attachmentPoints.stream()
                .flatMap(attachmentPoint -> attachmentPoint.getAttachmentList().entrySet().stream()
                        .filter(entry -> !entry.getValue().isEmpty())
                        .map(entry -> entry.getKey() + "_" + entry.getValue()))
                .collect(Collectors.toList());

        List<String> combinations = generateCombinations(selectedFields);
        combinations.remove(0);

        Query query = new Query();
        query.addCriteria(Criteria.where("tags").in(combinations).and("active").is(1));
        List<ToolGroup> retrievedToolGroupList = mongoTemplate.find(query, ToolGroup.class);

      return retrievedToolGroupList;
    }
}
