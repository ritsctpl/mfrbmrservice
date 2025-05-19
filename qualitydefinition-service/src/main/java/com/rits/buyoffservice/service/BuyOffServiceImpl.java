package com.rits.buyoffservice.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.rits.buyoffservice.dto.*;
import com.rits.buyoffservice.exception.BuyOffException;
import com.rits.buyoffservice.model.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.rits.buyoffservice.repository.BuyOffRepository;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class BuyOffServiceImpl implements BuyOffService {

    private final BuyOffRepository repository;
    private final WebClient.Builder webClientBuilder;
    private final MongoTemplate mongoTemplate;

    @Value("${usergroup-service.url}/getAvailableUserGroup")
    private String availableUserGroup;

    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;

    @Value("${auditlog-service.url}/producer")
    private String auditlogUrl;

    @Override
    public MessageModel createBuyOff(BuyOffRequest buyOffRequest) throws Exception {
        if (!isBuyOffExist(buyOffRequest.getBuyOff(), buyOffRequest.getVersion(), buyOffRequest.getSite())) {
            List<BuyOff> buyOffs= repository.findByActiveAndSiteAndBuyOff(1,buyOffRequest.getSite(),buyOffRequest.getBuyOff());

            if(buyOffRequest.isCurrentVersion()) {
                buyOffs.stream().filter(BuyOff::isCurrentVersion)
                        .map(buyOff -> {
                                    buyOff.setModifiedDateTime(LocalDateTime.now());
                                    buyOff.setModifiedBy(buyOffRequest.getUserId());
                            buyOff.setHandle("BuyOffBO:"+buyOffRequest.getSite()+","+buyOffRequest.getBuyOff()+","+buyOffRequest.getVersion());
                                    buyOff.setCurrentVersion(false);
                                    return buyOff;
                                }
                        ).forEach(repository::save);
            }
            if(buyOffRequest.getDescription()==null || buyOffRequest.getDescription().isEmpty())
            {
                buyOffRequest.setDescription(buyOffRequest.getBuyOff());
            }
            List<String> combinations= new ArrayList<>();
            if(buyOffRequest.getAttachmentList() != null && !buyOffRequest.getAttachmentList().isEmpty())
            {
                combinations = getAttachment( buyOffRequest.getAttachmentList());
                buyOffRequest.setAttachmentList(formatAttachmentList(buyOffRequest.getAttachmentList()));
            }

            
            BuyOff buyOff = createBuilder(buyOffRequest,combinations);
            AuditLogRequest activityLog = AuditLogRequest.builder()
                    .site(buyOffRequest.getSite())
                    .change_stamp("Create")
                    .action_code("BUYOFF-CREATE")
                    .action_detail("BUYOFF Created "+buyOffRequest.getBuyOff())
                    .action_detail_handle("ActionDetailBO:"+buyOffRequest.getSite()+","+"BUYOFF-CREATE"+","+buyOffRequest.getUserId()+":"+"com.rits.buyoffservice.service")
                    .date_time(String.valueOf(LocalDateTime.now()))
                    .userId(buyOffRequest.getUserId())
                    .txnId("BUYOFF-CREATE"+String.valueOf(LocalDateTime.now())+buyOffRequest.getUserId())
                    .created_date_time(String.valueOf(LocalDateTime.now()))
                    .category("BUYOFF")
                    .build();

            webClientBuilder.build()
                    .post()
                    .uri(auditlogUrl)
                    .bodyValue(activityLog)
                    .retrieve()
                    .bodyToMono(AuditLogRequest.class)
                    .block();
            return MessageModel.builder().message_details(new MessageDetails(buyOffRequest.getBuyOff()+" Created SuccessFully","S")).response(repository.save(buyOff)).build();
        }
        throw new BuyOffException(2700,buyOffRequest.getBuyOff());
    }

    private BuyOff createBuilder(BuyOffRequest buyOffRequest, List<String> combinations) {
        return BuyOff.builder()
                .site(buyOffRequest.getSite())
                .buyOff(buyOffRequest.getBuyOff())
                .version(buyOffRequest.getVersion())
                .handle("BuyOffBO:" + buyOffRequest.getSite() + "," + buyOffRequest.getBuyOff() + "," + buyOffRequest.getVersion())
                .description(buyOffRequest.getDescription())
                .status(buyOffRequest.getStatus())
                .messageType(buyOffRequest.getMessageType())
                .partialAllowed(buyOffRequest.isPartialAllowed())
                .rejectAllowed(buyOffRequest.isRejectAllowed())
                .skipAllowed(buyOffRequest.isSkipAllowed())
                .currentVersion(buyOffRequest.isCurrentVersion())
                .userGroupList(buyOffRequest.getUserGroupList())
                .tags(combinations)
                .attachmentList(buyOffRequest.getAttachmentList())
                .customDataList(buyOffRequest.getCustomDataList())
                .active(1)
                .createdDateTime(LocalDateTime.now())
                .createdBy(buyOffRequest.getUserId())
                .build();
    }
    private List<AttachmentList> formatAttachmentList(List<AttachmentList> attachmentList) {
        for (AttachmentList attachment : attachmentList) {
            attachment.setSequence(attachment.getSequence() == null ? "" : attachment.getSequence());
            attachment.setQuantityRequired(attachment.getQuantityRequired() == null ? "" : attachment.getQuantityRequired());
            attachment.setItem(attachment.getItem() == null ? "" : attachment.getItem());
            attachment.setItemVersion(attachment.getItemVersion() == null ? "" : attachment.getItemVersion());
            attachment.setStepId(attachment.getStepId() == null ? "" : attachment.getStepId());
            attachment.setOperation(attachment.getOperation() == null ? "" : attachment.getOperation());
            attachment.setOperationVersion(attachment.getOperationVersion() == null ? "" : attachment.getOperationVersion());
            attachment.setWorkCenter(attachment.getWorkCenter() == null ? "" : attachment.getWorkCenter());
            attachment.setResource(attachment.getResource() == null ? "" : attachment.getResource());
            attachment.setResourceType(attachment.getResourceType() == null ? "" : attachment.getResourceType());
            attachment.setShopOrder(attachment.getShopOrder() == null ? "" : attachment.getShopOrder());
            attachment.setPcu(attachment.getPcu() == null ? "" : attachment.getPcu());
            attachment.setRouting(attachment.getRouting() == null ? "" : attachment.getRouting());
            attachment.setRoutingVersion(attachment.getRoutingVersion() == null ? "" : attachment.getRoutingVersion());
        }
        return attachmentList;
    }

    private List<String> getAttachment(List<AttachmentList> attachmentList) {
        List<String> listOfAttachments = new ArrayList<>();

        // Iterate through the attachmentList
        for (AttachmentList attachment : attachmentList) {
            List<String> listToBeConcatenated = new ArrayList<>();

            // Check each attribute and concatenate non-blank ones
            if (StringUtils.isNotBlank(attachment.getItem()) && StringUtils.isNotBlank(attachment.getItemVersion())) {
                listToBeConcatenated.add("item_" + attachment.getItem()+"/"+attachment.getItemVersion());
            }

            if (StringUtils.isNotBlank(attachment.getStepId())) {
                listToBeConcatenated.add("stepId_" + attachment.getStepId());
            }
            if (StringUtils.isNotBlank(attachment.getOperation()) && StringUtils.isNotBlank(attachment.getOperationVersion())) {
                listToBeConcatenated.add("operation_" + attachment.getOperation());
            }

            if (StringUtils.isNotBlank(attachment.getWorkCenter())) {
                listToBeConcatenated.add("workCenter_" + attachment.getWorkCenter());
            }
            if (StringUtils.isNotBlank(attachment.getResource())) {
                listToBeConcatenated.add("resource_" + attachment.getResource());
            }
            if (StringUtils.isNotBlank(attachment.getResourceType())) {
                listToBeConcatenated.add("resourceType_" + attachment.getResourceType());
            }
            if (StringUtils.isNotBlank(attachment.getShopOrder())) {
                listToBeConcatenated.add("shopOrder_" + attachment.getShopOrder());
            }
            if (StringUtils.isNotBlank(attachment.getPcu())) {
                listToBeConcatenated.add("pcu_" + attachment.getPcu());
            }
            if (StringUtils.isNotBlank(attachment.getRouting()) && StringUtils.isNotBlank(attachment.getRoutingVersion())) {
                listToBeConcatenated.add("routing_" + attachment.getRouting()+"/" + attachment.getRoutingVersion());

            }
            String concatenatedAttachment = String.join("_", listToBeConcatenated);
            listOfAttachments.add(concatenatedAttachment);
        }
        return listOfAttachments;
    }

    @Override
    public List<BuyOffTop50Record> retrieveTop50BuyOff(BuyOffRequest buyOffRequest) throws Exception{
        return repository.findTop50ByActiveAndSiteOrderByCreatedDateTimeAsc(1, buyOffRequest.getSite());
    }

    @Override
    public List<BuyOffTop50Record> retrieveAll(BuyOffRequest buyOffRequest) throws Exception{
        return repository.findByActiveAndSiteOrderByCreatedDateTimeAsc(1, buyOffRequest.getSite());
    }

    @Override
    public BuyOff retrieve(BuyOffRequest buyOffRequest) throws Exception{
        if (isBuyOffExist(buyOffRequest.getBuyOff(), buyOffRequest.getVersion(), buyOffRequest.getSite())) {
            return repository.findByBuyOffAndVersionAndSiteAndActive(buyOffRequest.getBuyOff(),buyOffRequest.getVersion(), buyOffRequest.getSite(),1);
        }
        throw new BuyOffException(2701,buyOffRequest.getBuyOff());
    }

    @Override
    public MessageModel update(BuyOffRequest buyOffRequest) throws Exception{
        if (isBuyOffExist(buyOffRequest.getBuyOff(), buyOffRequest.getVersion(), buyOffRequest.getSite())) {
            BuyOff existingBuyOff = repository.findByBuyOffAndVersionAndSiteAndActive(buyOffRequest.getBuyOff(),buyOffRequest.getVersion(),buyOffRequest.getSite(),1);
            if(buyOffRequest.getDescription()==null || buyOffRequest.getDescription().isEmpty())
            {
                buyOffRequest.setDescription(buyOffRequest.getBuyOff());
            }
            List<BuyOff> buyOffs=repository.findByActiveAndSiteAndBuyOff(1, buyOffRequest.getSite(), buyOffRequest.getBuyOff());
            if(buyOffRequest.isCurrentVersion()) {
                buyOffs.stream().filter(BuyOff::isCurrentVersion)
                        .map(buyOff -> {
                                    buyOff.setModifiedDateTime(LocalDateTime.now());
                                    buyOff.setModifiedBy(buyOffRequest.getUserId());
                                    buyOff.setCurrentVersion(false);
                                    return buyOff;
                                }
                        ).forEach(repository::save);
            }
            List<String> combinations= new ArrayList<>();
            if(buyOffRequest.getAttachmentList() != null && !buyOffRequest.getAttachmentList().isEmpty())
            {
                combinations = getAttachment( buyOffRequest.getAttachmentList());
                buyOffRequest.setAttachmentList(formatAttachmentList(buyOffRequest.getAttachmentList()));
            }



            BuyOff buyOff = updateBuilder(existingBuyOff,buyOffRequest,combinations);
            AuditLogRequest activityLog = AuditLogRequest.builder()
                    .site(buyOffRequest.getSite())
                    .change_stamp("Update")
                    .action_code("BUYOFF-UPDATE")
                    .action_detail("BUYOFF Updated "+buyOffRequest.getBuyOff())
                    .action_detail_handle("ActionDetailBO:"+buyOffRequest.getSite()+","+"BUYOFF-UPDATE"+","+buyOffRequest.getUserId()+":"+"com.rits.buyoffservice.service")
                    .date_time(String.valueOf(LocalDateTime.now()))
                    .userId(buyOffRequest.getUserId())
                    .txnId("BUYOFF-UPDATE"+String.valueOf(LocalDateTime.now())+buyOffRequest.getUserId())
                    .created_date_time(String.valueOf(LocalDateTime.now()))
                    .category("BUYOFF")
                    .build();

            webClientBuilder.build()
                    .post()
                    .uri(auditlogUrl)
                    .bodyValue(activityLog)
                    .retrieve()
                    .bodyToMono(AuditLogRequest.class)
                    .block();
            return MessageModel.builder().message_details(new MessageDetails(buyOffRequest.getBuyOff()+" updated SuccessFully","S")).response(repository.save(buyOff)).build();
        }
        throw new BuyOffException(2701,buyOffRequest.getBuyOff());
    }

    private BuyOff updateBuilder(BuyOff existingBuyOff, BuyOffRequest buyOffRequest, List<String> combinations) {
    return BuyOff.builder()
            .site(existingBuyOff.getSite())
            .buyOff(existingBuyOff.getBuyOff())
            .version(existingBuyOff.getVersion())
            .handle(existingBuyOff.getHandle())
            .description(buyOffRequest.getDescription())
            .status(buyOffRequest.getStatus())
            .messageType(buyOffRequest.getMessageType())
            .partialAllowed(buyOffRequest.isPartialAllowed())
            .rejectAllowed(buyOffRequest.isRejectAllowed())
            .skipAllowed(buyOffRequest.isSkipAllowed())
            .currentVersion(buyOffRequest.isCurrentVersion())
            .userGroupList(buyOffRequest.getUserGroupList())
            .tags(combinations)
            .attachmentList(buyOffRequest.getAttachmentList())
            .customDataList(buyOffRequest.getCustomDataList())
            .active(1)
            .createdBy(existingBuyOff.getCreatedBy())
            .modifiedBy(buyOffRequest.getUserId())
            .createdDateTime(existingBuyOff.getCreatedDateTime())
            .modifiedDateTime(LocalDateTime.now())
            .build();
    }

    @Override
    public MessageModel delete(BuyOffRequest buyOffRequest) throws Exception{
        if (isBuyOffExist(buyOffRequest.getBuyOff(), buyOffRequest.getVersion(), buyOffRequest.getSite())) {
            BuyOff existingRecord = repository.findByBuyOffAndVersionAndSiteAndActive(buyOffRequest.getBuyOff(), buyOffRequest.getVersion(), buyOffRequest.getSite(),1);
           existingRecord.setActive(0);
           repository.save(existingRecord);
            AuditLogRequest activityLog = AuditLogRequest.builder()
                    .site(buyOffRequest.getSite())
                    .change_stamp("Delete")
                    .action_code("BUYOFF-DELETE")
                    .action_detail("BUYOFF Deleted "+buyOffRequest.getBuyOff())
                    .action_detail_handle("ActionDetailBO:"+buyOffRequest.getSite()+","+"BUYOFF-DELETE"+","+buyOffRequest.getUserId()+":"+"com.rits.buyoffservice.service")
                    .date_time(String.valueOf(LocalDateTime.now()))
                    .userId(buyOffRequest.getUserId())
                    .txnId("BUYOFF-DELETE"+String.valueOf(LocalDateTime.now())+buyOffRequest.getUserId())
                    .created_date_time(String.valueOf(LocalDateTime.now()))
                    .category("BUYOFF")
                    .build();

            webClientBuilder.build()
                    .post()
                    .uri(auditlogUrl)
                    .bodyValue(activityLog)
                    .retrieve()
                    .bodyToMono(AuditLogRequest.class)
                    .block();
            return MessageModel.builder().message_details(new MessageDetails(buyOffRequest.getBuyOff()+" deleted SuccessFully","S")).build();
        }
        throw new BuyOffException(2701,buyOffRequest.getBuyOff());
    }

    @Override
    public boolean isBuyOffExist(String buyOff, String version, String site) throws Exception{
        if (buyOff != null && !buyOff.isEmpty()) {
            return repository.existsByBuyOffAndVersionAndSiteAndActiveEquals(buyOff,version, site, 1);
        }
        throw new BuyOffException(2704,buyOff);
    }

    @Override
    public List<BuyOff> retrieveByAttachmentDetails(AttachmentDetailsRequest attachment) throws Exception{
//        switch (attachmentDetailsRequest.getKey()) {
//            case "item":
//                return repository.findByActiveAndSiteAndAttachmentListItem(1, attachmentDetailsRequest.getSite(), attachmentDetailsRequest.getItem());
//            case "resource":
//                return repository.findByActiveAndSiteAndAttachmentListResource(1, attachmentDetailsRequest.getSite(), attachmentDetailsRequest.getResource());
//            case "operation":
//                return repository.findByActiveAndSiteAndAttachmentListOperation(1, attachmentDetailsRequest.getSite(), attachmentDetailsRequest.getOperation());
//            case "workCenter":
//                return repository.findByActiveAndSiteAndAttachmentListWorkCenter(1, attachmentDetailsRequest.getSite(), attachmentDetailsRequest.getWorkCenter());
//            case "shopOrder":
//                return repository.findByActiveAndSiteAndAttachmentListShopOrder(1, attachmentDetailsRequest.getSite(), attachmentDetailsRequest.getShopOrder());
//            case "pcu":
//                return repository.findByActiveAndSiteAndAttachmentListPcu(1, attachmentDetailsRequest.getSite(), attachmentDetailsRequest.getPcu());
//            default:
//                throw new IllegalArgumentException("Invalid field: " + attachmentDetailsRequest.getKey());
//        }
            attachment.setSequence(attachment.getSequence() == null ? "" : attachment.getSequence());
            attachment.setQuantityRequired(attachment.getQuantityRequired() == null ? "" : attachment.getQuantityRequired());
            attachment.setItem(attachment.getItem() == null ? "" : attachment.getItem());
            attachment.setStepId(attachment.getStepId() == null ? "" : attachment.getStepId());
            attachment.setOperation(attachment.getOperation() == null ? "" : attachment.getOperation());
            attachment.setWorkCenter(attachment.getWorkCenter() == null ? "" : attachment.getWorkCenter());
            attachment.setResource(attachment.getResource() == null ? "" : attachment.getResource());
            attachment.setResourceType(attachment.getResourceType() == null ? "" : attachment.getResourceType());
            attachment.setShopOrder(attachment.getShopOrder() == null ? "" : attachment.getShopOrder());
            attachment.setPcu(attachment.getPcu() == null ? "" : attachment.getPcu());
            attachment.setRouting(attachment.getRouting() == null ? "" : attachment.getRouting());

            if (!attachment.getItem().isEmpty()) {
                if (!attachment.getItemVersion().isEmpty()) {
                    attachment.setItem(attachment.getItem() + "/" + attachment.getItemVersion());
                }
            }
            if (!attachment.getRouting().isEmpty()) {
                if (!attachment.getRoutingVersion().isEmpty()) {
                    attachment.setRouting(attachment.getRouting() + "/" + attachment.getRoutingVersion());
                }
            }
            if (!attachment.getOperation().isEmpty()) {
                attachment.setOperation(attachment.getOperation());
//                if (!attachment.getOperationVersion().isEmpty()) {
//                }
            }
            AttachmentPoint attachmentPoint= AttachmentPoint.builder().attachmentList(createFields(attachment)).build();

            List<String> selectedFields=attachmentPoint.getAttachmentList().entrySet().stream().filter(entry ->
                !entry.getValue().isEmpty())
                    .map(entry ->entry.getKey() + "_" + entry.getValue())
                    .collect(Collectors.toList());
            List<String> combinations=generateCombinations(selectedFields);
            combinations.remove(0);
            Query query=new Query();
        query.addCriteria(
                Criteria.where("tags").in(combinations)
                        .and("active").is(1)
                        .and("currentVersion").is(true)
                        .and("site").is(attachment.getSite())
                        .and("status").is("Enabled")
        );
// Execute the query
        List<BuyOff> buyOffs = mongoTemplate.find(query, BuyOff.class);
        return buyOffs;

    }

    private List<String> generateCombinations(List<String> selectedFields) {
        List<String> result = new ArrayList<>();
        generateCombinationsHelper(selectedFields, "", 0, result);
        return result;
    }

    private void generateCombinationsHelper(List<String> elements, String prefix, int startIndex, List<String> result) {
        result.add(prefix);
        for (int i = startIndex; i < elements.size(); i++) {
            generateCombinationsHelper(elements, prefix.isEmpty() ? elements.get(i) : prefix + "_" + elements.get(i), i + 1, result);
        }
    }

    private Map<String, String> createFields(AttachmentDetailsRequest attachment) {
        Map<String, String> attachmentList = new LinkedHashMap<>();
        attachmentList.put("resourceType", attachment.getResourceType());
        attachmentList.put("item", attachment.getItem());
        attachmentList.put("stepId",attachment.getStepId());
        attachmentList.put("routing", attachment.getRouting());
        attachmentList.put("pcu", attachment.getPcu());
        attachmentList.put("operation", attachment.getOperation());
        attachmentList.put("workCenter", attachment.getWorkCenter());
        attachmentList.put("resource", attachment.getResource());
        attachmentList.put("shopOrder", attachment.getShopOrder());
        return attachmentList;
    }

    @Override
    public List<BuyOff> retrieveByResource(String site, String resource) throws Exception{
        if (resource != null && !resource.isEmpty()) {
            return repository.findByActiveAndSiteAndAttachmentListResource(1, site, resource);
        }
        throw new BuyOffException(2705,resource);
    }

    @Override
    public List<BuyOff> retrieveByOperation(String operation, String site) throws Exception{
        if (operation != null && !operation.isEmpty()) {
            return repository.findByActiveAndSiteAndAttachmentListOperation(1, site, operation);
        }
        throw new BuyOffException(2706,operation);
    }

    @Override
    public List<BuyOff> retrieveByWorkCenter(String site, String workCenter) throws Exception{
        if (workCenter != null && !workCenter.isEmpty()) {
            return repository.findByActiveAndSiteAndAttachmentListWorkCenter(1, site, workCenter);
        }
        throw new BuyOffException(2707,workCenter);
    }

    @Override
    public List<BuyOff> retrieveByShopOrder(String site, String shopOrder) throws Exception{
        if (shopOrder != null && !shopOrder.isEmpty()) {
            return repository.findByActiveAndSiteAndAttachmentListShopOrder(1, site, shopOrder);
        }
        throw new BuyOffException(2708,shopOrder);
    }

    @Override
    public List<BuyOff> retrieveByPcu(String site, String pcu) throws Exception{
        if (pcu != null && !pcu.isEmpty()) {
            return repository.findByActiveAndSiteAndAttachmentListPcu(1, site, pcu);
        }
        throw new BuyOffException(2709,pcu);
    }

    @Override
    public List<BuyOff> retrieveByItem(String site, String item) throws Exception{
        if (item != null && !item.isEmpty()) {
            return repository.findByActiveAndSiteAndAttachmentListItem(1, site, item);
        }
        throw new BuyOffException(2710,item);
    }

    @Override
    public List<String> retrieveBuyOffNameListByItem(AttachmentDetailsRequest attachmentDetailsRequest) throws Exception{
        if (attachmentDetailsRequest.getItem() != null && !attachmentDetailsRequest.getItem().isEmpty()) {
            List<BuyOff> listByItem = repository.findByActiveAndSiteAndAttachmentListItem(1, attachmentDetailsRequest.getSite(), attachmentDetailsRequest.getItem());
            List<String> buyOffNameListByItem = new ArrayList<String>();
            for (BuyOff buyOff : listByItem) {
                String buyOffName = buyOff.getBuyOff();
                buyOffNameListByItem.add(buyOffName);
            }
            return buyOffNameListByItem;
        }
        throw new BuyOffException(2710,attachmentDetailsRequest.getItem());
    }

    @Override
    public List<String> retrieveBuyOffNameByItemAndOperation(AttachmentDetailsRequest attachmentDetailsRequest) throws Exception{
        if (attachmentDetailsRequest.getItem() != null && !attachmentDetailsRequest.getItem().isEmpty() && attachmentDetailsRequest.getOperation() != null && !attachmentDetailsRequest.getOperation().isEmpty()) {
            List<BuyOff> listByItemAndOperation = repository.findByActiveAndSiteAndAttachmentListItemAndAttachmentListOperation(1, attachmentDetailsRequest.getSite(), attachmentDetailsRequest.getItem(),
                    attachmentDetailsRequest.getOperation());
            List<String> buyOffNameListByItemAndOperation = new ArrayList<String>();
            for (BuyOff buyOff : listByItemAndOperation) {
                String buyOffName = buyOff.getBuyOff();
                buyOffNameListByItemAndOperation.add(buyOffName);
            }
            return buyOffNameListByItemAndOperation;
        }
        throw new BuyOffException(2711,attachmentDetailsRequest.getItem()+" + "+attachmentDetailsRequest.getOperation());
    }

    @Override
    public List<String> retrieveBuyOffNameByPcuAndOperation(AttachmentDetailsRequest attachmentDetailsRequest) throws Exception{
        if (attachmentDetailsRequest.getPcu() != null && !attachmentDetailsRequest.getPcu().isEmpty() && attachmentDetailsRequest.getOperation() != null && !attachmentDetailsRequest.getOperation().isEmpty()) {
            List<BuyOff> listByPcuAndOperation = repository.findByActiveAndSiteAndAttachmentListPcuAndAttachmentListOperation(1, attachmentDetailsRequest.getSite(), attachmentDetailsRequest.getPcu(),
                    attachmentDetailsRequest.getOperation());
            List<String> buyOffNameListByPcuAndOperation = new ArrayList<String>();
            for (BuyOff buyOff : listByPcuAndOperation) {
                String buyOffName = buyOff.getBuyOff();
                buyOffNameListByPcuAndOperation.add(buyOffName);
            }
            return buyOffNameListByPcuAndOperation;
        }
        throw new BuyOffException(2712,attachmentDetailsRequest.getPcu()+" + "+attachmentDetailsRequest.getOperation());
    }

    @Override
    public List<String> retrieveBuyOffNameByItemAndOperationAndRouting(AttachmentDetailsRequest attachmentDetailsRequest) throws Exception{
        if (attachmentDetailsRequest.getItem() != null && !attachmentDetailsRequest.getItem().isEmpty() && attachmentDetailsRequest.getOperation() != null && !attachmentDetailsRequest.getOperation().isEmpty() && attachmentDetailsRequest.getRouting() != null && !attachmentDetailsRequest.getRouting().isEmpty()) {
            List<BuyOff> listByItemAndOperationAndRouting = repository
                    .findByActiveAndSiteAndAttachmentListItemAndAttachmentListOperationAndAttachmentListRouting(1, attachmentDetailsRequest.getSite(), attachmentDetailsRequest.getItem(), attachmentDetailsRequest.getOperation(), attachmentDetailsRequest.getRouting());
            List<String> buyOffNameListByItemAndOperationAndRouting = new ArrayList<String>();
            for (BuyOff buyOff : listByItemAndOperationAndRouting) {
                String buyOffName = buyOff.getBuyOff();
                buyOffNameListByItemAndOperationAndRouting.add(buyOffName);
            }
            return buyOffNameListByItemAndOperationAndRouting;
        }
        throw new BuyOffException(2713,attachmentDetailsRequest.getItem()+" + "+attachmentDetailsRequest.getOperation()+" + "+attachmentDetailsRequest.getRouting());
    }

    @Override
    public List<String> retrieveBuyOffNameListByResource(AttachmentDetailsRequest attachmentDetailsRequest) throws Exception{
        if (attachmentDetailsRequest.getResource() != null && !attachmentDetailsRequest.getResource().isEmpty()) {
            List<BuyOff> listByResource = repository.findByActiveAndSiteAndAttachmentListResource(1, attachmentDetailsRequest.getSite(), attachmentDetailsRequest.getResource());
            List<String> buyOffNameListByResource = new ArrayList<String>();
            for (BuyOff buyOff : listByResource) {
                String buyOffName = buyOff.getBuyOff();
                buyOffNameListByResource.add(buyOffName);
            }
            return buyOffNameListByResource;
        }
        throw new BuyOffException(2705,attachmentDetailsRequest.getResource());
    }

    @Override
    public List<String> retrieveBuyOffNameListByResourceAndPcu(AttachmentDetailsRequest attachmentDetailsRequest) throws Exception{
        if (attachmentDetailsRequest.getResource() != null && !attachmentDetailsRequest.getResource().isEmpty() && attachmentDetailsRequest.getPcu() != null && !attachmentDetailsRequest.getPcu().isEmpty()) {
            List<BuyOff> listByResourceAndPcu = repository.findByActiveAndSiteAndAttachmentListResourceAndAttachmentListPcu(1, attachmentDetailsRequest.getSite(), attachmentDetailsRequest.getResource(), attachmentDetailsRequest.getPcu());
            List<String> buyOffNameListByResourceAndPcu = new ArrayList<String>();
            for (BuyOff buyOff : listByResourceAndPcu) {
                String buyOffName = buyOff.getBuyOff();
                buyOffNameListByResourceAndPcu.add(buyOffName);
            }
            return buyOffNameListByResourceAndPcu;
        }
        throw new BuyOffException(2719,attachmentDetailsRequest.getResource()+ " + " +attachmentDetailsRequest.getPcu());
    }

    @Override
    public List<String> retrieveBuyOffNameListByShopOrderAndOperation(AttachmentDetailsRequest attachmentDetailsRequest) throws Exception{
        if (attachmentDetailsRequest.getShopOrder() != null && !attachmentDetailsRequest.getShopOrder().isEmpty() && attachmentDetailsRequest.getOperation() != null && !attachmentDetailsRequest.getOperation().isEmpty()) {
            List<BuyOff> listByShopOrderAndOperation = repository
                    .findByActiveAndSiteAndAttachmentListShopOrderAndAttachmentListOperation(1, attachmentDetailsRequest.getSite(), attachmentDetailsRequest.getShopOrder(), attachmentDetailsRequest.getOperation());
            List<String> buyOffNameListByShopOrderAndOperation = new ArrayList<String>();
            for (BuyOff buyOff : listByShopOrderAndOperation) {
                String buyOffName = buyOff.getBuyOff();
                buyOffNameListByShopOrderAndOperation.add(buyOffName);
            }
            return buyOffNameListByShopOrderAndOperation;
        }
        throw new BuyOffException(2720,attachmentDetailsRequest.getShopOrder()+" + "+attachmentDetailsRequest.getOperation());
    }

    @Override
    public List<String> retrieveBuyOffNameListByPcu(AttachmentDetailsRequest attachmentDetailsRequest) throws Exception{
        if (attachmentDetailsRequest.getPcu() != null && !attachmentDetailsRequest.getPcu().isEmpty() && attachmentDetailsRequest.getItem() != null && !attachmentDetailsRequest.getItem().isEmpty()) {
            List<BuyOff> listByPcu = repository.findByActiveAndSiteAndAttachmentListPcu(1, attachmentDetailsRequest.getSite(), attachmentDetailsRequest.getPcu());
            List<String> buyOffNameListByPcu = new ArrayList<String>();
            for (BuyOff buyOff : listByPcu) {
                String buyOffName = buyOff.getBuyOff();
                buyOffNameListByPcu.add(buyOffName);
            }
            List<String> getByitemList = retrieveBuyOffNameListByItem(attachmentDetailsRequest);
            for (int i = 0; i < getByitemList.size(); i++) {
                if (!buyOffNameListByPcu.contains(getByitemList.get(i))) {
                    buyOffNameListByPcu.add(getByitemList.get(i));
                }
            }
            return buyOffNameListByPcu;
        }
        throw new BuyOffException(2721,attachmentDetailsRequest.getPcu()+" + "+attachmentDetailsRequest.getItem());
    }

    @Override
    public List<String> retrieveBuyOffNameByOperationAndRoutingAndPcuAndMergeItemOp(AttachmentDetailsRequest attachmentDetailsRequest) throws Exception{
        if (attachmentDetailsRequest.getRouting() != null && !attachmentDetailsRequest.getRouting().isEmpty() && attachmentDetailsRequest.getOperation() != null && !attachmentDetailsRequest.getOperation().isEmpty() && attachmentDetailsRequest.getPcu() != null && !attachmentDetailsRequest.getPcu().isEmpty() && attachmentDetailsRequest.getItem() != null && !attachmentDetailsRequest.getItem().isEmpty()) {
            List<BuyOff> listByOperationAndRoutingAndPcu = repository
                    .findByAttachmentListOperationAndAttachmentListRoutingAndAttachmentListPcu(attachmentDetailsRequest.getOperation(), attachmentDetailsRequest.getRouting(), attachmentDetailsRequest.getPcu());
            List<String> buyOffNameListByOperationAndRoutingAndPcu = new ArrayList<String>();
            for (BuyOff buyOff : listByOperationAndRoutingAndPcu) {
                String buyOffName = buyOff.getBuyOff();
                System.out.println(buyOffName);
                buyOffNameListByOperationAndRoutingAndPcu.add(buyOffName);
            }
            List<String> getByitemList = retrieveBuyOffNameListByItem(attachmentDetailsRequest);
            for (int i = 0; i < getByitemList.size(); i++) {
                if (!buyOffNameListByOperationAndRoutingAndPcu.contains(getByitemList.get(i))) {
                    buyOffNameListByOperationAndRoutingAndPcu.add(getByitemList.get(i));
                }
            }
            return buyOffNameListByOperationAndRoutingAndPcu;
        }
        throw new BuyOffException(2722,attachmentDetailsRequest.getRouting()+" + "+attachmentDetailsRequest.getOperation()+" + "+attachmentDetailsRequest.getPcu()+" + "+attachmentDetailsRequest.getItem());
    }

    @Override
    public List<String> retrieveBuyOffNameByWorkCenterAndMergeResourceList(AttachmentDetailsRequest attachmentDetailsRequest) throws Exception{
        if (attachmentDetailsRequest.getWorkCenter() != null && !attachmentDetailsRequest.getWorkCenter().isEmpty() && attachmentDetailsRequest.getResource() != null && !attachmentDetailsRequest.getResource().isEmpty()) {
            List<BuyOff> listByWorkCenter = repository.findByActiveAndSiteAndAttachmentListWorkCenter(1, attachmentDetailsRequest.getSite(), attachmentDetailsRequest.getWorkCenter());
            List<String> buyOffNameListByWorkCenter = new ArrayList<String>();
            for (BuyOff buyOff : listByWorkCenter) {
                String buyOffName = buyOff.getBuyOff();
                buyOffNameListByWorkCenter.add(buyOffName);
            }
            List<String> getByResourceList = retrieveBuyOffNameListByResource(attachmentDetailsRequest);
            for (int i = 0; i < getByResourceList.size(); i++) {
                if (!buyOffNameListByWorkCenter.contains(getByResourceList.get(i))) {
                    buyOffNameListByWorkCenter.add(getByResourceList.get(i));
                }
            }
            return buyOffNameListByWorkCenter;
        }
        throw new BuyOffException(2723,attachmentDetailsRequest.getWorkCenter()+" + "+attachmentDetailsRequest.getResource());
    }

    @Override
    public List<String> retrieveBuyOffNameListByShopOrderAndMergeItemList(AttachmentDetailsRequest attachmentDetailsRequest) throws Exception{
        if (attachmentDetailsRequest.getShopOrder() != null && !attachmentDetailsRequest.getShopOrder().isEmpty() && attachmentDetailsRequest.getItem() != null && !attachmentDetailsRequest.getItem().isEmpty()) {
            List<BuyOff> listByShopOrder = repository.findByActiveAndSiteAndAttachmentListShopOrder(1, attachmentDetailsRequest.getSite(), attachmentDetailsRequest.getShopOrder());
            List<String> buyOffNameListByShopOrder = new ArrayList<String>();
            for (BuyOff buyOff : listByShopOrder) {
                String buyOffName = buyOff.getBuyOff();
                buyOffNameListByShopOrder.add(buyOffName);
            }
            List<String> getByitemList = retrieveBuyOffNameListByItem(attachmentDetailsRequest);
            for (int i = 0; i < getByitemList.size(); i++) {
                if (!buyOffNameListByShopOrder.contains(getByitemList.get(i))) {
                    buyOffNameListByShopOrder.add(getByitemList.get(i));
                }
            }
            return buyOffNameListByShopOrder;
        }
        throw new BuyOffException(2724,attachmentDetailsRequest.getShopOrder()+" + "+attachmentDetailsRequest.getItem());
    }
    @Override
    public BuyOff associateResourceUserGroup(AssociateUserGroup associateUserGroup) throws Exception
    {
        if(associateUserGroup.getBuyOff() != null && !associateUserGroup.getBuyOff().isEmpty())
        {
            if(associateUserGroup.getUserGroupList() != null && !associateUserGroup.getUserGroupList().isEmpty()) {
                if (isBuyOffExist(associateUserGroup.getBuyOff(), associateUserGroup.getVersion(), associateUserGroup.getSite())) {
                    BuyOff existingGroupList = repository.findByBuyOffAndVersionAndSiteAndActive(associateUserGroup.getBuyOff(), associateUserGroup.getVersion() ,associateUserGroup.getSite(),1);
                    for (int i = 0; i < associateUserGroup.getUserGroupList().size(); i++) {
                        UserGroupList userGroup = new UserGroupList(associateUserGroup.getUserGroupList().get(i));
                        existingGroupList.getUserGroupList().add(userGroup);
                    }
                    repository.save(existingGroupList);
                    return existingGroupList;
                }
                throw new BuyOffException(2701,associateUserGroup.getBuyOff());
            }
            throw new BuyOffException(2718);
        }
        throw new BuyOffException(2701,associateUserGroup.getBuyOff());
    }
    @Override
    public BuyOff removeUserGroupType( AssociateUserGroup associateUserGroup) throws Exception{
        if(associateUserGroup.getBuyOff() != null && !associateUserGroup.getBuyOff().isEmpty())
        {
            if(associateUserGroup.getBuyOff() != null && !associateUserGroup.getBuyOff().isEmpty()){
                if (isBuyOffExist(associateUserGroup.getBuyOff(), associateUserGroup.getVersion(), associateUserGroup.getSite())) {
                    BuyOff existingBuyOff = repository.findByBuyOffAndVersionAndSiteAndActive(associateUserGroup.getBuyOff(), associateUserGroup.getVersion(), associateUserGroup.getSite(),1);
                List<UserGroupList> existingUserGroupList = existingBuyOff.getUserGroupList();
                    existingUserGroupList.removeIf(association -> associateUserGroup.getUserGroupList().contains(association.getUserGroup()));
                    existingBuyOff.setUserGroupList(existingUserGroupList);
                repository.save(existingBuyOff);
                return existingBuyOff;
            }
                throw new BuyOffException(2701,associateUserGroup.getBuyOff());
            }
            throw new BuyOffException(2718);
        }
        throw new BuyOffException(2701,associateUserGroup.getBuyOff());
    }


    @Override
    public AvailableUserGroups availableUserGroup(AssociateUserGroup associateUserGroup) throws Exception
    {
        if(isBuyOffExist(associateUserGroup.getBuyOff(), associateUserGroup.getVersion(), associateUserGroup.getSite()))
        {
            UserGroupRequest userGroupRequest = new UserGroupRequest(associateUserGroup.getSite());
            List<AvailableUserGroup> userGroupList = webClientBuilder.build()
                    .post()
                    .uri(availableUserGroup)
                    .bodyValue(userGroupRequest)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<AvailableUserGroup>>() {
                    })
                    .block();
            BuyOff existingRecord = repository.findByBuyOffAndVersionAndSiteAndActive(associateUserGroup.getBuyOff(), associateUserGroup.getVersion(), associateUserGroup.getSite(),1);
            List<UserGroupList> existingUserGroupList = existingRecord.getUserGroupList();
            List<String> allUserGroup = new ArrayList<>();
            List<String> associatedUserGroup = new ArrayList<>();
            for(int i=0;i<userGroupList.size();i++)
            {
                String userGroup = userGroupList.get(i).getUserGroup();
                allUserGroup.add(userGroup);
            }
            for(int i=0;i<existingUserGroupList.size();i++)
            {
                String existingUserGroup = existingUserGroupList.get(i).getUserGroup();
                associatedUserGroup.add(existingUserGroup);
            }
            for(int i =0;i<associatedUserGroup.size();i++)
            {
                if(allUserGroup.contains(associatedUserGroup.get(i)))
                {
                    allUserGroup.remove(associatedUserGroup.get(i));
                }
            }
            List<AvailableUserGroup> availableUserGroupList = new ArrayList<AvailableUserGroup>();
            for(int i=0;i<allUserGroup.size();i++)
            {
                AvailableUserGroup updatedUserGroupList = new AvailableUserGroup(allUserGroup.get(i));
                availableUserGroupList.add(updatedUserGroupList);
            }
            AvailableUserGroups availableUserGroup = new AvailableUserGroups(availableUserGroupList);
            return availableUserGroup;
        }
        throw new BuyOffException(2700,associateUserGroup.getBuyOff());
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
        if (extensionResponse == null) {
            throw new BuyOffException(800);
        }
        return extensionResponse;
    }

    @Override
    public boolean isSkipAllowed(String handle) {
        if(repository.existsByHandleAndActive(handle,1)){
            BuyOff buyOff=repository.findByActiveAndHandle(1,handle);
            return buyOff.isSkipAllowed();
        }else{
            throw new BuyOffException(2701,handle);
        }
    }

    @Override
    public boolean isPartialAllowed(String handle) {
        if(repository.existsByHandleAndActive(handle,1)){
            BuyOff buyOff=repository.findByActiveAndHandle(1,handle);
            return buyOff.isPartialAllowed();
        }else{
            throw new BuyOffException(2701,handle);
        }
    }

    @Override
    public boolean isRejectAllowed(String handle) {
        if(repository.existsByHandleAndActive(handle,1)){
            BuyOff buyOff=repository.findByActiveAndHandle(1,handle);
            return buyOff.isRejectAllowed();
        }else{
            throw new BuyOffException(2701,handle);
        }
    }

