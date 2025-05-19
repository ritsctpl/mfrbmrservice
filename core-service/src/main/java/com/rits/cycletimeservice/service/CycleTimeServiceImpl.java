package com.rits.cycletimeservice.service;


import com.rits.cycletimeservice.dto.*;
import com.rits.cycletimeservice.exception.CycleTimeException;
import com.rits.cycletimeservice.model.*;
import com.rits.cycletimeservice.repository.AttachmentPriorityRepository;
import com.rits.cycletimeservice.repository.CycleTimePostgresRepository;
import com.rits.cycletimeservice.repository.CycleTimePriorityRepository;
import com.rits.cycletimeservice.repository.CycleTimeRepository;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.asm.Advice;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.core.ParameterizedTypeReference;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CycleTimeServiceImpl implements CycleTimeService {
    private final CycleTimeRepository cycleTimeRepository;
    private final CycleTimePriorityRepository cycleTimePriorityRepository;
    private final CycleTimePostgresRepository cycleTimePostgresRepository;
    private final AttachmentPriorityRepository attachmentPriorityRepository;
    private final WebClient.Builder webClientBuilder;
    private final MessageSource localMessageSource;
    private final MongoTemplate mongoTemplate;
    @Value("${shoporder-service.url}/isExist")
    private String shopOrderUrl;
    @Value("${resource-service.url}/isExist")
    private String resourceUrl;
    @Value("${operation-service.url}/isExist")
    private String operationUrl;
    @Value("${routing-service.url}/isExist")
    private String routingUrl;
    @Value("${item-service.url}/isExist")
    private String itemUrl;
    @Value("${item-service.url}/retrieve")
    private String retrieveItemUrl;
    @Value("${cycleTimePriorities}")
    private String cycleTimePrioritiesFile;
    @Value("${cycleTimePriorities}")
    private String cycleTimePrioritiesJson;

    @Value("${production-service.url}/getUniqueCombinations")
    private String productionlog;

    @Value("${shift-service.url}/availabilityPlannedOperatingTime")
    private String performancePlannedOperatingTimeUrl;

    @Value("${production-service.url}/getActualCycleTimeAndActualQuantity")
    private String actualCycleTime;

    @Value("${resourcetype-service.url}/retrieveByResourceType")
    private String resourceTypeUrl;

    int count;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }
    @Override
    public List<CycleTimePostgres> getCycleTimeRecords(CycleTimeRequest cycleTimeRequest){
        return cycleTimePostgresRepository.findBySiteAndActiveOrderByCreatedDatetimeDesc(cycleTimeRequest.getSite(),  1);
    }

    private String getCycleTimeHandle(CycleTimeRequest cycleTimeRequest){
        StringBuilder handleBuilder = new StringBuilder("CycleTimeBO:")
                .append(cycleTimeRequest.getSite());

        if (StringUtils.isNotEmpty(cycleTimeRequest.getOperation()) && StringUtils.isNotEmpty(cycleTimeRequest.getOperationVersion())) {
            handleBuilder.append("_OperationBO:")
                    .append(cycleTimeRequest.getSite()).append(",")
                    .append(cycleTimeRequest.getOperation()).append(",")
                    .append(cycleTimeRequest.getOperationVersion());
        }

        if (StringUtils.isNotEmpty(cycleTimeRequest.getResource())) {
            handleBuilder.append("_ResourceBO:")
                    .append(cycleTimeRequest.getSite()).append(",")
                    .append(cycleTimeRequest.getResource());
        }

        if (StringUtils.isNotEmpty(cycleTimeRequest.getItem()) && StringUtils.isNotEmpty(cycleTimeRequest.getItemVersion())) {
            handleBuilder.append("_ItemBO:")
                    .append(cycleTimeRequest.getSite()).append(",")
                    .append(cycleTimeRequest.getItem()).append(",")
                    .append(cycleTimeRequest.getItemVersion());
        }
        if (StringUtils.isNotEmpty(cycleTimeRequest.getWorkCenter())) {
            handleBuilder.append("_WorkCenterBO:")
                    .append(cycleTimeRequest.getSite()).append(",")
                    .append(cycleTimeRequest.getWorkCenter());
        }
        return handleBuilder.toString();
    }
    @Override
    public CycleTimeMessageModel createOrUpdate(CycleTimeRequest cycleTimeRequest) throws Exception {

        List<CycleTime> updatedCycleTimes = new ArrayList<>();
        List<CycleTimePostgres> updatedPostgresCycleTimes = new ArrayList<>();

        if(cycleTimeRequest.getResourceType() != null && !cycleTimeRequest.getResourceType().isEmpty()/* && (cycleTimeRequest.getResource() == null || cycleTimeRequest.getResource().isEmpty())*/) {
            ResourceType resourceType = fetchResourceType(cycleTimeRequest);
            if (resourceType == null || CollectionUtils.isEmpty(resourceType.getResourceMemberList())) {
                throw new CycleTimeException(7013, cycleTimeRequest.getResourceType());
            }

            for (ResourceMemberList resource : resourceType.getResourceMemberList()) {
                cycleTimeRequest.setResource(resource.getResource());

                // Check if record exists for this specific resource
                createOrUpdateCycleTimeRecord(cycleTimeRequest, updatedCycleTimes, updatedPostgresCycleTimes);

            }
        }else{
            createOrUpdateCycleTimeRecord(cycleTimeRequest, updatedCycleTimes, updatedPostgresCycleTimes);
        }
        if(cycleTimeRequest.getCycleTimeRequestList() != null && !cycleTimeRequest.getCycleTimeRequestList().isEmpty()) {
            for (CycleTimeRequest cycleTimeRequest1 : cycleTimeRequest.getCycleTimeRequestList()) {

                createOrUpdateCycleTimeRecord(cycleTimeRequest1, updatedCycleTimes, updatedPostgresCycleTimes);
            }
        }

        CycleTimeRequest cycleTimeCreate = cycleTimeResponseBuilder(updatedCycleTimes.get(0));
        String message = (!updatedCycleTimes.isEmpty() && !updatedPostgresCycleTimes.isEmpty()) ? "Updated Successfully" : "Created Successfully";
        return CycleTimeMessageModel.builder()
                .message_details(new MessageDetails(message, "S"))
                .response(cycleTimeCreate)
                .build();
    }

    // Reusable method for saving/updating records
    private void createOrUpdateCycleTimeRecord(CycleTimeRequest cycleTimeRequest,
                                     List<CycleTime> updatedCycleTimes,
                                     List<CycleTimePostgres> updatedPostgresCycleTimes) throws Exception {
        boolean isRecordExistInMongo = isCycleTimeExist(cycleTimeRequest);
        boolean isRecordExistInPost = isCycleTimeExistInPostgres(cycleTimeRequest);

        CycleTime updatedCycleTime = isRecordExistInMongo
                ? updateCycleTimeBuilder(retrieveRec(cycleTimeRequest), cycleTimeRequest)
                : cycleTimeBuilder(cycleTimeRequest);

        CycleTimePostgres updatedPostgresCycleTime = isRecordExistInPost
                ? updateCycleTimePostgresBuilder(retrievePostgre(cycleTimeRequest), cycleTimeRequest)
                : cycleTimePostgresBuilder(cycleTimeRequest);

        saveRecords(updatedCycleTime, updatedPostgresCycleTime);

        updatedCycleTimes.add(updatedCycleTime);
        updatedPostgresCycleTimes.add(updatedPostgresCycleTime);
    }

    private CycleTime updateCycleTimeBuilder(CycleTime existingRecord, CycleTimeRequest cycleTimeRequest) {
        return CycleTime.builder()
                .handle(existingRecord.getHandle())
                .site(existingRecord.getSite())
                .operationId(cycleTimeRequest.getOperation() != null ? cycleTimeRequest.getOperation() : existingRecord.getOperationId())
                .operationVersion(cycleTimeRequest.getOperationVersion() != null ? cycleTimeRequest.getOperationVersion() : existingRecord.getOperationVersion())
                .resourceId(cycleTimeRequest.getResource() != null ? cycleTimeRequest.getResource() : existingRecord.getResourceId())
                .resourceType(cycleTimeRequest.getResourceType() != null ? cycleTimeRequest.getResourceType() : existingRecord.getResourceType())
                .item(cycleTimeRequest.getItem() != null ? cycleTimeRequest.getItem() : existingRecord.getItem())
                .itemVersion(cycleTimeRequest.getItemVersion() != null ? cycleTimeRequest.getItemVersion() : existingRecord.getItemVersion())
//                .material(cycleTimeRequest.getMaterial() != null ? cycleTimeRequest.getMaterial() : existingRecord.getMaterial())
//                .materialVersion(cycleTimeRequest.getMaterialVersion() != null ? cycleTimeRequest.getMaterialVersion() : existingRecord.getMaterialVersion())
                .workCenterId(cycleTimeRequest.getWorkCenter() != null ? cycleTimeRequest.getWorkCenter() : existingRecord.getWorkCenterId())
                .cycleTime(cycleTimeRequest.getCycleTime() != null ? cycleTimeRequest.getCycleTime() : existingRecord.getCycleTime())
                .manufacturedTime(cycleTimeRequest.getManufacturedTime() != null ? cycleTimeRequest.getManufacturedTime() : (existingRecord.getManufacturedTime() != null ? existingRecord.getManufacturedTime() : 0.0))
                .createdBy(existingRecord.getCreatedBy())
                .modifiedBy(cycleTimeRequest.getModifiedBy() != null ? cycleTimeRequest.getModifiedBy() : existingRecord.getModifiedBy())
                .createdDateTime(existingRecord.getCreatedDateTime())
                .modifiedDateTime(LocalDateTime.now())
                .active(existingRecord.getActive())
                .pcu(existingRecord.getPcu())
                .shiftId(existingRecord.getShiftId())
                .time(cycleTimeRequest.getTime())
                .targetQuantity(cycleTimeRequest.getTargetQuantity())
                .userId(cycleTimeRequest.getUserId() != null ? cycleTimeRequest.getUserId() : existingRecord.getUserId())
                .build();
    }


    private CycleTimePostgres updateCycleTimePostgresBuilder(CycleTimePostgres existingRecord, CycleTimeRequest cycleTimeRequest) {
        existingRecord.setOperation(cycleTimeRequest.getOperation());
        existingRecord.setOperationVersion(cycleTimeRequest.getOperationVersion());
        existingRecord.setResourceId(cycleTimeRequest.getResource());
        existingRecord.setResourceType(cycleTimeRequest.getResourceType());
        existingRecord.setItem(cycleTimeRequest.getItem());
        existingRecord.setItemVersion(cycleTimeRequest.getItemVersion());
        existingRecord.setWorkcenterId(cycleTimeRequest.getWorkCenter());
        existingRecord.setPlannedCycleTime(cycleTimeRequest.getCycleTime());
        existingRecord.setManufacturedTime(cycleTimeRequest.getManufacturedTime());
        existingRecord.setModifiedBy(cycleTimeRequest.getUserId());
        existingRecord.setModifiedDatetime(LocalDateTime.now());
        existingRecord.setTargetQuantity(cycleTimeRequest.getTargetQuantity());
        existingRecord.setTime(cycleTimeRequest.getTime());
        return existingRecord;
    }

    private void saveRecords(CycleTime updatedCycleTime, CycleTimePostgres updatedPostgresCycleTime) {
        if (updatedCycleTime == null || updatedPostgresCycleTime == null) {
            throw new CycleTimeException(7015, updatedCycleTime != null ? updatedCycleTime.getResourceId() : null);
        }

        cycleTimeRepository.save(updatedCycleTime);
        cycleTimePostgresRepository.save(updatedPostgresCycleTime);
    }

    private void markRecordAsDeleted(CycleTime record, String modifiedBy) {
        record.setActive(0);
        record.setModifiedBy(modifiedBy);
        record.setModifiedDateTime(LocalDateTime.now());
    }

    private void markPostgresRecordAsDeleted(CycleTimePostgres record, String modifiedBy) {
        record.setActive(0);
        record.setModifiedBy(modifiedBy);
        record.setModifiedDatetime(LocalDateTime.now());
    }

    private void setItemInCycleTimeRequest(CycleTimeRequest cycleTimeRequest) {
        if (StringUtils.isBlank(cycleTimeRequest.getItem()) && StringUtils.isNotBlank(cycleTimeRequest.getMaterial())) {
            cycleTimeRequest.setItem(cycleTimeRequest.getMaterial());
            cycleTimeRequest.setItemVersion(cycleTimeRequest.getMaterialVersion());
        }
    }

    private ResourceType fetchResourceType(CycleTimeRequest request) {
        ResourceTypeRequest resourceTypeRequest = ResourceTypeRequest.builder()
                .site(request.getSite())
                .resourceType(request.getResourceType())
                .build();

        return webClientBuilder.build()
                .post()
                .uri(resourceTypeUrl)
                .bodyValue(resourceTypeRequest)
                .retrieve()
                .bodyToMono(ResourceType.class)
                .block();
    }

    private List<String> fetchValidCycleTimeRecords(CycleTimeRequest cycleTimeRequest, ResourceType resourceType,
                                                    List<CycleTimePostgres> foundRecords) {
        List<String> missingResources = new ArrayList<>();

        for (ResourceMemberList resource : resourceType.getResourceMemberList()) {
            cycleTimeRequest.setResource(resource.getResource());

            String handle = getCycleTimeHandle(cycleTimeRequest);
            CycleTimePostgres postgresRecord = cycleTimePostgresRepository.findBySiteAndActiveAndHandle(
                    cycleTimeRequest.getSite(), 1, handle);

            if (postgresRecord == null) {
                missingResources.add(resource.getResource());
            } else {
                foundRecords.add(postgresRecord);
            }
        }
        return missingResources;
    }

    private CycleTimeResponse buildCycleTimeResponse(CycleTimePostgres record) {
        return CycleTimeResponse.builder()
                .operation(record.getOperation())
                .operationVersion(record.getOperationVersion())
                .resource(record.getResourceId())
                .resourceType(record.getResourceType())
                .workCenter(record.getWorkcenterId())
                .cycleTime(record.getPlannedCycleTime() != null ? record.getPlannedCycleTime() : 0.0)
                .manufacturedTime(record.getManufacturedTime() != null ? record.getManufacturedTime() : 0.0)
                .userId(record.getUserId())
                .handle(record.getHandle())
                .time(record.getTime())
                .createdBy(record.getCreatedBy())
                .modifiedBy(record.getModifiedBy())
                .targetQuantity(record.getTargetQuantity())
                .active(1)
                .createdDateTime(record.getCreatedDatetime() != null ? record.getCreatedDatetime() : LocalDateTime.now())
                .item(record.getItem())
                .itemVersion(record.getItemVersion())
                .build();
    }

    @Override
    public CycleTimeMessageModel delete(CycleTimeRequest cycleTimeRequest) throws Exception {
        setItemInCycleTimeRequest(cycleTimeRequest);

        if(cycleTimeRequest.getResourceType() != null && !cycleTimeRequest.getResourceType().isEmpty()/* && (cycleTimeRequest.getResource() == null || cycleTimeRequest.getResource().isEmpty())*/) {
            return processDeletionForResourceType(cycleTimeRequest);
        } else {
            return processDeletionForResource(cycleTimeRequest);
        }
    }

    private CycleTimeMessageModel processDeletionForResourceType(CycleTimeRequest cycleTimeRequest) throws Exception {
        ResourceType resourceType = fetchResourceType(cycleTimeRequest);

        if (resourceType == null || CollectionUtils.isEmpty(resourceType.getResourceMemberList())) {
            throw new CycleTimeException(7013, cycleTimeRequest.getResourceType());
        }

        boolean allDeleted = true;
        List<String> missingResources = new ArrayList<>();
        CycleTimeRequest lastProcessedRequest = null;

        for (ResourceMemberList resource : resourceType.getResourceMemberList()) {
            cycleTimeRequest.setResource(resource.getResource());

            try {
                lastProcessedRequest = deleteRecord(cycleTimeRequest);
            } catch (CycleTimeException e) {
                allDeleted = false;
                missingResources.add(resource.getResource());
            }
        }

        if (!allDeleted) {
            throw new CycleTimeException(7013, String.join(", ", missingResources));
        }

        return buildSuccessResponse(lastProcessedRequest);
    }

    private CycleTimeMessageModel processDeletionForResource(CycleTimeRequest cycleTimeRequest) throws Exception {
        CycleTimeRequest lastProcessedRequest = deleteRecord(cycleTimeRequest);
        return buildSuccessResponse(lastProcessedRequest);
    }

    private CycleTimeRequest deleteRecord(CycleTimeRequest cycleTimeRequest) throws Exception {
        CycleTime retrievedMongoRecord = retrieveRec(cycleTimeRequest);
        CycleTimePostgres retrievedPostgreRecord = retrievePostgre(cycleTimeRequest);

        if (retrievedMongoRecord == null || retrievedPostgreRecord == null) {
            throw new CycleTimeException(7013, cycleTimeRequest.getResource());
        }

        markRecordAsDeleted(retrievedMongoRecord, cycleTimeRequest.getModifiedBy());
        markPostgresRecordAsDeleted(retrievedPostgreRecord, cycleTimeRequest.getModifiedBy());

        cycleTimeRepository.save(retrievedMongoRecord);
        cycleTimePostgresRepository.save(retrievedPostgreRecord);

        return cycleTimeResponseBuilder(retrievedMongoRecord);
    }

    private CycleTimeMessageModel buildSuccessResponse(CycleTimeRequest lastProcessedRequest) {
        String createdMessage = getFormattedMessage(6);
        return CycleTimeMessageModel.builder()
                .message_details(new MessageDetails(createdMessage, "S"))
                .response(lastProcessedRequest)
                .build();
    }

    @Override
    public CycleTimeResponse retrieve(CycleTimeRequest cycleTimeRequest) throws Exception {
        setItemInCycleTimeRequest(cycleTimeRequest);

        if(cycleTimeRequest.getResourceType() != null && !cycleTimeRequest.getResourceType().isEmpty()/* && (cycleTimeRequest.getResource() == null || cycleTimeRequest.getResource().isEmpty())*/) {
            return retrieveForResourceType(cycleTimeRequest);
        } else {
            return retrieveForResource(cycleTimeRequest);
        }
    }

    private CycleTimeResponse retrieveForResourceType(CycleTimeRequest cycleTimeRequest) throws Exception {
        ResourceType resourceType = fetchResourceType(cycleTimeRequest);

        if (resourceType == null || CollectionUtils.isEmpty(resourceType.getResourceMemberList())) {
            throw new CycleTimeException(7002);
        }

        List<CycleTimePostgres> foundRecords = new ArrayList<>();
        fetchValidCycleTimeRecords(cycleTimeRequest, resourceType, foundRecords);

        if (foundRecords.isEmpty()) {
            throw new CycleTimeException(7003);
        }

        CycleTimeResponse response = buildCycleTimeResponse(foundRecords.get(0));

        List<CycleTimeResponse> responseList = foundRecords.stream()
                .map(this::buildCycleTimeResponse)
                .collect(Collectors.toList());

        response.setCycleTimeResponseList(responseList);

        return response;
    }

    private CycleTimeResponse retrieveForResource(CycleTimeRequest cycleTimeRequest) {
        String handle = getCycleTimeHandle(cycleTimeRequest);
        CycleTimePostgres postgresRecord = cycleTimePostgresRepository.findBySiteAndActiveAndHandle(
                cycleTimeRequest.getSite(), 1, handle);

        return buildCycleTimeResponse(postgresRecord);
    }

    @Override
    public CycleTimeResponseList retrieveAll(String site) throws Exception {
        List<CycleTimePostgres> allRecordsBySite = cycleTimePostgresRepository.findBySiteAndActiveOrderByCreatedDatetimeDesc(site, 1);

        // Group by item and itemVersion
        Map<String, List<CycleTimePostgres>> groupedByItemAndVersion = allRecordsBySite.stream()
                .collect(Collectors.groupingBy(record -> {
                    if (record.getItem() == null || record.getItem().isEmpty() ||
                            record.getItemVersion() == null || record.getItemVersion().isEmpty()) {
                        return "";  // Treat missing item/itemVersion as an empty key
                    }
                    return record.getItem() + "/" + record.getItemVersion();
                }));

        List<ItemAndVersionGroup> itemAndVersionGroups = new ArrayList<>();

        for (Map.Entry<String, List<CycleTimePostgres>> entry : groupedByItemAndVersion.entrySet()) {
            ItemAndVersionGroup itemAndVersionGroup = new ItemAndVersionGroup();
            itemAndVersionGroup.setItemAndVersion(entry.getKey());

            List<CycleTimePostgres> records = entry.getValue();
            List<ItemBasedRecord> resultRecords = new ArrayList<>();

            // Create a set to track resource types we've already processed
            Set<String> processedResourceTypes = new HashSet<>();

            // Process each record in a single pass
            for (CycleTimePostgres record : records) {
                String resourceType = record.getResourceType();

                // If no resource type or we haven't seen this type before, add the record
                if (resourceType == null || resourceType.isEmpty() ||
                        !processedResourceTypes.contains(resourceType)) {

                    // Mark this resource type as processed
                    if (resourceType != null && !resourceType.isEmpty()) {
                        processedResourceTypes.add(resourceType);
                    }

                    resultRecords.add(ItemBasedRecord.builder()
                            .site(record.getSite())
                            .operation(record.getOperation())
                            .operationVersion(record.getOperationVersion())
                            //.resource(record.getResourceId())
                            .resourceType(resourceType)
                            .workCenter(record.getWorkcenterId())
                            .cycleTime(record.getPlannedCycleTime())
                            .time(record.getTime())
                            .manufacturedTime(record.getManufacturedTime())
                            .targetQuantity(record.getTargetQuantity())
                            .build());
                }
            }

            itemAndVersionGroup.setRecords(resultRecords);
            itemAndVersionGroups.add(itemAndVersionGroup);
        }

        // Sort groups with empty key first
        itemAndVersionGroups.sort(Comparator.comparing(group ->
                group.getItemAndVersion().isEmpty() ? "" : group.getItemAndVersion()));

        return CycleTimeResponseList.builder()
                .itemAndVersionGroups(itemAndVersionGroups)
                .build();
    }
    @Override
    public Boolean createPriorityCombinations(AttachmentPriority attachmentPriority) throws Exception {
        Boolean attachmentCreated = false;

        if (attachmentPriority != null) {
            Boolean isMongoExist=attachmentPriorityRepository.existsByTag(attachmentPriority.getTag());
            Boolean isPostgresExist=cycleTimePostgresRepository.existsByTag(attachmentPriority.getTag());

            if (isMongoExist || isPostgresExist) {
                throw new CycleTimeException(1404);
            }

            attachmentPriority.setHandle(attachmentPriority.getTag());
            AttachmentPriorityPostgres postgres=new AttachmentPriorityPostgres();
            postgres.setHandle(attachmentPriority.getTag());
            postgres.setTag(attachmentPriority.getTag());
            postgres.setPriority(attachmentPriority.getPriority());
            // saving in both the databases
            attachmentPriorityRepository.save(postgres);
            cycleTimePriorityRepository.save(attachmentPriority);
            attachmentCreated = true;
        }
        return attachmentCreated;
    }

    public CycleTimeResponseList retrieveByCriteria(CycleTimeRequest cycleTimeRequest) throws Exception {
        if(StringUtils.isBlank(cycleTimeRequest.getItem()) && StringUtils.isNotBlank(cycleTimeRequest.getMaterial())) {
            cycleTimeRequest.setItem(cycleTimeRequest.getMaterial());
            cycleTimeRequest.setItemVersion(cycleTimeRequest.getMaterialVersion());
        }
        List<CycleTimePostgres> retrievedList = cycleTimePostgresRepository.findByResourceIdAndOperationAndOperationVersionAndItemAndItemVersionAndSiteAndActive(
                cycleTimeRequest.getResource(),
                cycleTimeRequest.getOperation(),
                cycleTimeRequest.getOperationVersion(),
                cycleTimeRequest.getItem(),
                cycleTimeRequest.getItemVersion(),
                cycleTimeRequest.getSite(),
                1
        );
        List<CycleTimeResponse> responseList = new ArrayList<>();

        for (CycleTimePostgres cycleTime : retrievedList) {
            CycleTimeResponse cycleTimeResponse = new CycleTimeResponse();

            cycleTimeResponse.setHandle(cycleTime.getHandle());
            cycleTimeResponse.setCycleTime(cycleTime.getPlannedCycleTime());
            cycleTimeResponse.setItem(cycleTime.getItem() != null ? cycleTime.getItem() : "");
            cycleTimeResponse.setItemVersion(cycleTime.getItemVersion() != null ? cycleTime.getItemVersion() : "");
            cycleTimeResponse.setOperation(cycleTime.getOperation() != null ? cycleTime.getOperation() : "");
            cycleTimeResponse.setOperationVersion(cycleTime.getOperationVersion() != null ? cycleTime.getOperationVersion() : "");
            cycleTimeResponse.setResource(cycleTime.getResourceId() != null ? cycleTime.getResourceId() : "");
            cycleTimeResponse.setWorkCenter(cycleTime.getWorkcenterId() != null ? cycleTime.getWorkcenterId() : "");
            cycleTimeResponse.setCreatedBy(cycleTime.getCreatedBy() != null ? cycleTime.getCreatedBy() : "");
            cycleTimeResponse.setActive(cycleTime.getActive());
            cycleTimeResponse.setModifiedBy(cycleTime.getModifiedBy() != null ? cycleTime.getModifiedBy() : "");
            cycleTimeResponse.setCreatedDateTime(cycleTime.getCreatedDatetime() != null ? cycleTime.getCreatedDatetime() : null);
            cycleTimeResponse.setManufacturedTime(cycleTime.getManufacturedTime() != null ? cycleTime.getManufacturedTime() : 0.0);
            cycleTimeResponse.setUserId(cycleTime.getUserId());
            responseList.add(cycleTimeResponse);
        }

        return CycleTimeResponseList.builder().cycleTimeList(responseList).build();
    }

    private CycleTimeRequest cycleTimeResponseBuilder(CycleTime cycleTime) {
        CycleTimeRequest cycleTimeResponse = new CycleTimeRequest();
        cycleTimeResponse.setSite(cycleTime.getSite());
        cycleTimeResponse.setHandle(cycleTime.getHandle());
        cycleTimeResponse.setCreatedBy(cycleTime.getCreatedBy());
        cycleTimeResponse.setCreatedDateTime(cycleTime.getCreatedDateTime());
        cycleTimeResponse.setModifiedBy(cycleTime.getModifiedBy());
        cycleTimeResponse.setModifiedDateTime(cycleTime.getModifiedDateTime());
        cycleTimeResponse.setCycleTime(cycleTime.getCycleTime());
        cycleTimeResponse.setManufacturedTime(cycleTime.getManufacturedTime() != null ? cycleTime.getManufacturedTime() : 0.0);
        cycleTimeResponse.setActive(cycleTime.getActive());
        cycleTimeResponse.setWorkCenter(cycleTime.getWorkCenterId());
        cycleTimeResponse.setResource(cycleTime.getResourceId());
        cycleTimeResponse.setResourceType(cycleTime.getResourceType());
//            cycleTimeResponse.setMaterial(cycleTime.getMaterial());
//            cycleTimeResponse.setMaterialVersion(cycleTime.getMaterialVersion());
        cycleTimeResponse.setOperation(cycleTime.getOperationId());
        cycleTimeResponse.setOperationVersion(cycleTime.getOperationVersion());
        cycleTimeResponse.setItem(cycleTime.getItem());
        cycleTimeResponse.setItemVersion(cycleTime.getItemVersion());
        cycleTimeResponse.setUserId(cycleTime.getUserId());
        cycleTimeResponse.setPcu(cycleTime.getPcu());
        cycleTimeResponse.setShiftId(cycleTime.getShiftId());
        cycleTimeResponse.setTargetQuantity(cycleTime.getTargetQuantity());
        cycleTimeResponse.setTime(cycleTime.getTime());

        return cycleTimeResponse;
    }

    public CycleTime cycleTimeBuilder(CycleTimeRequest cycleTimeRequest) {
        String handleSetUp = getCycleTimeHandle(cycleTimeRequest);
        return CycleTime.builder()
                .site(cycleTimeRequest.getSite())
                .handle(handleSetUp)
                .operationId(cycleTimeRequest.getOperation())
                .operationVersion(cycleTimeRequest.getOperationVersion())
                .resourceId(cycleTimeRequest.getResource())
                .resourceType(cycleTimeRequest.getResourceType())
                .item(cycleTimeRequest.getItem())
                .itemVersion(cycleTimeRequest.getItemVersion())
//                .material(cycleTimeRequest.getMaterial())
//                .materialVersion(cycleTimeRequest.getMaterialVersion())
                .workCenterId(cycleTimeRequest.getWorkCenter())
                .cycleTime(cycleTimeRequest.getCycleTime())
                .manufacturedTime(cycleTimeRequest.getManufacturedTime() != null ? cycleTimeRequest.getManufacturedTime() : 0.0)
                .createdBy(cycleTimeRequest.getCreatedBy())
                .modifiedBy(cycleTimeRequest.getModifiedBy())
                .createdDateTime(LocalDateTime.now())
                .modifiedDateTime(cycleTimeRequest.getModifiedDateTime())
                .active(1)
                .pcu(cycleTimeRequest.getPcu())
                .shiftId(cycleTimeRequest.getShiftId())
                .userId(cycleTimeRequest.getUserId())
                .targetQuantity(cycleTimeRequest.getTargetQuantity())
                .time(cycleTimeRequest.getTime())
                .build();
    }

    public CycleTimePostgres cycleTimePostgresBuilder(CycleTimeRequest cycleTimeRequest) {
        String handleSetUp = getCycleTimeHandle(cycleTimeRequest);
        return CycleTimePostgres.builder()
                .handle(handleSetUp)
                .site(cycleTimeRequest.getSite())
                .operation(cycleTimeRequest.getOperation())
                .operationVersion(cycleTimeRequest.getOperationVersion())
                .resourceId(cycleTimeRequest.getResource())
                .resourceType(cycleTimeRequest.getResourceType())
                .item(cycleTimeRequest.getItem())
                .itemVersion(cycleTimeRequest.getItemVersion())
//                .material(cycleTimeRequest.getMaterial())
//                .materialVersion(cycleTimeRequest.getMaterialVersion())
                .workcenterId(cycleTimeRequest.getWorkCenter())
                .plannedCycleTime(cycleTimeRequest.getCycleTime())
                .manufacturedTime(cycleTimeRequest.getManufacturedTime() != null ? cycleTimeRequest.getManufacturedTime() : 0.0)
                .createdBy(cycleTimeRequest.getCreatedBy())
                .modifiedBy(cycleTimeRequest.getModifiedBy())
                .createdDatetime(LocalDateTime.now())
                .userId(cycleTimeRequest.getUserId())
                .active(1)
                .tag("Generated_Tag_" + System.currentTimeMillis())
                .priority(1)
                .attachmentCount(1)
                .pcu(cycleTimeRequest.getPcu())
                .shiftId(cycleTimeRequest.getShiftId())
                .targetQuantity(cycleTimeRequest.getTargetQuantity())
                .time(cycleTimeRequest.getTime())
                .build();
    }


    public CycleTime retrieveRec(CycleTimeRequest cycleTimeRequest) throws Exception {

        setItemInCycleTimeRequest(cycleTimeRequest);

        String handle = getCycleTimeHandle(cycleTimeRequest);
        CycleTime retrievedRecord = cycleTimeRepository.findBySiteAndActiveAndHandle(cycleTimeRequest.getSite(), 1, handle);
        /*if (retrievedRecord == null) {
            throw new CycleTimeException(7000);
        }*/
        return retrievedRecord;
    }

    public CycleTimePostgres retrievePostgre(CycleTimeRequest cycleTimeRequest) throws Exception {

        setItemInCycleTimeRequest(cycleTimeRequest);

        String handle = getCycleTimeHandle(cycleTimeRequest);

        CycleTimePostgres retrievedRecord = cycleTimePostgresRepository.findBySiteAndActiveAndHandle(cycleTimeRequest.getSite(), 1,handle);
        /*if (retrievedRecord == null) {
            throw new CycleTimeException(7000);
        }*/
        return retrievedRecord;
    }

    public boolean isCycleTimeExist(CycleTimeRequest cycleTimeRequest) {

        setItemInCycleTimeRequest(cycleTimeRequest);

        String handle = getCycleTimeHandle(cycleTimeRequest);
        return cycleTimeRepository.existsBySiteAndActiveAndHandle(cycleTimeRequest.getSite(), 1, handle);
    }

    public boolean isCycleTimeExistInPostgres(CycleTimeRequest cycleTimeRequest) {

        setItemInCycleTimeRequest(cycleTimeRequest);

        String handle = getCycleTimeHandle(cycleTimeRequest);
        return cycleTimeRepository.existsBySiteAndActiveAndHandle(cycleTimeRequest.getSite(), 1, handle);
    }

    private Item retrieveItem(IsExist isExist) {
        Item item = webClientBuilder.build()
                .post()
                .uri(retrieveItemUrl)
                .bodyValue(isExist)
                .retrieve()
                .bodyToMono(Item.class)
                .block();
        if (item == null || item.getItem() == null || item.getItem().isEmpty()) {
            throw new CycleTimeException(7005, isExist.getItem(), isExist.getRevision());
        }
        return item;
    }

    private Tag createTag(CycleTimeRequest cycleTimeRequest) {
        count = 0;
        List<String> listToBeConcatenated = new ArrayList<>();
        List<String> priorityTagToBeConcatenated = new ArrayList<>();

        if (StringUtils.isNotBlank(cycleTimeRequest.getItem()) && StringUtils.isNotBlank(cycleTimeRequest.getItemVersion())) {
            count++;
            listToBeConcatenated.add("item_" + cycleTimeRequest.getItem() + "/" + cycleTimeRequest.getItemVersion());
            priorityTagToBeConcatenated.add("item");
        }
      /*  if(StringUtils.isNotBlank(cycleTimeRequest.getShopOrder()))
        {
            count++;
            listToBeConcatenated.add("shopOrder_"+cycleTimeRequest.getShopOrder());
            priorityTagToBeConcatenated.add("shopOrder");
        }*/
        if (StringUtils.isNotBlank(cycleTimeRequest.getWorkCenter())) {
            count++;
            listToBeConcatenated.add("workCenter_" + cycleTimeRequest.getWorkCenter());
            priorityTagToBeConcatenated.add("workCenter");
        }
        if (StringUtils.isNotBlank(cycleTimeRequest.getResource())) {
            count++;
            listToBeConcatenated.add("resource_" + cycleTimeRequest.getResource());
            priorityTagToBeConcatenated.add("resource");
        }
      /*  if(StringUtils.isNotBlank(cycleTimeRequest.getRouting()) && StringUtils.isNotBlank(cycleTimeRequest.getRoutingVersion()))
        {
            count++;
            listToBeConcatenated.add("routing_"+cycleTimeRequest.getRouting()+"/"+cycleTimeRequest.getRoutingVersion());
            priorityTagToBeConcatenated.add("routing");
        }*/
        if (StringUtils.isNotBlank(cycleTimeRequest.getOperation()) && StringUtils.isNotBlank(cycleTimeRequest.getOperationVersion())) {
            count++;
            listToBeConcatenated.add("operation_" + cycleTimeRequest.getOperation() + "/" + cycleTimeRequest.getOperationVersion());
            priorityTagToBeConcatenated.add("operation");
        }

        String concatenatedAttachment = String.join("_", listToBeConcatenated);
        String concatenatedPriorityTag = String.join(",", priorityTagToBeConcatenated);
        return Tag.builder().tag(concatenatedAttachment).priorityCombination(concatenatedPriorityTag).build();
    }
    public void validate(CycleTimeRequest cycleTimeRequest) throws Exception {
       /* if(StringUtils.isNotEmpty(cycleTimeRequest.getShopOrder())) {
            isShopOrderExists(cycleTimeRequest.getSite(), cycleTimeRequest.getShopOrder());
        }*/
        if (StringUtils.isNotEmpty(cycleTimeRequest.getResource())) {
            isResourceExists(cycleTimeRequest.getSite(), cycleTimeRequest.getResource());
        }
        if (StringUtils.isNotEmpty(cycleTimeRequest.getItem()) || StringUtils.isNotEmpty(cycleTimeRequest.getItemVersion())) {
            isItemExists(cycleTimeRequest.getSite(), cycleTimeRequest.getItem(), cycleTimeRequest.getItemVersion());
        }
        if (StringUtils.isNotEmpty(cycleTimeRequest.getOperation()) || StringUtils.isNotEmpty(cycleTimeRequest.getOperationVersion())) {
            isOperationExists(cycleTimeRequest.getSite(), cycleTimeRequest.getOperation(), cycleTimeRequest.getOperationVersion());
        }
      /*  if(StringUtils.isNotEmpty(cycleTimeRequest.getRouting()) || StringUtils.isNotEmpty(cycleTimeRequest.getRoutingVersion())) {
            isRoutingExists(cycleTimeRequest.getSite(), cycleTimeRequest.getRouting(), cycleTimeRequest.getRoutingVersion());
        }*/
       /* if(cycleTimeRequest.getCycleTime()>cycleTimeRequest.getManufacturedTime())
        {
            throw new CycleTimeException(7008);
        }*/

    }


    public Boolean isShopOrderExists(String site, String shopOrder) throws Exception {
        IsExist shopOrderRetrieveRequest = IsExist.builder().site(site).shopOrder(shopOrder).build();
        Boolean isExist = webClientBuilder
                .build()
                .post()
                .uri(shopOrderUrl)
                .body(BodyInserters.fromValue(shopOrderRetrieveRequest))
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        if (!isExist) {
            throw new CycleTimeException(7001, shopOrder);
        }
        return isExist;
    }


    public Boolean isResourceExists(String site, String resource) throws Exception {
        IsExist resourceRequest = IsExist.builder().site(site).resource(resource).build();
        Boolean isResourceExist = webClientBuilder.build()
                .post()
                .uri(resourceUrl)
                .bodyValue(resourceRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        if (isResourceExist || !isResourceExist) {
            throw new CycleTimeException(7002, resource);
        }
        return isResourceExist;
    }


    public Boolean isOperationExists(String site, String operation, String operationVersion) throws Exception {
        IsExist operationRequest = IsExist.builder().operation(operation).site(site).revision(operationVersion).build();
        Boolean isOperationExist = webClientBuilder.build()
                .post()
                .uri(operationUrl)
                .bodyValue(operationRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        if (!isOperationExist) {
            throw new CycleTimeException(7003, operation, operationVersion);
        }
        return isOperationExist;
    }


    public Boolean isRoutingExists(String site, String routing, String routingVersion) throws Exception {
        IsExist isExist = IsExist.builder().site(site).routing(routing).version(routingVersion).build();
        Boolean routingExist = webClientBuilder.build()
                .post()
                .uri(routingUrl)
                .bodyValue(isExist)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        if (!routingExist) {
            throw new CycleTimeException(7004, routing, routingVersion);
        }
        return routingExist;
    }


    public Boolean isItemExists(String site, String item, String itemVersion) throws Exception {
        IsExist isExist = IsExist.builder().site(site).item(item).revision(itemVersion).build();
        Boolean itemExist = webClientBuilder.build()
                .post()
                .uri(itemUrl)
                .bodyValue(isExist)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        if (!itemExist) {
            throw new CycleTimeException(7005, item, itemVersion);
        }
        return itemExist;
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

    public static Map<String, String> createFields(Attachment attachment) {
        Map<String, String> attachmentList = new LinkedHashMap<>();
        attachmentList.put("item", attachment.getItem());
        attachmentList.put("workCenter", attachment.getWorkCenter());
        attachmentList.put("resource", attachment.getResource());
        attachmentList.put("routing", attachment.getRouting());
        attachmentList.put("operation", attachment.getOperation());
        attachmentList.put("shopOrder", attachment.getShopOrder());
        return attachmentList;
    }


    @Override
    public List<CycleTimeDto> getCalculatedPerformance(PerformanceRequest request) {

        PerformanceRequest performanceRequest = PerformanceRequest.builder()
                .site(request.getSite())
                .shiftIds(request.getShiftIds())
                .startDateTime(request.getStartDateTime())
                .endDateTime(request.getEndDateTime())
                .build();

        PerformancePlannedOperatingTimeResponse performancePlannedOperatingTimeResponse = webClientBuilder.build()
                .post()
                .uri(performancePlannedOperatingTimeUrl)
                .bodyValue(performanceRequest)
                .retrieve()
                .bodyToMono(PerformancePlannedOperatingTimeResponse.class)
                .block();

        Double availableProductionTime = (performancePlannedOperatingTimeResponse != null && performancePlannedOperatingTimeResponse.getPlannedOperatingTime() != null)
                ? performancePlannedOperatingTimeResponse.getPlannedOperatingTime()
                : 0.0;


        ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                .site(request.getSite())
                .startDateTime(request.getStartDateTime())
                .build();

        List<ProductionLogDto> combinationResponse = webClientBuilder.build()
                .post()
                .uri(productionlog)
                .bodyValue(productionLogRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ProductionLogDto>>() {
                })
                .block();

        List<ProductionLogResponseDto> productionLogResponseDtos = webClientBuilder.build()
                .post()
                .uri(actualCycleTime)
                .bodyValue(productionLogRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ProductionLogResponseDto>>() {
                })
                .block();


        List<CycleTimeDto> plannedCycleTimeData = new ArrayList<>();

        if (combinationResponse != null && !combinationResponse.isEmpty() &&
                productionLogResponseDtos != null && !productionLogResponseDtos.isEmpty()) {

            Iterator<ProductionLogDto> combinationIterator = combinationResponse.iterator();
            Iterator<ProductionLogResponseDto> responseIterator = productionLogResponseDtos.iterator();

            while (combinationIterator.hasNext() && responseIterator.hasNext()) {
                ProductionLogDto productionLogDto = combinationIterator.next();
                ProductionLogResponseDto responseDto = responseIterator.next();

                Double plannedCycleTime = cycleTimePostgresRepository.findPlannedCycleTime(
                        productionLogDto.getSite(),
                        productionLogDto.getWorkcenter_id(),
                        productionLogDto.getOperation(),
                        productionLogDto.getOperation_version(),
                        productionLogDto.getResource_id(),
                        productionLogDto.getItem(),
                        productionLogDto.getItem_version(),
                        productionLogDto.getShift_id(),
                        productionLogDto.getPcu()
                );

                if (plannedCycleTime == null || plannedCycleTime == 0) {
                    plannedCycleTime = 0.0;
                }

                Double plannedQuantity = (plannedCycleTime > 0) ? availableProductionTime / plannedCycleTime : 0.0;

                double actualCycleTime = responseDto.getActualCycleTime();
                double actualQuantity = responseDto.getActualQuantity();
                double manufacturedTime = responseDto.getManufacturedTime();

                Double performancePercentageByCycleTime = 0.0;
                if (actualCycleTime != 0 && actualQuantity != 0) {
                    performancePercentageByCycleTime = ((plannedCycleTime * plannedQuantity) / (actualCycleTime * actualQuantity)) * 100;
                }

                Double performancePercentageByManufacturedTime = 0.0;
                if (manufacturedTime != 0 && actualQuantity != 0) {
                    performancePercentageByManufacturedTime = ((plannedCycleTime * plannedQuantity) / (manufacturedTime * actualQuantity)) * 100;
                }

                CycleTimeDto cycleTimeDto = new CycleTimeDto();
                cycleTimeDto.setSite(productionLogDto.getSite());
                cycleTimeDto.setShiftId(productionLogDto.getShift_id());
                cycleTimeDto.setWorkcenterId(productionLogDto.getWorkcenter_id());
                cycleTimeDto.setResourceId(productionLogDto.getResource_id());
                cycleTimeDto.setItem(productionLogDto.getItem());
                cycleTimeDto.setItemVersion(productionLogDto.getItem_version());
                cycleTimeDto.setOperation(productionLogDto.getOperation());
                cycleTimeDto.setPcu(productionLogDto.getPcu());
                cycleTimeDto.setOperationVersion(productionLogDto.getOperation_version());
                cycleTimeDto.setPlannedCycleTime(plannedCycleTime);
                cycleTimeDto.setActualCycleTime(actualCycleTime);
                cycleTimeDto.setPlannedOutput(plannedQuantity);
                cycleTimeDto.setActualOutput(actualQuantity);
                cycleTimeDto.setPerformancePercentage(performancePercentageByCycleTime);
                cycleTimeDto.setCreatedDatetime(LocalDateTime.now());
                cycleTimeDto.setActive(1);

                plannedCycleTimeData.add(cycleTimeDto);
            }
        }

        return plannedCycleTimeData;
    }

    @Override
    public Double getPlannedCycleTime(ProductionLogDto request) {
        Double plannedCycleTime = cycleTimePostgresRepository.findPlannedCycleTime(
                request.getSite(),
                request.getWorkcenter_id(),
                request.getOperation(),
                request.getOperation_version(),
                request.getResource_id(),
                request.getItem(),
                request.getItem_version(),
                request.getShift_id(),
                request.getPcu()
        );
        return plannedCycleTime;
    }

    @Override
    public List<CycleTime> getActiveCycleTimesBySiteAndResource(String site, String resourceId) {
        return cycleTimeRepository.findBySiteAndResourceIdAndActive(site, resourceId, 1);
    }

    public List<CycleTime> getActiveCycleTimesBySiteAndWorkcenter(String site, String workcenterId) {
        return cycleTimeRepository.findBySiteAndWorkCenterIdAndActive(site, workcenterId, 1);
    }
    /*@Override
    public List<CycleTime> getFilteredCycleTimes(CycleTimeReq cycleTimeReq) {
        // Fetch all active cycle times for the given site and resource
        List<CycleTime> cycleTimes = cycleTimeRepository.findBySiteAndResourceIdAndActive(
                cycleTimeReq.getSite(),
                cycleTimeReq.getResourceId(),
                1
        );

        // Filter cycle times based on the item and itemVersion list
        return cycleTimes.stream()
                .filter(cycleTime ->
                        cycleTime.getItem() == null || cycleTime.getItem().isEmpty() ||
                                cycleTimeReq.getItemVersionReqs().stream().anyMatch(req ->
                                        req.getItem().equals(cycleTime.getItem()) &&
                                                req.getItemVersion().equals(cycleTime.getItemVersion())))
                .collect(Collectors.toList());
    }*/

    @Override
    public List<CycleTime> getFilteredCycleTimes(CycleTimeReq cycleTimeReq) {
        // Fetch all active cycle times for the given site and resource
        List<CycleTime> cycleTimes = cycleTimeRepository.findBySiteAndResourceIdAndActive(
                cycleTimeReq.getSite(),
                cycleTimeReq.getResourceId(),
                1
        );

        if (cycleTimes.isEmpty()) {
            CycleTime zeroCycleTime = new CycleTime();
            zeroCycleTime.setHandle("Average");
            zeroCycleTime.setResourceId(cycleTimeReq.getResourceId());
            zeroCycleTime.setSite(cycleTimeReq.getSite());
            zeroCycleTime.setItem(null);
            zeroCycleTime.setItemVersion(null);
            zeroCycleTime.setCycleTime(0.0);
            cycleTimes.add(zeroCycleTime);
            return cycleTimes;
        }

        // Filter cycle times based on item and itemVersion
        List<CycleTime> filteredCycleTimes = cycleTimes.stream()
                .filter(cycleTime ->
                        cycleTime.getItem() == null || cycleTime.getItem().isEmpty() ||
                                cycleTimeReq.getItemVersionReqs().stream().anyMatch(req ->
                                        req.getItem().equals(cycleTime.getItem()) &&
                                                req.getItemVersion().equals(cycleTime.getItemVersion())))
                .collect(Collectors.toList());

        // Check if there's already a resource-level cycle time
        boolean hasResourceLevelCycleTime = filteredCycleTimes.stream()
                .anyMatch(cycleTime ->
                        cycleTime.getResourceId() != null && cycleTime.getResourceId() != "" &&
                                (cycleTime.getItem() == null || cycleTime.getItem().isEmpty()));

        // If resource-level cycle time exists, return filtered results
        if (hasResourceLevelCycleTime) {
            return filteredCycleTimes;
        }

        // Compute the average cycle time across all items for this resource
        double avgCycleTime = cycleTimes.stream()
                .mapToDouble(CycleTime::getCycleTime)
                .average()
                .orElse(0);  // Default to 0 if no cycle time exists

        // Create and add the averaged cycle time entry only if needed
        CycleTime averagedCycleTime = new CycleTime();
        averagedCycleTime.setHandle("Average");
        averagedCycleTime.setResourceId(cycleTimeReq.getResourceId());
        averagedCycleTime.setSite(cycleTimeReq.getSite());
        averagedCycleTime.setItem(null);
        averagedCycleTime.setItemVersion(null);
        averagedCycleTime.setCycleTime(avgCycleTime);

        filteredCycleTimes.add(averagedCycleTime);

        return filteredCycleTimes;
    }

    @Override
    public List<CycleTime> getFilteredCycleTimesByWorkCenter(CycleTimeReq cycleTimeReq) {
        // Fetch all active cycle times for the given site and workcenter
        List<CycleTime> cycleTimes = cycleTimeRepository.findBySiteAndWorkCenterIdAndActive(
                cycleTimeReq.getSite(),
                cycleTimeReq.getWorkCenterId(),
                1
        );

        if (cycleTimes.isEmpty()) {
            CycleTime zeroCycleTime = new CycleTime();
            zeroCycleTime.setHandle("ZeroCycleTime");
            zeroCycleTime.setResourceId(cycleTimeReq.getResourceId());
            zeroCycleTime.setSite(cycleTimeReq.getSite());
            zeroCycleTime.setItem(null);
            zeroCycleTime.setItemVersion(null);
            zeroCycleTime.setCycleTime(0.0);
            cycleTimes.add(zeroCycleTime);
            return cycleTimes;
        }

        // Filter cycle times based on item and itemVersion (only matching items)
        List<CycleTime> filteredCycleTimes = cycleTimes.stream()
                .filter(cycleTime ->
                        cycleTimeReq.getItemVersionReqs().stream().anyMatch(req ->
                                req.getItem().equals(cycleTime.getItem()) &&
                                        req.getItemVersion().equals(cycleTime.getItemVersion())))
                .collect(Collectors.toList());

        // Check if there's already a workcenter-level cycle time
        boolean hasWorkCenterLevelCycleTime = filteredCycleTimes.stream()
                .anyMatch(cycleTime ->
                        cycleTime.getWorkCenterId() != null && !cycleTime.getWorkCenterId().isEmpty() &&
                                (cycleTime.getItem() == null || cycleTime.getItem().isEmpty()));

        // If workcenter-level cycle time exists, return filtered results
        if (hasWorkCenterLevelCycleTime) {
            return filteredCycleTimes;
        }

        // Compute the average manufactured time across all items for this workcenter
        double avgManufacturedTime = cycleTimes.stream()
                .mapToDouble(CycleTime::getManufacturedTime)
                .average()
                .orElse(0); // Default to 0 if no manufactured time exists

        // Create and add the averaged manufactured time entry only if needed
        CycleTime averagedCycleTime = new CycleTime();
        averagedCycleTime.setHandle("Average");
        averagedCycleTime.setWorkCenterId(cycleTimeReq.getWorkCenterId());
        averagedCycleTime.setSite(cycleTimeReq.getSite());
        averagedCycleTime.setItem(null);
        averagedCycleTime.setItemVersion(null);
        averagedCycleTime.setManufacturedTime(avgManufacturedTime);

        filteredCycleTimes.add(averagedCycleTime);

        return filteredCycleTimes;
    }


    @Override
    public Double getCycleTimeValue(ProductionLogDto dto) {
        // Site is mandatory.
        if (StringUtils.isBlank(dto.getSite())) {
            return null;
        }

        // Determine which field to use based on eventType.
/*        boolean useCycleTime = "completeSfcBatch".equalsIgnoreCase(dto.getEventType());
        boolean useManufacturedTime = "doneSfcBatch".equalsIgnoreCase(dto.getEventType());*/
        boolean useCycleTime = "completeSfcBatch".equalsIgnoreCase(dto.getEventType())
                || "machineCompleteSfcBatch".equalsIgnoreCase(dto.getEventType());
        boolean useManufacturedTime = "doneSfcBatch".equalsIgnoreCase(dto.getEventType())
                || "machineDoneSfcBatch".equalsIgnoreCase(dto.getEventType());

        if (!useCycleTime && !useManufacturedTime) {
            // Unrecognized eventType
            return null;
        }

        // Ensure that at least one base key is provided.
        boolean hasResource = StringUtils.isNotBlank(dto.getResource_id());
        boolean hasWorkCenter = StringUtils.isNotBlank(dto.getWorkcenter_id());
        if (!hasResource && !hasWorkCenter) {
            return null;
        }

        // Determine if the input is base criteria only.
        // Extra fields: operation, operation_version, item, item_version, shift_id, pcu.
        boolean extraFieldsProvided = StringUtils.isNotBlank(dto.getOperation()) ||
                StringUtils.isNotBlank(dto.getOperation_version()) ||
                StringUtils.isNotBlank(dto.getItem()) ||
                StringUtils.isNotBlank(dto.getItem_version());// ||
            //    StringUtils.isNotBlank(dto.getShift_id()) ||
            //    StringUtils.isNotBlank(dto.getPcu());

        // --- Case 1: Base Criteria Only ---
        if (!extraFieldsProvided) {
            Query baseQuery = new Query();
            // Mandatory site check.
            baseQuery.addCriteria(Criteria.where("site").is(dto.getSite()));

            // Base key.
            if (hasResource) {
                baseQuery.addCriteria(Criteria.where("resourceId").is(dto.getResource_id()));
            } else {
                baseQuery.addCriteria(Criteria.where("workCenterId").is(dto.getWorkcenter_id()));
            }

            // IMPORTANT: Ensure extra fields in the record are empty or null.
            baseQuery.addCriteria(Criteria.where("operationId").in("", null));
            baseQuery.addCriteria(Criteria.where("operationVersion").in("", null));
            baseQuery.addCriteria(Criteria.where("item").in("", null));
            baseQuery.addCriteria(Criteria.where("itemVersion").in("", null));
            baseQuery.addCriteria(Criteria.where("shiftId").in("", null));
            baseQuery.addCriteria(Criteria.where("pcu").in("", null));

            List<CycleTime> records = mongoTemplate.find(baseQuery, CycleTime.class);
            if (records == null || records.isEmpty()) {
                // If no record is found, then (as per your specification) we retrieve all records for the base key
                // and average their values. In this case the baseQuery is already limiting to those with empty extra fields,
                // so if nothing is found we return 0.
                return 0.0;
            }
            return computeValue(records, useCycleTime);
        }

        // --- Case 2: Combination Provided ---
        // Here we include the provided extra fields in the query.
        Query comboQuery = new Query();
        comboQuery.addCriteria(Criteria.where("site").is(dto.getSite()));
        if (hasResource) {
            comboQuery.addCriteria(Criteria.where("resourceId").is(dto.getResource_id()));
        } else {
            comboQuery.addCriteria(Criteria.where("workCenterId").is(dto.getWorkcenter_id()));
        }
        if (StringUtils.isNotBlank(dto.getOperation())) {
            comboQuery.addCriteria(Criteria.where("operationId").is(dto.getOperation()));
        }
        if (StringUtils.isNotBlank(dto.getOperation_version())) {
            comboQuery.addCriteria(Criteria.where("operationVersion").is(dto.getOperation_version()));
        }
        if (StringUtils.isNotBlank(dto.getItem())) {
            comboQuery.addCriteria(Criteria.where("item").is(dto.getItem()));
        }
        if (StringUtils.isNotBlank(dto.getItem_version())) {
            comboQuery.addCriteria(Criteria.where("itemVersion").is(dto.getItem_version()));
        }
        /*if (StringUtils.isNotBlank(dto.getShift_id())) {
            comboQuery.addCriteria(Criteria.where("shiftId").is(dto.getShift_id()));
        }
        if (StringUtils.isNotBlank(dto.getPcu())) {
            comboQuery.addCriteria(Criteria.where("pcu").is(dto.getPcu()));
        }*/

        List<CycleTime> comboRecords = mongoTemplate.find(comboQuery, CycleTime.class);
        if (comboRecords == null || comboRecords.isEmpty()) {
            // For combination input, if no record is found then return 0.
            return 0.0;
        }
        return computeValue(comboRecords, useCycleTime);
    }

    /**
     * Helper method to compute the value from the list of CycleTime records.
     * If a single record exists, its value is returned.
     * If multiple records exist, the value (cycleTime or manufacturedTime) is averaged.
     */
    private Double computeValue(List<CycleTime> records, boolean useCycleTime) {
        if (records.size() == 1) {
            CycleTime ct = records.get(0);
            return useCycleTime ? ct.getCycleTime() : ct.getManufacturedTime();
        }
        double sum = 0.0;
        int count = 0;
        for (CycleTime ct : records) {
            Double value = useCycleTime ? ct.getCycleTime() : ct.getManufacturedTime();
            if (value != null) {
                sum += value;
                count++;
            }
        }
        return count > 0 ? sum / count : 0.0;
    }

    //    private CycleTimeRequest setCycleTimeAndMfgTime(CycleTimeRequest cycleTimeRequest) {
//        if(cycleTimeRequest.getMaterial()!=null && !cycleTimeRequest.getMaterial().isEmpty()){
//            IsExist isExist= IsExist.builder().site(cycleTimeRequest.getSite()).item(cycleTimeRequest.getMaterial()).revision(cycleTimeRequest.getMaterialVersion()).build();
//            Item item= retrieveItem(isExist);
//            double lotsize=Double.parseDouble(item.getLotSize());
//            double calculatedIdealTime= cycleTimeRequest.getCycleTime() / lotsize;
//            double calculatedMfgTime= cycleTimeRequest.getManufacturedTime() / lotsize;
//            calculatedIdealTime = Double.parseDouble(String.format("%.3f", calculatedIdealTime));
//            calculatedMfgTime = Double.parseDouble(String.format("%.3f", calculatedMfgTime));
//            cycleTimeRequest.setCycleTime(calculatedIdealTime);
//            cycleTimeRequest.setManufacturedTime(calculatedMfgTime);
//
//        }
//        return cycleTimeRequest;
//    }


    /* @PostConstruct
     public Boolean uploadCycleTimePriorityRecordsToDataBaseOnLoad()
     {
         *//*ObjectMapper objectMapper = new ObjectMapper();
        File jsonFile = new File(cycleTimePrioritiesFile);

        List<CycleTime> records = cycleTimePriorityConfig.getPriorities();*//*
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            List<CycleTime> records = objectMapper.readValue(cycleTimePrioritiesJson, new TypeReference<List<CycleTime>>() {});
                 //  List<CycleTime> records = objectMapper.readValue(jsonFile, new TypeReference<List<CycleTime>>() {});
            if(records == null || records.isEmpty()){
                return false;
           }
            cycleTimeRepository.saveAll(records);
       } catch (IOException e) {
         throw new RuntimeException(e);
        }
        return true;
    }*/


   /* public CycleTime cycleTimeBuilder(CycleTimeRequest cycleTimeRequest)
    {
        Tag tag =  createTag(cycleTimeRequest);
        CycleTime createCycleTime = CycleTime.builder()
                .site(cycleTimeRequest.getSite())
               *//* .shopOrder(cycleTimeRequest.getShopOrder())//

                //.routing(cycleTimeRequest.getRouting())
                .routingVersion(cycleTimeRequest.getRoutingVersion())*//*
                .resourceId("ResourceBO:" + cycleTimeRequest.getSite() + "," + cycleTimeRequest.getResource())
                .operationId("OperationBO:" + cycleTimeRequest.getSite() + "," + cycleTimeRequest.getOperation() + "," + cycleTimeRequest.getOperationVersion())
                *//*.operationVersion(cycleTimeRequest.getOperationVersion())*//*
                .itemId("ItemBO:" + cycleTimeRequest.getSite() + "," + cycleTimeRequest.getMaterial() + "," + cycleTimeRequest.getMaterialVersion())
               *//* .itemVersion(cycleTimeRequest.getMaterialVersion())*//*
             .workCenterId("WorkcenterBO:" + cycleTimeRequest.getSite() + "," + cycleTimeRequest.getWorkCenter())
                .cycleTime(cycleTimeRequest.getCycleTime())
                .manufacturedTime(cycleTimeRequest.getManufacturedTime())
               *//* .priority(attachmentPriority.getPriority())*//*
     *//*    .wholeCycleTime(cycleTimeRequest.getWholeCycletime())
                .wholeManufacturedTime(cycleTimeRequest.getWholeManufacturedTime())*//*
     *//* .attachmentCount(count)*//*
     *//*   .tag(tag.getTag())*//*
                .active(1)
                .build();
        return createCycleTime;
    }*/


   /* public CycleTime retrieveRec(CycleTimeRequest cycleTimeRequest) throws Exception
    {
        CycleTimeResponse response=new CycleTimeResponse();
        CycleTime retrievedRecord = null;
        String handle = "CycleTimeBO:"+cycleTimeRequest.getSite()+"_"+"ShopOrderBO:"+cycleTimeRequest.getSite()+"_"+"OperationBO:"+cycleTimeRequest.getSite()+","+cycleTimeRequest.getOperation()+","+cycleTimeRequest.getOperationVersion()+"_"+"ResourceBO:"+cycleTimeRequest.getSite()+","+cycleTimeRequest.getResource()+"_"+"ItemBO:"+cycleTimeRequest.getSite()+","+cycleTimeRequest.getMaterial()+","+cycleTimeRequest.getMaterialVersion();
        retrievedRecord = cycleTimeRepository.findBySiteAndActiveAndHandle(cycleTimeRequest.getSite(),1,handle);
        if(retrievedRecord==null)
        {
            throw new CycleTimeException(7000);
        }
        return retrievedRecord;
    }*/
   /*public CycleTime retrieveRec(CycleTimeRequest cycleTimeRequest) throws Exception {
       // Initialize the handle with the site and basic structure
       StringBuilder handleBuilder = new StringBuilder("CycleTimeBO:")
               .append(cycleTimeRequest.getSite())
               .append("_ShopOrderBO:")
               .append(cycleTimeRequest.getSite());

       // Append operation and operationVersion if they are not empty or null
       if (StringUtils.isNotEmpty(cycleTimeRequest.getOperation()) && StringUtils.isNotEmpty(cycleTimeRequest.getOperationVersion())) {
           handleBuilder.append("_OperationBO:")
                   .append(cycleTimeRequest.getSite()).append(",")
                   .append(cycleTimeRequest.getOperation()).append(",")
                   .append(cycleTimeRequest.getOperationVersion());
       }

       // Append resource if it is not empty or null
       if (StringUtils.isNotEmpty(cycleTimeRequest.getResource())) {
           handleBuilder.append("_ResourceBO:")
                   .append(cycleTimeRequest.getSite()).append(",")
                   .append(cycleTimeRequest.getResource());
       }

       // Append material and materialVersion if they are not empty or null
       if (StringUtils.isNotEmpty(cycleTimeRequest.getMaterial()) && StringUtils.isNotEmpty(cycleTimeRequest.getMaterialVersion())) {
           handleBuilder.append("_ItemBO:")
                   .append(cycleTimeRequest.getSite()).append(",")
                   .append(cycleTimeRequest.getMaterial()).append(",")
                   .append(cycleTimeRequest.getMaterialVersion());
       }

       // Convert StringBuilder to String to form the complete handle
       String handle = handleBuilder.toString();

       // Retrieve the record from the repository
       CycleTime retrievedRecord = cycleTimeRepository.findBySiteAndActiveAndHandle(cycleTimeRequest.getSite(), 1, handle);

       // Throw an exception if no record is found
       if (retrievedRecord == null) {
           throw new CycleTimeException(7000);
       }

       return retrievedRecord;
   }*/

    /*public CycleTimeResponseList retrieveAll(String site) throws Exception
    {
        List<CycleTimeResponse> response= new ArrayList<CycleTimeResponse>();
        CycleTimeResponse cycleTimeResponse=new CycleTimeResponse();
        List<CycleTime> allRecordsBySite = cycleTimeRepository.findBySiteAndActive(site,1);
        for (CycleTime cycleTime : allRecordsBySite) {
            String[] itemobj = cycleTime.getItem().split(",");
            String[] oprobj = cycleTime.getOperationId().split(",");
            String[] resobj = cycleTime.getResourceId().split(",");
            String[] workcenter = cycleTime.getWorkCenterId().split(",");
            cycleTimeResponse.setHandle(cycleTime.getHandle());
            cycleTimeResponse.setCycleTime(cycleTime.getCycleTime());
            cycleTimeResponse.setMaterial(itemobj[1]);
            cycleTimeResponse.setMaterialVersion(itemobj[2]);
            cycleTimeResponse.setOperation(oprobj[1]);
            cycleTimeResponse.setOperationVersion(oprobj[2]);
            cycleTimeResponse.setResource(resobj[1]);
            cycleTimeResponse.setWorkCenter(workcenter[1]);
            cycleTimeResponse.setCreatedBy(cycleTime.getCreatedBy());
            cycleTimeResponse.setManufacturedTime(cycleTime.getManufacturedTime());
            cycleTimeResponse.setCycleTime(cycleTime.getCycleTime());
            response.add(cycleTimeResponse);
        }
        return CycleTimeResponseList.builder().cycleTimeList(response).build();
    }
*/



    private String getArrayElement(String[] array, int index) {
        return array.length > index ? array[index] : null;
    }


    /*   public CycleTimeResponseList retrieveBySiteActiveAndObject(CycleTimeRequest cycleTimeRequest)throws Exception
    {
        Query query = new Query();
      *//*  if(StringUtils.isNotEmpty(cycleTimeRequest.getShopOrder())) {
            query.addCriteria(Criteria.where("shopOrder").is(cycleTimeRequest.getShopOrder()));
        }*//*
        if(StringUtils.isNotEmpty(cycleTimeRequest.getResource()))
        {
            query.addCriteria(Criteria.where("resource").is(cycleTimeRequest.getResource()));
        }
     *//*   if(StringUtils.isNotEmpty(cycleTimeRequest.getRouting()) && StringUtils.isNotEmpty(cycleTimeRequest.getRoutingVersion()))
        {
            query.addCriteria(Criteria.where("routing").is(cycleTimeRequest.getRouting()));
            query.addCriteria(Criteria.where("routingVersion").is(cycleTimeRequest.getRoutingVersion()));
        }*//*
        if(StringUtils.isNotEmpty(cycleTimeRequest.getOperation()) && StringUtils.isNotEmpty(cycleTimeRequest.getOperationVersion()))
        {
            query.addCriteria(Criteria.where("operation").is(cycleTimeRequest.getOperation()));
            query.addCriteria(Criteria.where("operationVersion").is(cycleTimeRequest.getOperationVersion()));
        }
        if(StringUtils.isNotEmpty(cycleTimeRequest.getMaterial()) && StringUtils.isNotEmpty(cycleTimeRequest.getMaterialVersion()))
        {
            query.addCriteria(Criteria.where("item").is(cycleTimeRequest.getMaterial()));
            query.addCriteria(Criteria.where("itemVersion").is(cycleTimeRequest.getMaterialVersion()));
        }
        query.addCriteria(Criteria.where("site").is(cycleTimeRequest.getSite()));
        query.addCriteria(Criteria.where("active").is(1));
        List<CycleTime> retrievedList = new ArrayList<>();

        retrievedList = mongoTemplate.find(query, CycleTime.class);
        for (CycleTime cycleTime : retrievedList) {
            CycleTimeResponse cycleTimeResponse=new CycleTimeResponse();
            String[] itemobj = cycleTime.getItem().split(",");
            String[] oprobj = cycleTime.getOperationId().split(",");
            String[] resobj = cycleTime.getResourceId().split(",");
            String[] workcenter = cycleTime.getWorkCenterId().split(",");
            cycleTimeResponse.setHandle(cycleTime.getHandle());
            cycleTimeResponse.setCycleTime(cycleTime.getCycleTime());
            cycleTimeResponse.setMaterial(itemobj[1]);
            cycleTimeResponse.setMaterialVersion(itemobj[2]);
            cycleTimeResponse.setOperation(oprobj[1]);
            cycleTimeResponse.setOperationVersion(oprobj[2]);
            cycleTimeResponse.setResource(resobj[1]);
            cycleTimeResponse.setWorkCenter(workcenter[1]);
            cycleTimeResponse.setCreatedBy(cycleTime.getCreatedBy());
            cycleTimeResponse.setManufacturedTime(cycleTime.getManufacturedTime());
            cycleTimeResponse.setCycleTime(cycleTime.getCycleTime());
            cycleTimeResponseList.addAll(cycleTimeResponse);
        }
       // CycleTimeResponseList cycleTimeResponseList = CycleTimeResponseList.builder().cycleTimeList(retrievedList).build();
        return cycleTimeResponseList;
    }*/


    // @Override
   /* public CycleTime retrieveByAttachment(CycleTimeRequest cycleTimeRequest)throws Exception
    {
        List<CycleTime> forShopOrder = new ArrayList<>();

       *//* if(StringUtils.isNotEmpty(cycleTimeRequest.getShopOrder()))
        {*//*
            Query query = new Query();
           *//* query.addCriteria(Criteria.where("shopOrder").is(cycleTimeRequest.getShopOrder()));*//*

            if(StringUtils.isNotEmpty(cycleTimeRequest.getMaterial()) && StringUtils.isNotEmpty(cycleTimeRequest.getMaterialVersion()))
            {
                query.addCriteria(Criteria.where("item").is(cycleTimeRequest.getMaterial()));
                query.addCriteria(Criteria.where("itemVersion").is(cycleTimeRequest.getMaterialVersion()));
            }

            query.addCriteria(Criteria.where("site").is(cycleTimeRequest.getSite()));
            query.addCriteria(Criteria.where("active").is(1));

          forShopOrder =  mongoTemplate.find(query, CycleTime.class);


        Query query = new Query();
//        if(StringUtils.isNotEmpty(cycleTimeRequest.getShopOrder())) {
//            query.addCriteria(Criteria.where("shopOrder").is(cycleTimeRequest.getShopOrder()));
//        }
        if(StringUtils.isNotEmpty(cycleTimeRequest.getResource()))
        {
            query.addCriteria(Criteria.where("resource").is(cycleTimeRequest.getResource()));
        }
        if(StringUtils.isNotEmpty(cycleTimeRequest.getRouting()) && StringUtils.isNotEmpty(cycleTimeRequest.getRoutingVersion()))
        {
            query.addCriteria(Criteria.where("routing").is(cycleTimeRequest.getRouting()));
            query.addCriteria(Criteria.where("routingVersion").is(cycleTimeRequest.getRoutingVersion()));
        }
        if(StringUtils.isNotEmpty(cycleTimeRequest.getOperation()) && StringUtils.isNotEmpty(cycleTimeRequest.getOperationVersion()))
        {
            query.addCriteria(Criteria.where("operation").is(cycleTimeRequest.getOperation()));
            query.addCriteria(Criteria.where("operationVersion").is(cycleTimeRequest.getOperationVersion()));
        }
        if(StringUtils.isNotEmpty(cycleTimeRequest.getMaterial()) && StringUtils.isNotEmpty(cycleTimeRequest.getMaterialVersion()))
        {
            query.addCriteria(Criteria.where("item").is(cycleTimeRequest.getMaterial()));
            query.addCriteria(Criteria.where("itemVersion").is(cycleTimeRequest.getMaterialVersion()));
        }
        query.addCriteria(Criteria.where("site").is(cycleTimeRequest.getSite()));
        query.addCriteria(Criteria.where("active").is(1));

//        query.with(Sort.by(Sort.Direction.ASC,"priority"));
        List<CycleTime> retrievedList = new ArrayList<>();

        retrievedList = mongoTemplate.find(query, CycleTime.class);

        if(cycleTimeRequest !=null)
        {
            cycleTimeRequest.setMaterial(cycleTimeRequest.getMaterial() == null ? "" : cycleTimeRequest.getMaterial());
            cycleTimeRequest.setMaterialVersion(cycleTimeRequest.getMaterialVersion() == null ? "" : cycleTimeRequest.getMaterialVersion());
            cycleTimeRequest.setRouting(cycleTimeRequest.getRouting() == null ? "" : cycleTimeRequest.getRouting());
            cycleTimeRequest.setRoutingVersion(cycleTimeRequest.getRoutingVersion() == null ? "" : cycleTimeRequest.getRoutingVersion());
            cycleTimeRequest.setOperation(cycleTimeRequest.getOperation() == null ? "" : cycleTimeRequest.getOperation());
            cycleTimeRequest.setOperationVersion(cycleTimeRequest.getOperationVersion() == null ? "" : cycleTimeRequest.getOperationVersion());
            cycleTimeRequest.setWorkCenter(cycleTimeRequest.getWorkCenter() == null ? "" : cycleTimeRequest.getWorkCenter());
            cycleTimeRequest.setResource(cycleTimeRequest.getResource() == null ? "" : cycleTimeRequest.getResource());
            cycleTimeRequest.setShopOrder(cycleTimeRequest.getShopOrder() == null ? "" : cycleTimeRequest.getShopOrder());
        }
        if (!cycleTimeRequest.getMaterial().isEmpty()) {
            if (!cycleTimeRequest.getMaterialVersion().isEmpty()) {
                cycleTimeRequest.setMaterial(cycleTimeRequest.getMaterial() + "/" + cycleTimeRequest.getMaterialVersion());
            }
        }
        if (!cycleTimeRequest.getRouting().isEmpty()) {
            if (!cycleTimeRequest.getRoutingVersion().isEmpty()) {
                cycleTimeRequest.setRouting(cycleTimeRequest.getRouting() + "/" + cycleTimeRequest.getRoutingVersion());
            }
        }
        if (!cycleTimeRequest.getOperation().isEmpty()) {
            if (!cycleTimeRequest.getOperationVersion().isEmpty()) {
                cycleTimeRequest.setOperation(cycleTimeRequest.getOperation() + "/" + cycleTimeRequest.getOperationVersion());
            }
        }
        Attachment attachment = Attachment.builder().item(cycleTimeRequest.getMaterial()).workCenter(cycleTimeRequest.getWorkCenter()).operation(cycleTimeRequest.getOperation()).resource(cycleTimeRequest.getResource()).shopOrder(cycleTimeRequest.getShopOrder()).routing(cycleTimeRequest.getRouting()).build();
        List<AttachmentPoint> attachmentPoints = new ArrayList<>();
        AttachmentPoint createAttachmentPoint = AttachmentPoint.builder().attachmentList(createFields(attachment)).build();
        attachmentPoints.add(createAttachmentPoint);

        List<String> selectedFields = attachmentPoints.stream()
                .flatMap(attachmentPoint -> attachmentPoint.getAttachmentList().entrySet().stream()
                        .filter(entry -> !entry.getValue().isEmpty())
                        .map(entry -> entry.getKey() + "_" + entry.getValue()))
                .collect(Collectors.toList());

        List<String> combinations = generateCombinations(selectedFields);
        combinations.remove(0);
//        Tag createdTag = createTag(cycleTimeRequest);
        MatchOperation match1 = Aggregation.match(
                Criteria.where("tag").in(combinations).and("active").is(1).and("site").is(cycleTimeRequest.getSite())
        );
        SortOperation sortByPriorityAsc = Aggregation.sort(Sort.by(Sort.Direction.ASC, "priority"));
        Aggregation aggregation = Aggregation.newAggregation(
                match1
        );
        AggregationResults<CycleTime> result = mongoTemplate.aggregate(aggregation, CycleTime.class, CycleTime.class);

        List<CycleTime> aggregatedResult = result.getMappedResults();



        retrievedList.addAll(forShopOrder);
        retrievedList.addAll(aggregatedResult);
        List<CycleTime> cycleTimes = retrievedList.stream().distinct().collect(Collectors.toList());

        Collections.sort(cycleTimes,Comparator.comparingInt(CycleTime::getPriority));

        return cycleTimes.get(0);

    }*/

}