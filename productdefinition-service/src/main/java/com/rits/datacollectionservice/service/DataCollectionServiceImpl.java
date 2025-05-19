package com.rits.datacollectionservice.service;

import com.rits.datacollectionservice.dto.*;
import com.rits.datacollectionservice.exception.DataCollectionException;
import com.rits.datacollectionservice.model.*;
import com.rits.datacollectionservice.repository.DataCollectionRepository;
import com.rits.kafkaservice.ProducerEvent;
import com.rits.operationservice.dto.AuditLogRequest;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataCollectionServiceImpl implements DataCollectionService {


    private final DataCollectionRepository dataCollectionRepository;
    private final WebClient.Builder webClientBuilder;
    private final MongoTemplate mongoTemplate;
    private final ApplicationEventPublisher eventPublisher;
    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;
    @Value("${auditlog-service.url}/producer")
    private String auditlogUrl;
    @Value("${dccollect-service.url}/isExists")
    private String dcCollectIsExist;
    private final MessageSource localMessageSource;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }

    public static Map<String, String> createFields(Attachment attachment) {
        Map<String, String> attachmentList = new LinkedHashMap<>();
        attachmentList.put("itemGroup", attachment.getItemGroup());
        attachmentList.put("item", attachment.getItem());
        attachmentList.put("routing", attachment.getRouting());
        attachmentList.put("pcu", attachment.getPcu());
        attachmentList.put("operation", attachment.getOperation());
        attachmentList.put("workCenter", attachment.getWorkCenter());
        attachmentList.put("resource", attachment.getResource());
        attachmentList.put("shopOrder", attachment.getShopOrder());
        return attachmentList;
    }

    public static Map<String, String> createBatchRecipeFields(Attachment attachment) {
        Map<String, String> attachmentList = new LinkedHashMap<>();

        if (attachment.getItem() != null) {
            attachmentList.put("item", attachment.getItem());
        }
        if (attachment.getItemVersion() != null) {
            attachmentList.put("itemVersion", attachment.getItemVersion());
        }
        if (attachment.getOperationId() != null) {
            attachmentList.put("operation", attachment.getOperationId());
        }
        if (attachment.getPhaseId() != null) {
            attachmentList.put("phase", attachment.getPhaseId());
        }
        if (attachment.getBatchNo() != null) {
            attachmentList.put("batchNo", attachment.getBatchNo());
        }
        if (attachment.getOrderNo() != null) {
            attachmentList.put("orderNo", attachment.getOrderNo());
        }

        return attachmentList;
    }

    @Override
    public DataCollectionMessageModel createDataCollection(DataCollectionRequest dataCollectionRequest) throws Exception {
        if (dataCollectionRepository.existsByActiveAndSiteAndDataCollectionAndVersion(1, dataCollectionRequest.getSite(), dataCollectionRequest.getDataCollection(), dataCollectionRequest.getVersion())) {
            return updateDataCollection(dataCollectionRequest);
        }
        List<DataCollection> dataCollections = dataCollectionRepository.findByActiveAndSiteAndDataCollection(1, dataCollectionRequest.getSite(), dataCollectionRequest.getDataCollection());

        dataCollections.stream()
                .filter(DataCollection::isCurrentVersion)
                .map(dataCollectionList -> {
                    dataCollectionList.setModifiedBy(dataCollectionRequest.getUserId());
                    dataCollectionList.setCurrentVersion(false);
                    dataCollectionList.setModifiedDateTime(LocalDateTime.now());
                    return dataCollectionList;
                })
                .forEach(dataCollectionRepository::save);
        if (dataCollectionRequest.getDescription() == null || dataCollectionRequest.getDescription().isEmpty()) {
            dataCollectionRequest.setDescription(dataCollectionRequest.getDataCollection());
        }
        List<String> combinations= new ArrayList<>();
        if(dataCollectionRequest.getAttachmentList() != null && !dataCollectionRequest.getAttachmentList().isEmpty())
        {
           combinations = getAttachment( dataCollectionRequest.getAttachmentList());
        }

        dataCollectionRequest.setAttachmentList(formatAttachmentList(dataCollectionRequest.getAttachmentList()));

        DataCollection dataCollection = createBuilder(dataCollectionRequest,dataCollectionRequest.getAttachmentList(),combinations);
        String createdMessage = getFormattedMessage(1,dataCollectionRequest.getDataCollection(),dataCollectionRequest.getVersion());



        return DataCollectionMessageModel.builder().message_details(new MessageDetails(createdMessage, "S")).response(dataCollectionRepository.save(dataCollection)).build();

    }

    private DataCollection createBuilder(DataCollectionRequest dataCollectionRequest, List<Attachment> createAttachment, List<String> combinations) {
        return DataCollection.builder()
                .handle("DataCollectionBo: " + dataCollectionRequest.getSite() + "," + dataCollectionRequest.getDataCollection() + "," + dataCollectionRequest.getVersion())
                .site(dataCollectionRequest.getSite())
                .dataCollection(dataCollectionRequest.getDataCollection())
                .version(dataCollectionRequest.getVersion())
                .description(dataCollectionRequest.getDescription())
                .status(dataCollectionRequest.getStatus())
                .currentVersion(dataCollectionRequest.isCurrentVersion())
                .collectionType(dataCollectionRequest.getCollectionType())
                .collectDataAt(dataCollectionRequest.getCollectDataAt())
                .collectionMethod(dataCollectionRequest.getCollectionMethod())
                .erpGroup(dataCollectionRequest.isErpGroup())
                .dataCollected(true)
                .showReport(false)
                .qmInspectionGroup(dataCollectionRequest.isQmInspectionGroup())
                .passOrFailGroup(dataCollectionRequest.isPassOrFailGroup())
                .failOrRejectNumber(dataCollectionRequest.getFailOrRejectNumber())
                .userAuthenticationRequired(dataCollectionRequest.isUserAuthenticationRequired())
                .certification(dataCollectionRequest.getCertification())
                .frequency(dataCollectionRequest.getFrequency())
                .tags(combinations)
                .parameterList(dataCollectionRequest.getParameterList())
                .attachmentList(createAttachment)
                .customDataList(dataCollectionRequest.getCustomDataList())
                .active(1)
                .createdBy(dataCollectionRequest.getUserId())
                .createdDateTime(LocalDateTime.now())
                .build();
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
            /*if(StringUtils.isNotBlank(attachment.getOperation()) && StringUtils.isNotBlank(attachment.getOperationVersion()))
            {
                listToBeConcatenated.add("operation_"+attachment.getOperation()+"/"+attachment.getOperationVersion());
            }*/
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
            if(StringUtils.isNotBlank(attachment.getBatchNo()))
            {
                listToBeConcatenated.add("batchNo_"+attachment.getBatchNo());
            }
            if(StringUtils.isNotBlank(attachment.getOrderNo()))
            {
                listToBeConcatenated.add("orderNo_"+attachment.getOrderNo());
            }
            if(StringUtils.isNotBlank(attachment.getOperationId()))
            {
                listToBeConcatenated.add("operationId_"+attachment.getBatchNo());
            }
            if(StringUtils.isNotBlank(attachment.getPhaseId()))
            {
                listToBeConcatenated.add("phaseId_"+attachment.getPhaseId());
            }
            String concatenatedAttachment = String.join("_",listToBeConcatenated);
            listOfAttachments.add(concatenatedAttachment);
        }
        return listOfAttachments;
    }

    private List<String> getCombinations(List<Attachment> attachmentList) {
        List<AttachmentPoint> attachmentPoints = new ArrayList<>();
        for (Attachment attachments : attachmentList) {
            AttachmentPoint attachmentPoint = AttachmentPoint.builder().attachmentList(createFields(attachments)).build();
            attachmentPoints.add(attachmentPoint);
        }
        List<String> selectedFields = attachmentPoints.stream()
                .flatMap(attachmentPoint -> attachmentPoint.getAttachmentList().entrySet().stream()
                        .filter(entry -> !entry.getValue().isEmpty())
                        .map(entry -> entry.getKey() + "_" + entry.getValue()))
                .collect(Collectors.toList());
        List<String> combinations = generateCombinations(selectedFields);
        return combinations;
    }

    private List<Attachment> formatAttachmentList(List<Attachment> attachmentList) {

        for (Attachment attachment : attachmentList) {
            attachment.setSequence(attachment.getSequence() == null ? "" : attachment.getSequence());
            attachment.setItemGroup(attachment.getItemGroup() == null ? "" : attachment.getItemGroup());
            attachment.setItem(attachment.getItem() == null ? "" : attachment.getItem());
            attachment.setItemVersion(attachment.getItemVersion() == null ? "" : attachment.getItemVersion());
            attachment.setRouting(attachment.getRouting() == null ? "" : attachment.getRouting());
            attachment.setRoutingVersion(attachment.getRoutingVersion() == null ? "" : attachment.getRoutingVersion());
            attachment.setOperation(attachment.getOperation() == null ? "" : attachment.getOperation());
            attachment.setOperationVersion(attachment.getOperationVersion() == null ? "" : attachment.getOperationVersion());
            attachment.setWorkCenter(attachment.getWorkCenter() == null ? "" : attachment.getWorkCenter());
            attachment.setResource(attachment.getResource() == null ? "" : attachment.getResource());
            attachment.setShopOrder(attachment.getShopOrder() == null ? "" : attachment.getShopOrder());
            attachment.setPcu(attachment.getPcu() == null ? "" : attachment.getPcu());
            attachment.setBatchNo(attachment.getBatchNo() == null ? "" : attachment.getBatchNo());
            attachment.setOrderNo(attachment.getOrderNo() == null ? "" : attachment.getOrderNo());
//            if (!attachment.getItem().isEmpty()) {
//                if (!attachment.getItemVersion().isEmpty()) {
//                    attachment.setItem(attachment.getItem() + "/" + attachment.getItemVersion());
//                }
//            }
//            if (!attachment.getRouting().isEmpty()) {
//                if (!attachment.getRoutingVersion().isEmpty()) {
//                    attachment.setRouting(attachment.getRouting() + "/" + attachment.getRoutingVersion());
//                }
//            }
//            if (!attachment.getOperation().isEmpty()) {
//                if (!attachment.getOperationVersion().isEmpty()) {
//                    attachment.setOperation(attachment.getOperation() + "/" + attachment.getOperationVersion());
//                }
//            }
        }
        return attachmentList;
    }

    private List<Attachment> createAttachment(List<Attachment> attachmentList) {
        List<Attachment> createAttachment = new ArrayList<>();
        for (Attachment attachments : attachmentList) {
            Attachment newAttachment = Attachment.builder()
                    .sequence(attachments.getSequence())
                    .item(attachments.getItem())
                    .itemGroup(attachments.getItemGroup())
                    .itemVersion(attachments.getItemVersion())
                    .routing(attachments.getRouting())
                    .routingVersion(attachments.getRoutingVersion())
                    .operation(attachments.getOperation())
                    .workCenter(attachments.getWorkCenter())
                    .resource(attachments.getResource())
                    .shopOrder(attachments.getShopOrder())
                    .pcu(attachments.getPcu())
                    .build();
            createAttachment.add(newAttachment);
        }
        return createAttachment;
    }

    @Override
    public DataCollectionMessageModel updateDataCollection(DataCollectionRequest dataCollectionRequest) throws Exception {
        if (dataCollectionRepository.existsByActiveAndSiteAndDataCollectionAndVersion(1, dataCollectionRequest.getSite(), dataCollectionRequest.getDataCollection(), dataCollectionRequest.getVersion())) {

            DataCollection dataCollection = dataCollectionRepository.findByActiveAndSiteAndDataCollectionAndVersion(1, dataCollectionRequest.getSite(), dataCollectionRequest.getDataCollection(), dataCollectionRequest.getVersion());


            if (dataCollectionRequest.getDescription() == null || dataCollectionRequest.getDescription().isEmpty()) {
                dataCollectionRequest.setDescription(dataCollectionRequest.getDataCollection());
            }
            if (dataCollectionRequest.isCurrentVersion()) {
                List<DataCollection> dataCollections = dataCollectionRepository.findByActiveAndSiteAndDataCollection(1, dataCollectionRequest.getSite(), dataCollectionRequest.getDataCollection());


                dataCollections.stream()
                        .filter(DataCollection::isCurrentVersion)
                        .map(dataCollectionList -> {
                            dataCollectionList.setModifiedBy(dataCollectionRequest.getUserId());
                            dataCollectionList.setCurrentVersion(false);
                            dataCollectionList.setModifiedDateTime(LocalDateTime.now());
                            return dataCollectionList;
                        })
                        .forEach(dataCollectionRepository::save);
            }
            List<String> combinations= new ArrayList<>();
            if(dataCollectionRequest.getAttachmentList() != null && !dataCollectionRequest.getAttachmentList().isEmpty())
            {
                combinations = getAttachment( dataCollectionRequest.getAttachmentList());
            }
            DataCollection updatedDataCollection = updateBuilder(dataCollection,dataCollectionRequest,dataCollectionRequest.getAttachmentList(),combinations);
            String updatedMessage = getFormattedMessage(2, dataCollectionRequest.getDataCollection(),dataCollectionRequest.getVersion());



            return DataCollectionMessageModel.builder().message_details(new MessageDetails(updatedMessage, "S")).response(dataCollectionRepository.save(updatedDataCollection)).build();
        } else {

            return createDataCollection(dataCollectionRequest);
        }

    }

    private DataCollection updateBuilder(DataCollection dataCollection, DataCollectionRequest dataCollectionRequest, List<Attachment> updateAttachment, List<String> combinations) {
        return DataCollection.builder()
                .handle(dataCollection.getHandle())
                .site(dataCollection.getSite())
                .dataCollection(dataCollection.getDataCollection())
                .version(dataCollectionRequest.getVersion())
                .description(dataCollectionRequest.getDescription())
                .status(dataCollectionRequest.getStatus())
                .currentVersion(dataCollectionRequest.isCurrentVersion())
                .collectionType(dataCollectionRequest.getCollectionType())
                .collectDataAt(dataCollectionRequest.getCollectDataAt())
                .collectionMethod(dataCollectionRequest.getCollectionMethod())
                .erpGroup(dataCollectionRequest.isErpGroup())
                .qmInspectionGroup(dataCollectionRequest.isQmInspectionGroup())
                .passOrFailGroup(dataCollectionRequest.isPassOrFailGroup())
                .failOrRejectNumber(dataCollectionRequest.getFailOrRejectNumber())
                .userAuthenticationRequired(dataCollectionRequest.isUserAuthenticationRequired())
                .certification(dataCollectionRequest.getCertification())
                .frequency(dataCollectionRequest.getFrequency())
                .tags(combinations)
                .dataCollected(true)
                .showReport(false)
                .dataCollected(dataCollectionRequest.isDataCollected())
                .showReport(dataCollectionRequest.isShowReport())
                .parameterList(dataCollectionRequest.getParameterList())
                .attachmentList(updateAttachment)
                .customDataList(dataCollectionRequest.getCustomDataList())
                .active(1)
                .createdDateTime(dataCollection.getCreatedDateTime())
                .modifiedBy(dataCollectionRequest.getUserId())
                .modifiedDateTime(LocalDateTime.now())
                .build();
    }

    @Override
    public DataCollection retrieveDataCollection(String dataCollection, String version, String site) throws Exception {
        DataCollection existingDataCollection;
        if (version != null && !version.isEmpty()) {
            existingDataCollection = dataCollectionRepository.findByActiveAndSiteAndDataCollectionAndVersion(1, site, dataCollection, version);
            if (existingDataCollection == null) {
                throw new DataCollectionException(302, dataCollection, version);
            }
        } else {
            existingDataCollection = dataCollectionRepository.findByActiveAndSiteAndDataCollectionAndCurrentVersion(1, site, dataCollection, true);
            if (existingDataCollection == null) {
                throw new DataCollectionException(302, dataCollection, version);
            }
        }
        return existingDataCollection;
    }

    @Override
    public DataCollectionResponseList getDataCollectionList(String dataCollection, String site) throws Exception {
        if (dataCollection != null && !dataCollection.isEmpty()) {
            List<DataCollectionResponse> dataCollections = dataCollectionRepository.findByActiveAndSiteAndDataCollectionContainingIgnoreCase(1, site, dataCollection);
            if (dataCollections.isEmpty()) {
                throw new DataCollectionException(302, dataCollection);
            }
            DataCollectionResponseList dataCollectionResponseList = DataCollectionResponseList.builder().dataCollectionList(dataCollections).build();
            return dataCollectionResponseList;
        }
        return getDataCollectionListByCreationDate(site);
    }

    @Override
    public DataCollectionResponseList getDataCollectionListByCreationDate(String site) throws Exception {
        List<DataCollectionResponse> dataCollectionResponses = dataCollectionRepository.findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(1, site);
        DataCollectionResponseList dataCollectionResponseList = DataCollectionResponseList.builder().dataCollectionList(dataCollectionResponses).build();
        return dataCollectionResponseList;
    }

    @Override
    public DataCollectionMessageModel deleteDataCollection(String dataCollection, String version, String site, String userId) throws Exception {
        if (dataCollectionRepository.existsByActiveAndSiteAndDataCollectionAndVersion(1, site, dataCollection, version)) {
            DataCollection existingDataCollection = dataCollectionRepository.findByActiveAndSiteAndDataCollectionAndVersion(1, site, dataCollection, version);
            existingDataCollection.setActive(0);
            existingDataCollection.setModifiedBy(userId);
            existingDataCollection.setModifiedDateTime(LocalDateTime.now());
            dataCollectionRepository.save(existingDataCollection);
            String deletedMessage = getFormattedMessage(3, dataCollection,version);

            return DataCollectionMessageModel.builder().message_details(new MessageDetails(deletedMessage, "S")).build();
        }
        throw new DataCollectionException(302, dataCollection, version);
    }

    @Override
    public List<String> getDcGroupNameListByItem(String item, String site) throws Exception {
        List<DataCollection> dataCollections = dataCollectionRepository.findByActiveAndSiteAndAttachmentList_Item(1, site, item);
        if (dataCollections.isEmpty()) {
            throw new DataCollectionException(303, item);
        }
        List<String> dcGroupNameList = dataCollections.stream()
                .map(DataCollection::getDataCollection)
                .collect(Collectors.toList());
        return dcGroupNameList;
    }

    @Override
    public List<String> getDcGroupNameListByPCU(String pcu, String item, String site) throws Exception {
        List<DataCollection> dataCollections = dataCollectionRepository.findByActiveAndSiteAndAttachmentList_Pcu(1, site, pcu);
        if (dataCollections.isEmpty()) {
            throw new DataCollectionException(304, pcu);
        }
        List<String> dcGroupNameList = dataCollections.stream()
                .map(DataCollection::getDataCollection)
                .collect(Collectors.toList());
        List<String> itemList = getDcGroupNameListByItem(item, site);
        dcGroupNameList = dcGroupNameList.stream()
                .filter(itemList::contains)
                .collect(Collectors.toList());
        return dcGroupNameList;
    }

    @Override
    public List<String> getDcGroupNameListByItemOperation(String item, String operation, String site) throws Exception {
        List<DataCollection> dataCollections = dataCollectionRepository.findByActiveAndSiteAndAttachmentList_ItemAndAttachmentList_Operation(1, site, item, operation);
        if (dataCollections.isEmpty()) {
            throw new DataCollectionException(305, item, operation);
        }
        List<String> dcGroupNameLists = dataCollections.stream()
                .map(DataCollection::getDataCollection)
                .collect(Collectors.toList());
        return dcGroupNameLists;
    }

    @Override
    public List<String> getDcGroupNameListByPcuOperation(String pcu, String operation, String site) throws Exception {
        List<DataCollection> dataCollections = dataCollectionRepository.findByActiveAndSiteAndAttachmentList_PcuAndAttachmentList_Operation(1, site, pcu, operation);
        if (dataCollections.isEmpty()) {
            throw new DataCollectionException(306, pcu, operation);
        }
        List<String> dcGroupNameLists = dataCollections.stream()
                .map(DataCollection::getDataCollection)
                .collect(Collectors.toList());
        return dcGroupNameLists;
    }

    @Override
    public List<String> getDcGroupNameListByItemOperationRouting(String item, String operation, String routing, String site) throws Exception {
        List<DataCollection> dataCollections = dataCollectionRepository.findByActiveAndSiteAndAttachmentList_ItemAndAttachmentList_OperationAndAttachmentList_Routing(1, site, item, operation, routing);
        if (dataCollections.isEmpty()) {
            throw new DataCollectionException(307, item, operation, routing);
        }
        List<String> dcGroupNameLists = dataCollections.stream()
                .map(DataCollection::getDataCollection)
                .collect(Collectors.toList());
        return dcGroupNameLists;
    }

    @Override
    public List<String> getDcGroupNameListByOperationRoutingPcu(String operation, String routing, String pcu, String site, String item) throws Exception {
        List<DataCollection> dataCollections = dataCollectionRepository.findByActiveAndSiteAndAttachmentList_OperationAndAttachmentList_RoutingAndAttachmentList_Pcu(1, site, operation, routing, pcu);
        if (dataCollections.isEmpty()) {
            throw new DataCollectionException(308, operation, routing, pcu);
        }
        List<String> dcGroupNameLists = dataCollections.stream()
                .map(DataCollection::getDataCollection)
                .collect(Collectors.toList());
        List<String> itemList = getDcGroupNameListByItem(item, site);
        dcGroupNameLists = dcGroupNameLists.stream()
                .filter(itemList::contains)
                .collect(Collectors.toList());
        return dcGroupNameLists;
    }

    @Override
    public List<String> getDcGroupNameListByResource(String resource, String site) throws Exception {

        List<DataCollection> dataCollections = dataCollectionRepository.findByActiveAndSiteAndAttachmentList_Resource(1, site, resource);
        if (dataCollections.isEmpty()) {
            throw new DataCollectionException(309, resource);
        }
        List<String> dcGroupNameList = dataCollections.stream()
                .map(DataCollection::getDataCollection)
                .collect(Collectors.toList());
        return dcGroupNameList;
    }


    @Override
    public List<String> getDcGroupNameListByResourcePcu(String resource, String pcu, String site) throws Exception {
        List<DataCollection> dataCollections = dataCollectionRepository.findByActiveAndSiteAndAttachmentList_ResourceAndAttachmentList_Pcu(1, site, resource, pcu);
        if (dataCollections.isEmpty()) {
            throw new DataCollectionException(310, resource, pcu);
        }
        List<String> dcGroupNameList = dataCollections.stream()
                .map(DataCollection::getDataCollection)
                .collect(Collectors.toList());
        return dcGroupNameList;
    }

    @Override
    public List<String> getDcGroupNameListByWorkCenter(String workCenter, String site, String resource) throws Exception {
        List<DataCollection> dataCollections = dataCollectionRepository.findByActiveAndSiteAndAttachmentList_workCenter(1, site, workCenter);
        if (dataCollections.isEmpty()) {
            throw new DataCollectionException(311, workCenter, resource);
        }
        List<String> dcGroupNameLists = dataCollections.stream()
                .map(DataCollection::getDataCollection)
                .collect(Collectors.toList());
        List<String> itemList = getDcGroupNameListByResource(resource, site);
        dcGroupNameLists = dcGroupNameLists.stream()
                .filter(itemList::contains)
                .collect(Collectors.toList());
        return dcGroupNameLists;
    }

    @Override
    public List<String> getDcGroupNameListByShopOrder(String shopOrder, String site, String item) throws Exception {

        List<DataCollection> dataCollections = dataCollectionRepository.findByActiveAndSiteAndAttachmentList_shopOrder(1, site, shopOrder);
        if (dataCollections.isEmpty()) {
            throw new DataCollectionException(312, shopOrder, item);
        }
        List<String> dcGroupNameLists = dataCollections.stream()
                .map(DataCollection::getDataCollection)
                .collect(Collectors.toList());
        List<String> itemList = getDcGroupNameListByItem(item, site);
        dcGroupNameLists = dcGroupNameLists.stream()
                .filter(itemList::contains)
                .collect(Collectors.toList());
        return dcGroupNameLists;

    }

    @Override
    public List<String> getDcGroupNameListByShopOrderOperation(String shopOrder, String operation, String site) throws Exception {
        List<DataCollection> dataCollections = dataCollectionRepository.findByActiveAndSiteAndAttachmentList_ShopOrderAndAttachmentList_Operation(1, site, shopOrder, operation);
        if (dataCollections.isEmpty()) {
            throw new DataCollectionException(313, shopOrder, operation);
        }
        List<String> dcGroupNameList = dataCollections.stream()
                .map(DataCollection::getDataCollection)
                .collect(Collectors.toList());
        return dcGroupNameList;
    }

    @Override
    public DataCollectionList findByOperationPcuAndResource(String site, String operation, String resource, String pcu) throws Exception {
        List<DataCollection> dataCollectionList = new ArrayList<>();
        if (operation != null && !operation.isEmpty() && resource != null && !resource.isEmpty() && pcu != null && !pcu.isEmpty()) {
            List<DataCollection> retrievedData = dataCollectionRepository.findByActiveAndSiteAndAttachmentList_OperationAndAttachmentList_ResourceAndAttachmentList_Pcu(1, site, operation, resource, pcu);
            for (DataCollection data : retrievedData) {
                dataCollectionList.add(data);
            }
        }
        if (operation != null && !operation.isEmpty() && resource != null && !resource.isEmpty()) {
            List<DataCollection> retrievedData = dataCollectionRepository.findByActiveAndSiteAndAttachmentList_OperationAndAttachmentList_Resource(1, site, operation, resource);
            for (DataCollection data : retrievedData) {
                dataCollectionList.add(data);
            }
        }
        if (operation != null && !operation.isEmpty() && pcu != null && !pcu.isEmpty()) {
            List<DataCollection> retrievedData = dataCollectionRepository.findByActiveAndSiteAndAttachmentList_OperationAndAttachmentList_Pcu(1, site, operation, pcu);
            for (DataCollection data : retrievedData) {
                dataCollectionList.add(data);
            }
        }
        if (resource != null && !resource.isEmpty() && pcu != null && !pcu.isEmpty()) {
            List<DataCollection> retrievedData = dataCollectionRepository.findByActiveAndSiteAndAttachmentList_ResourceAndAttachmentList_Pcu(1, site, resource, pcu);
            for (DataCollection data : retrievedData) {
                dataCollectionList.add(data);
            }
        }
        if (pcu != null && !pcu.isEmpty()) {
            List<DataCollection> retrievedData = dataCollectionRepository.findByActiveAndSiteAndAttachmentList_Pcu(1, site, pcu);
            for (DataCollection data : retrievedData) {
                dataCollectionList.add(data);
            }
        }
        if (resource != null && !resource.isEmpty()) {
            List<DataCollection> retrievedData = dataCollectionRepository.findByActiveAndSiteAndAttachmentList_Resource(1, site, resource);
            for (DataCollection data : retrievedData) {
                dataCollectionList.add(data);
            }
        }
        if (operation != null && !operation.isEmpty()) {
            List<DataCollection> retrievedData = dataCollectionRepository.findByActiveAndSiteAndAttachmentList_Operation(1, site, operation);
            for (DataCollection data : retrievedData) {
                dataCollectionList.add(data);
            }
        }
        DataCollectionList dataCollectionListt = DataCollectionList.builder().dataCollectionList(dataCollectionList).build();
        return dataCollectionListt;
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
            throw new DataCollectionException(800);
        }
        return extensionResponse;
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
    public DataCollectionList retrieveByAttachment(List<Attachment> attachmentList, String pcu, String operation, String resource,String site) throws Exception {
        List<Attachment> attachmentPointsmodified = new ArrayList<>();
        for (Attachment attachment : attachmentList) {
            attachment.setSequence(attachment.getSequence() == null ? "" : attachment.getSequence());
            attachment.setItemGroup(attachment.getItemGroup() == null ? "" : attachment.getItemGroup());
//            attachment.setItem(attachment.getItem() == null ? "" : attachment.getItem());
//            attachment.setItemVersion(attachment.getItemVersion() == null ? "" : attachment.getItemVersion());
//            attachment.setRouting(attachment.getRouting() == null ? "" : attachment.getRouting());
//            attachment.setRoutingVersion(attachment.getRoutingVersion() == null ? "" : attachment.getRoutingVersion());
//            attachment.setOperation(attachment.getOperation() == null ? "" : attachment.getOperation());
//            attachment.setOperationVersion(attachment.getOperationVersion() == null ? "" : attachment.getOperationVersion());
            attachment.setWorkCenter(attachment.getWorkCenter() == null ? "" : attachment.getWorkCenter());
            attachment.setResource(attachment.getResource() == null ? "" : attachment.getResource());
            attachment.setShopOrder(attachment.getShopOrder() == null ? "" : attachment.getShopOrder());
            attachment.setPcu(attachment.getPcu() == null ? "" : attachment.getPcu());
            attachment.setBatchNo(attachment.getBatchNo() == null ? "" : attachment.getBatchNo());
            attachment.setOrderNo(attachment.getOrderNo() == null ? "" : attachment.getOrderNo());
            if (attachment.getItem() != null && !attachment.getItem().isEmpty()) {
                if (attachment.getItemVersion()!= null && !attachment.getItemVersion().isEmpty()) {
                    attachment.setItem(attachment.getItem() + "/" + attachment.getItemVersion());
                }
            }
            if (attachment.getRouting()!= null && !attachment.getRouting().isEmpty()) {
                if (attachment.getRoutingVersion()!= null && !attachment.getRoutingVersion().isEmpty()) {
                    attachment.setRouting(attachment.getRouting() + "/" + attachment.getRoutingVersion());
                }
            }
            if (attachment.getOperation()!= null && !attachment.getOperation().isEmpty()) {
                if (attachment.getOperationVersion()!= null && !attachment.getOperationVersion().isEmpty()) {
                    attachment.setOperation(attachment.getOperation() + "/" + attachment.getOperationVersion());
                }
            }
            attachmentPointsmodified.add(attachment);
        }
        List<AttachmentPoint> attachmentPoints = new ArrayList<>();
        for (Attachment attachments : attachmentPointsmodified) {
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
        query.addCriteria(
                Criteria.where("tags").in(combinations)
                        .and("active").is(1)
                        .and("site").is(site)
                        .and("currentVersion").is(true)
        );

        List<DataCollection> dataCollectionListRecord = mongoTemplate.find(query, DataCollection.class);
        if(StringUtils.isNotBlank(pcu)) {
            for (DataCollection dataCollectionList : dataCollectionListRecord) {

                DcCollectRequest dcCollectRequest = DcCollectRequest.builder().site(dataCollectionList.getSite()).dataCollection(dataCollectionList.getDataCollection()).version(dataCollectionList.getVersion()).pcu(pcu).operation(operation).resource(resource).build();
                Boolean dataCollection = webClientBuilder.build()
                        .post()
                        .uri(dcCollectIsExist)
                        .bodyValue(dcCollectRequest)
                        .retrieve()
                        .bodyToMono(Boolean.class)
                        .block();
                if (dataCollection && dataCollectionList.getCollectionMethod().equalsIgnoreCase("single")) {
                    dataCollectionList.setDataCollected(false);
                    dataCollectionList.setShowReport(true);
                } else if (dataCollection && dataCollectionList.getCollectionMethod().equalsIgnoreCase("multiple")) {
                    dataCollectionList.setDataCollected(true);
                    dataCollectionList.setShowReport(true);
                } else {
                    dataCollectionList.setDataCollected(true);
                    dataCollectionList.setShowReport(false);
                }
            }
        }
        DataCollectionList dataCollectionList = DataCollectionList.builder().dataCollectionList(dataCollectionListRecord).build();
        return dataCollectionList;
    }

    public DataCollectionList retrieveDCForBatchRecipe(RetrieveRequest retrieveRequest){
//        for(Map.Entry<String,String> recipeDc : retrieveRequest.getRecipeDc().entrySet()){
//
//        }
        Attachment attachment = new Attachment();
        attachment.setBatchNo(retrieveRequest.getBatchNo() == null ? "" : retrieveRequest.getBatchNo());
        attachment.setOrderNo(retrieveRequest.getOrderNo() == null ? "" : retrieveRequest.getOrderNo());
        attachment.setItem(retrieveRequest.getMaterial() == null ? "" : retrieveRequest.getMaterial());
        attachment.setItemVersion(retrieveRequest.getMaterialVersion() == null ? "" : retrieveRequest.getMaterialVersion());
        attachment.setOperationId(retrieveRequest.getOperationId() == null ? "" : retrieveRequest.getOperationId());
        attachment.setPhaseId(retrieveRequest.getPhaseId() == null ? "" : retrieveRequest.getPhaseId());
        List<AttachmentPoint> attachmentPoints = new ArrayList<>();
        AttachmentPoint attachmentPoint = AttachmentPoint.builder().attachmentList(createBatchRecipeFields(attachment)).build();
        attachmentPoints.add(attachmentPoint);
        List<String> selectedFields = attachmentPoints.stream()
                .flatMap(combination -> combination.getAttachmentList().entrySet().stream()
                        .filter(entry -> !entry.getValue().isEmpty())
                        .map(entry -> entry.getKey() + "_" + entry.getValue()))
                .collect(Collectors.toList());

        List<String> combinations = generateCombinations(selectedFields);
        combinations.remove(0);

        Query query = new Query();
        query.addCriteria(
                Criteria.where("tags").in(combinations)
                        .and("active").is(1)
                        .and("site").is(retrieveRequest.getSite())
                        .and("currentVersion").is(true)
        );

        List<DataCollection> dataCollectionListRecord = mongoTemplate.find(query, DataCollection.class);
        DataCollectionList dataCollectionList = DataCollectionList.builder().dataCollectionList(dataCollectionListRecord).build();
        return dataCollectionList;

    }
    @Override
    public List<Parameter> retrieveAllParameterNames(String site) throws Exception {
        List<DataCollection> retrievedDataCollections = dataCollectionRepository.findByActiveAndSite(1, site);
        List<Parameter> allParametersName = new ArrayList<>();

        if (!retrievedDataCollections.isEmpty() && retrievedDataCollections != null) {
            for (DataCollection dataCollection : retrievedDataCollections) {
                for (Parameter parameter : dataCollection.getParameterList()) {
                    boolean isExist = allParametersName.stream().anyMatch(obj -> obj.getParameterName().equals(parameter.getParameterName()));
                    if (!isExist) {
                        Parameter newParameter = Parameter.builder().parameterName(parameter.getParameterName()).build();
                        allParametersName.add(newParameter);
                    }
                }
            }
        }
        return allParametersName;
    }


    public AuditLogRequest createAUditLog(DataCollectionRequest dataCollectionRequest) {
        return AuditLogRequest.builder()
                .site(dataCollectionRequest.getSite())
                .action_code("DATACOLLECTION-CREATE")
                .action_detail("DataCollection Created " + dataCollectionRequest.getDataCollection() + "/" + dataCollectionRequest.getVersion())
                .action_detail_handle("ActionDetailBO:" + dataCollectionRequest.getSite() + "," + "DATACOLLECTION-CREATE" + "," + dataCollectionRequest.getUserId() + ":" + "com.rits.datacollectionservice.service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(dataCollectionRequest.getUserId())
                .txnId("DATACOLLECTION-CREATE" + LocalDateTime.now() + dataCollectionRequest.getUserId())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("Create")
                .topic("audit-log")
                .build();

    }

    public AuditLogRequest updateAuditLog(DataCollectionRequest dataCollectionRequest) {
        return AuditLogRequest.builder()
                .site(dataCollectionRequest.getSite())
                .action_code("DATACOLLECTION-UPDATE")
                .action_detail("DataCollection Updated " + dataCollectionRequest.getDataCollection() + "/" + dataCollectionRequest.getVersion())
                .action_detail_handle("ActionDetailBO:" + dataCollectionRequest.getSite() + "," + "DATACOLLECTION-UPDATE" + "," + dataCollectionRequest.getUserId() + ":" + "com.rits.datacollectionservice.service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(dataCollectionRequest.getUserId())
                .txnId("DATACOLLECTION-UPDATE" + LocalDateTime.now() + dataCollectionRequest.getUserId())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("Update")
                .topic("audit-log")
                .build();
    }

    public AuditLogRequest deleteAuditLog(DataCollectionRequest dataCollectionRequest) {
        return AuditLogRequest.builder()
                .site(dataCollectionRequest.getSite())
                .change_stamp("Delete")
                .action_code("DATACOLLECTION-DELETE")
                .action_detail("DataCollection Deleted " + dataCollectionRequest.getDataCollection() + "/" + dataCollectionRequest.getVersion())
                .action_detail_handle("ActionDetailBO:" + dataCollectionRequest.getSite() + "," + "DATACOLLECTION-DELETE" + "," + dataCollectionRequest.getUserId() + ":" + "com.rits.datacollectionservice.service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(dataCollectionRequest.getUserId())
                .txnId("DATACOLLECTION-DELETE" + LocalDateTime.now() + dataCollectionRequest.getUserId())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("delete")
                .topic("audit-log")
                .build();
    }
}