//        @Override
//        public List<String> retrieveAttachmentDetailsList(AttachmentDetailsRequest attachmentDetailsRequest)
//        {
//            if(attachmentDetailsRequest.getItem()!=null && !attachmentDetailsRequest.getItem().isEmpty())
//            {
//                return retrieveBuyOffNameListByItem(attachmentDetailsRequest);
//        } else if (attachmentDetailsRequest.getItem()!=null && !attachmentDetailsRequest.getItem().isEmpty() && attachmentDetailsRequest.getOperation()!=null && !attachmentDetailsRequest.getOperation().isEmpty() ) {
//                return retrieveBuyOffNameByItemAndOperation(attachmentDetailsRequest);
//            } else if (attachmentDetailsRequest.getPcu()!=null && !attachmentDetailsRequest.getPcu().isEmpty() && attachmentDetailsRequest.getOperation()!=null && !attachmentDetailsRequest.getOperation().isEmpty()) {
//                return retrieveBuyOffNameByPcuAndOperation(attachmentDetailsRequest);
//            } else if (attachmentDetailsRequest.getItem()!=null && !attachmentDetailsRequest.getItem().isEmpty() && attachmentDetailsRequest.getOperation()!=null && !attachmentDetailsRequest.getOperation().isEmpty() && attachmentDetailsRequest.getRouting()!=null && !attachmentDetailsRequest.getRouting().isEmpty()) {
//                return retrieveBuyOffNameByItemAndOperationAndRouting(attachmentDetailsRequest);
//            } else if (attachmentDetailsRequest.getResource()!=null && !attachmentDetailsRequest.getResource().isEmpty()) {
//                return retrieveBuyOffNameListByResource(attachmentDetailsRequest);
//            } else if (attachmentDetailsRequest.getResource()!=null && !attachmentDetailsRequest.getResource().isEmpty() && attachmentDetailsRequest.getPcu()!=null && !attachmentDetailsRequest.getPcu().isEmpty()) {
//                return retrieveBuyOffNameListByResourceAndPcu(attachmentDetailsRequest);
//            } else if (attachmentDetailsRequest.getShopOrder()!=null && !attachmentDetailsRequest.getShopOrder().isEmpty() && attachmentDetailsRequest.getOperation()!=null && !attachmentDetailsRequest.getOperation().isEmpty()) {
//                return retrieveBuyOffNameListByShopOrderAndOperation(attachmentDetailsRequest);
//            } else if (attachmentDetailsRequest.getPcu()!=null && !attachmentDetailsRequest.getPcu().isEmpty()) {
//                return retrieveBuyOffNameListByPcu(attachmentDetailsRequest);
//            } else if (attachmentDetailsRequest.getOperation()!=null && !attachmentDetailsRequest.getOperation().isEmpty() && attachmentDetailsRequest.getRouting()!=null && !attachmentDetailsRequest.getRouting().isEmpty() &&attachmentDetailsRequest.getPcu()!=null && !attachmentDetailsRequest.getPcu().isEmpty() && attachmentDetailsRequest.getItem()!=null && !attachmentDetailsRequest.getItem().isEmpty()) {
//                return retrieveBuyOffNameByOperationAndRoutingAndPcuAndMergeItemOp(attachmentDetailsRequest);
//            } else if (attachmentDetailsRequest.getWorkCenter()!=null && !attachmentDetailsRequest.getWorkCenter().isEmpty() && attachmentDetailsRequest.getResource()!=null && !attachmentDetailsRequest.getResource().isEmpty()) {
//                return retrieveBuyOffNameByWorkCenterAndMergeResourceList(attachmentDetailsRequest);
//            } else if (attachmentDetailsRequest.getShopOrder()!=null && !attachmentDetailsRequest.getShopOrder().isEmpty() && attachmentDetailsRequest.getItem()!=null && !attachmentDetailsRequest.getItem().isEmpty()) {
//                return retrieveBuyOffNameListByShopOrderAndMergeItemList(attachmentDetailsRequest);
//            }
//            throw new EmptyListException();
//        }
}
