package com.rits.worklistservice.service;

import com.rits.Utility.BOConverter;
import com.rits.batchnodoneservice.model.BatchNoDone;
import com.rits.batchnodoneservice.service.BatchNoDoneServiceImpl;
import com.rits.batchnohold.model.BatchNoHold;
import com.rits.batchnohold.service.BatchNoHoldServiceImpl;
import com.rits.batchnoinqueue.dto.BatchNoInQueueRequest;
import com.rits.batchnoinqueue.model.BatchNoInQueue;
import com.rits.batchnoinqueue.service.BatchNoInQueueServiceImpl;
import com.rits.batchnoinwork.dto.BatchNoInWorkRequest;
import com.rits.batchnoinwork.model.BatchNoInWork;
import com.rits.batchnoinwork.service.BatchNoInWorkServiceImpl;
import com.rits.bomheaderservice.dto.BomComponentList;
import com.rits.bomheaderservice.model.BomComponent;
import com.rits.pcuheaderservice.model.PcuHeader;
import com.rits.pcurouterheaderservice.dto.PcuRouterHeaderRequest;
import com.rits.pcurouterheaderservice.model.PcuRouterHeader;
import com.rits.pcurouterheaderservice.model.RoutingStep;
import com.rits.processorderservice.model.ProcessOrder;
import com.rits.processorderservice.service.ProcessOrderServiceImpl;
import com.rits.worklistservice.dto.*;
import com.rits.worklistservice.exception.WorkListException;
import com.rits.worklistservice.model.WorkList;
import com.rits.worklistservice.repository.WorkListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class WorkListServiceImpl implements WorkListService {
    private final WebClient.Builder webClientBuilder;
    private final WorkListRepository workListRepository;

    @Autowired
    private BatchNoInQueueServiceImpl batchNoInQueueService;

    @Autowired
    private ProcessOrderServiceImpl processOrderService;

    @Autowired
    private BatchNoInWorkServiceImpl batchNoInWorkService;

    @Autowired
    private BatchNoHoldServiceImpl batchNoHoldService;
    @Value("${listmaintenance-service.url}/retrieve")
    private String listMaintenanceRetrieveUrl;
    @Value("${pcurouterheader-service.uri}/retrieve")
    private String pcuRouterHeaderRetrieveUrl;
    @Value("${pcuheader-service.url}/readPcu")
    private String readPcuUrl;
    @Value("${pcuinqueue-service.url}/retrieveListOfPcuBO")
    private String pcuInQueueRetrieveUrl;
    @Value("${start-service.url}/retrieveByOperationAndResource")
    private String pcuInWorkRetrieveUrl;
    @Value("${pcudone-service.url}/retrieveListOfPcuBO")
    private String pcuDoneRetrieveUrl;
    @Value("${shoporder-service.url}/retrieve")
    private String shopOrderRetrieveUrl;
    @Value("${operation-service.url}/retrieve")
    private String retrieveOperationUrl;

    @Value("${operation-service.url}/retrieveOperationByCurrentVersion")
    private String retrieveOperationByCurrentVersionUrl;
    @Value("${workInstruction-service.url}/getWorkInstructionList")
    private String getWorkInstructionWorkListUrl;
    @Value("${bomheader-service.url}/getComponentList")
    private String getComponentListUrl;

    @Value("${batchnoinqueue-service.url}/getBatchInQueueList")
    private String getBatchInQueueListUrl;

    @Value("${batchnoinwork-service.url}/getBatchInWorkList")
    private String getBatchInWorkListUrl;

    @Value("${processorder-service.url}/retrieve")
    private String retrieveUrl;

    @Value("${activity-service.url}/getByActivityId")
    private String getActivityUrl;

    @Value("${pod-service.url}/retrieve")
    private String getPodUrl;

    @Value("${item-service.url}/retrieve")
    private String itemServiceUrl;
    ListMaintenance listResponse;

    public PcuInQueue boCreator(PcuInQueueWithoutBO pcuInQueueWithoutBO) {

        PcuInQueue pcuInQueue = new PcuInQueue();

        if(pcuInQueueWithoutBO.getSite() != null && !pcuInQueueWithoutBO.getSite().isEmpty()) {

            String siteVar = pcuInQueueWithoutBO.getSite();

            if(pcuInQueueWithoutBO.getUser() != null && !pcuInQueueWithoutBO.getUser().isEmpty())
                pcuInQueue.setUserBO(BOConverter.retrieveUserBO(siteVar, pcuInQueueWithoutBO.getUser()));

            if(pcuInQueueWithoutBO.getPcu() != null && !pcuInQueueWithoutBO.getPcu().isEmpty())
                pcuInQueue.setPcuBO(BOConverter.retrievePcuBO(siteVar, pcuInQueueWithoutBO.getPcu()));

            if(pcuInQueueWithoutBO.getItem() != null && !pcuInQueueWithoutBO.getItem().isEmpty()) {
                pcuInQueue.setItemBO(BOConverter.retrieveItemBO(siteVar, pcuInQueueWithoutBO.getItem(), pcuInQueueWithoutBO.getItemVersion()));
            }

            if(pcuInQueueWithoutBO.getRouter() != null && !pcuInQueueWithoutBO.getRouter().isEmpty()) {
                pcuInQueue.setRouterBO(BOConverter.retrieveRouterBO(siteVar, pcuInQueueWithoutBO.getRouter(), pcuInQueueWithoutBO.getRouterVersion()));
            }

            if(pcuInQueueWithoutBO.getOperation() != null && !pcuInQueueWithoutBO.getOperation().isEmpty()) {
                pcuInQueue.setOperationBO(BOConverter.retrieveOperationBO(siteVar, pcuInQueueWithoutBO.getOperation(), pcuInQueueWithoutBO.getOperationVersion()));
            }

            if(pcuInQueueWithoutBO.getResource() != null && !pcuInQueueWithoutBO.getResource().isEmpty())
                pcuInQueue.setResourceBO(BOConverter.retriveResourceBO(siteVar, pcuInQueueWithoutBO.getResource()));

            if(pcuInQueueWithoutBO.getShopOrder() != null && !pcuInQueueWithoutBO.getShopOrder().isEmpty())
                pcuInQueue.setShopOrderBO(BOConverter.retrieveShopOrderBO(siteVar, pcuInQueueWithoutBO.getShopOrder()));

        }
        return pcuInQueue;
    }

    @Override
    public WorkListRequest convertToWorkListRequest(WorkListRequestNoBO workListRequestNoBO) {
        WorkListRequest request = new WorkListRequest();

        request.setSite(workListRequestNoBO.getSite());
        request.setList(workListRequestNoBO.getList());
        request.setCategory(workListRequestNoBO.getCategory());
        request.setItem(workListRequestNoBO.getItem());
        request.setItemGroup(workListRequestNoBO.getItemGroup());
        request.setItemVersion(workListRequestNoBO.getItemVersion());
        request.setRouting(workListRequestNoBO.getRouting());
        request.setRoutingVersion(workListRequestNoBO.getRoutingVersion());
        request.setResourceType(workListRequestNoBO.getResourceType());
        request.setCustomerOrder(workListRequestNoBO.getCustomerOrder());
        request.setShopOrder(workListRequestNoBO.getShopOrder());
        request.setBom(workListRequestNoBO.getBom());
        request.setBomVersion(workListRequestNoBO.getBomVersion());
        request.setComponent(workListRequestNoBO.getComponent());
        request.setComponentVersion(workListRequestNoBO.getComponentVersion());
        request.setPodName(workListRequestNoBO.getPodName());
        if (workListRequestNoBO.getPcu() != null) {
            request.setPcuBO(BOConverter.retrievePcuBO(workListRequestNoBO.getSite(),workListRequestNoBO.getPcu()));
        }

        if (workListRequestNoBO.getResource() != null) {
            String resource = workListRequestNoBO.getResource();
            request.setResourceBO(BOConverter.retriveResourceBO(workListRequestNoBO.getSite(),resource));

        }

        if (workListRequestNoBO.getWorkCenter() != null) {
            request.setWorkCenterBO(BOConverter.retrieveWorkCenterBO(workListRequestNoBO.getSite(),workListRequestNoBO.getWorkCenter()));
        }

        if (workListRequestNoBO.getOperation() != null) {
            String operation = workListRequestNoBO.getOperation();
            request.setOperationBO(BOConverter.retrieveOperationBO(workListRequestNoBO.getSite(),operation, workListRequestNoBO.getOperationVersion()));
        }
        request.setResource(workListRequestNoBO.getResource());
        request.setPhase(workListRequestNoBO.getPhase());
        return request;
    }

    @Override
    public List<WorkListResponse> getWorkList(WorkListRequest workListRequest) throws Exception {
        workListRequest.setResourceBO("");
        boolean isPcu = false;
        List<WorkListResponse> workLists = new ArrayList<>();
        List<WorkListResponse> tempWorkLists = new ArrayList<>();
        listResponse = webClientBuilder.build()
                .post()
                .uri(listMaintenanceRetrieveUrl)
                .bodyValue(workListRequest)
                .retrieve()
                .bodyToMono(ListMaintenance.class)
                .block();

        if (listResponse == null||listResponse.getHandle()==null) {
            throw new WorkListException(3702, workListRequest.getList());
        }

        int maxRows = StringUtils.hasText(listResponse.getMaximumNumberOfRow()) ? Integer.parseInt(listResponse.getMaximumNumberOfRow()) : 0;
        Column statusColumn = null;
        List<WorkList> actualColumnList = workListRepository.findByPreDefinedFieldGroupContainingIgnoreCase(listResponse.getCategory());
        List<WorkList> findAll=workListRepository.findAll();
        List<String> listColumnName = new ArrayList<>();
        List<ColumnList> combinedColumnList = new ArrayList<>();
        if(listResponse.getColumnList() != null || !listResponse.getColumnList().isEmpty()) {
            for (Column column : listResponse.getColumnList()) {
                listColumnName.add(column.getColumnName());
                if (column.getColumnName().equalsIgnoreCase("Status")) {
                    statusColumn = column;
                }
                if (column.getColumnName().equalsIgnoreCase("pcu")) {
                    isPcu = true;
                }
            }
        }
        for (String column : listColumnName) {
            for (WorkList workList : actualColumnList) {
                if (column.equalsIgnoreCase(workList.getFieldName())) {
                    ColumnList columnList = new ColumnList();
                    columnList.setDataField(column);
                    columnList.setDataAttribute(workList.getFieldValue());
                    combinedColumnList.add(columnList);
                }
            }
        }

        List<ColumnList> finalColumnList = new ArrayList<>();
        List<Object> allRawData = new ArrayList<>();
        List<Object> allBatchRawData = new ArrayList<>();
        for (ColumnList columnList : combinedColumnList) {
            String combinedFieldName = columnList.getDataField();
            boolean isFound = false;

            for (ColumnList list : finalColumnList) {
                if (list.getDataAttribute()!=null && list.getDataAttribute().equalsIgnoreCase(columnList.getDataAttribute())) {
                    list.setDataField(list.getDataField() + "," + combinedFieldName);
                    isFound = true;
                    break;
                }
            }

            if (!isFound) {
                finalColumnList.add(columnList);
            }
        }

        if (!finalColumnList.isEmpty()) {


            PcuHeader pcuHeader = null;
            for (ColumnList columnList : finalColumnList) {
                if (columnList.getDataAttribute() != null) {
                    if (columnList.getDataAttribute()!=null && columnList.getDataAttribute().equalsIgnoreCase("Assembly")) {
                        pcuHeader = getPcuHeader(workListRequest);
                        if (pcuHeader == null || pcuHeader.getPcuBO() == null || pcuHeader.getPcuBO().isEmpty()) {
                            throw new WorkListException(4102, workListRequest.getPcuBO());
                        }
                        BomComponentList bomComponentList = getBomComponentList(pcuHeader, workListRequest);
                        String assemblyFieldName[] = columnList.getDataField().split(",");
                        PcuRouterHeaderRequest pcuRouterHeaderRequest = PcuRouterHeaderRequest.builder().site(workListRequest.getSite()).pcuBo(pcuHeader.getPcuBO()).build();
                        PcuRouterHeader pcuRouterHeader = webClientBuilder.build()
                                .post()
                                .uri(pcuRouterHeaderRetrieveUrl)
                                .bodyValue(pcuRouterHeaderRequest)
                                .retrieve()
                                .bodyToMono(PcuRouterHeader.class)
                                .block();

                        if (pcuRouterHeader == null || pcuRouterHeader.getHandle() == null) {
                            throw new WorkListException(3703, workListRequest.getList());
                        }


                        tempWorkLists = setBomComponentValues(bomComponentList, assemblyFieldName, workListRequest, pcuRouterHeader);
                        allRawData.add(bomComponentList);

                    }
                    if (columnList.getDataAttribute()!=null && columnList.getDataAttribute().equalsIgnoreCase("WorkInstruction")) {

                        pcuHeader = getPcuHeader(workListRequest);
                        if (pcuHeader == null || pcuHeader.getPcuBO() == null || pcuHeader.getPcuBO().isEmpty()) {
                            if (workListRequest.getPcuBO() == null || workListRequest.getPcuBO().isEmpty()) {
                                throw new WorkListException(4102, workListRequest.getPcuBO());
                            }
                        }
                        tempWorkLists = getWorkInstructionWorkList(workListRequest);

                    }

                    if(StringUtils.hasText(workListRequest.getOperationBO())) {
                        String opearation = BOConverter.getOperation(workListRequest.getOperationBO());

                        Operation oper = Operation.builder().site(workListRequest.getSite()).operation(opearation).build();
                        Operation operVersion = webClientBuilder.build()
                                .post()
                                .uri(retrieveOperationByCurrentVersionUrl)
                                .bodyValue(oper)
                                .retrieve()
                                .bodyToMono(Operation.class)
                                .block();

                        if (columnList.getDataAttribute().equalsIgnoreCase("WorkList")) {

                            PcuInQueueWithoutBO pcuCommon = PcuInQueueWithoutBO.builder()
                                    .site(workListRequest.getSite())
                                    .operation(operVersion.getOperation())
                                    .operationVersion(operVersion.getRevision())
                                    .recordLimit(maxRows)
                                    .build();

                            if(isPcu) {
                                List<PcuInQueue> pcuInQueueList = new ArrayList<>();
                                List<PcuInQueue> pcuInWorkList = new ArrayList<>();
                                if (statusColumn != null && statusColumn.getDetails() != null) {

                                    if (statusColumn.getDetails() != null && !statusColumn.getDetails().getStatusList().isEmpty()) {
                                        boolean hasInQueue = statusColumn.getDetails().getStatusList().stream()
                                                .anyMatch(detail -> detail.getStatus().equalsIgnoreCase("in Queue"));

                                        if (hasInQueue) {
                                            List<PcuInQueueWithoutBO> inQueueResponse = webClientBuilder.build()
                                                    .post()
                                                    .uri(pcuInQueueRetrieveUrl)
                                                    .bodyValue(pcuCommon)
                                                    .retrieve()
                                                    .bodyToMono(new ParameterizedTypeReference<List<PcuInQueueWithoutBO>>() {
                                                    })
                                                    .block();

                                            PcuInQueue pcuInQueue = null;

                                            for (PcuInQueueWithoutBO pcuInQueueWithoutBO : inQueueResponse) {
                                                pcuInQueue = new PcuInQueue();
                                                pcuInQueue = boCreator(pcuInQueueWithoutBO);
                                                pcuInQueue.setSite(pcuInQueueWithoutBO.getSite());
//                                                pcuInQueue.setHandle(pcuInQueueWithoutBO.getHandle());
                                                pcuInQueue.setDateTime(pcuInQueueWithoutBO.getDateTime());
                                                pcuInQueue.setQtyInQueue(pcuInQueueWithoutBO.getQtyInQueue());
                                                pcuInQueue.setType(pcuInQueueWithoutBO.getType());
//                                                pcuInQueue.setParentStepID(pcuInQueueWithoutBO.getParentStepID());
//                                                pcuInQueue.setActive(pcuInQueueWithoutBO.getActive());

                                                pcuInQueueList.add(pcuInQueue);
                                                pcuInQueue = null;
                                            }

                                            if (inQueueResponse == null) {
                                                throw new WorkListException(3602);
                                            }

//                                            tempWorkLists.addAll(setValues(pcuInQueueList));
//                                            allRawData.addAll(inQueueResponse);
                                        }

                                        boolean hasWork = statusColumn.getDetails().getStatusList().stream()
                                                .anyMatch(detail -> detail.getStatus().equalsIgnoreCase("active"));


                                        if (hasWork) {
                                            List<PcuInQueueWithoutBO> inWorkResponse = webClientBuilder.build()
                                                    .post()
                                                    .uri(pcuInWorkRetrieveUrl)
                                                    .bodyValue(pcuCommon)
                                                    .retrieve()
                                                    .bodyToMono(new ParameterizedTypeReference<List<PcuInQueueWithoutBO>>() {
                                                    })
                                                    .block();

                                            PcuInWork pcuInWork = null;
                                            PcuInQueue pcuInQueue = null;

                                            for (PcuInQueueWithoutBO pcuInQueueWithoutBO : inWorkResponse) {
                                                pcuInQueue = boCreator(pcuInQueueWithoutBO);

//                                                pcuInWork = new PcuInWork(pcuInQueue);
                                                pcuInQueue.setSite(pcuInQueueWithoutBO.getSite());
//                                                pcuInWork.setHandle(pcuInQueueWithoutBO.getHandle());
                                                pcuInQueue.setDateTime(pcuInQueueWithoutBO.getCreatedDateTime());
                                                pcuInQueue.setQtyInWork(pcuInQueueWithoutBO.getQtyInWork());
                                                pcuInQueue.setType(pcuInQueueWithoutBO.getType());
//                                                pcuInWork.setParentStepID(pcuInQueueWithoutBO.getParentStepID());
//                                                pcuInWork.setActive(pcuInQueueWithoutBO.getActive());
//                                                pcuInWork.setCreatedDateTime(pcuInQueueWithoutBO.getCreatedDateTime());
//                                                pcuInWork.setModifiedDateTime(pcuInQueueWithoutBO.getModifiedDateTime());

                                                pcuInWorkList.add(pcuInQueue);
                                                pcuInWork = null;
                                                pcuInQueue = null;
                                            }

                                            if (inWorkResponse == null) {
                                                throw new WorkListException(3600);
                                            }

//                                            tempWorkLists.addAll(setValuesOfPcuInWork(pcuInWorkList));
//                                            allRawData.addAll(inWorkResponse);
                                        }

//                                        boolean hasDone = statusColumn.getDetails().getStatusList().stream()
//                                                .anyMatch(detail -> detail.getStatus().equalsIgnoreCase("done"));
//
//                                        if (hasDone) {
//                                            List<PcuInQueueWithoutBO> pcuInQueueRes = webClientBuilder.build()
//                                                    .post()
//                                                    .uri(pcuInWorkRetrieveUrl)
//                                                    .bodyValue(pcuCommon)
//                                                    .retrieve()
//                                                    .bodyToMono(new ParameterizedTypeReference<List<PcuInQueueWithoutBO>>() {
//                                                    })
//                                                    .block();
//
//                                            PcuDone pcuDone = null;
//                                            PcuInQueue pcuInQueue = null;
//                                            List<PcuDone> pcuDoneList = new ArrayList<>();
//
//                                            for (PcuInQueueWithoutBO pcuInQueueWithoutBO : pcuInQueueRes) {
//                                                pcuDone = new PcuDone();
//                                                pcuInQueue = boCreator(pcuInQueueWithoutBO);
//                                                pcuDone = new PcuDone(pcuInQueue);
//                                                pcuDone.setSite(pcuInQueueWithoutBO.getSite());
////                                                pcuDone.setHandle(pcuInQueueWithoutBO.getHandle());
////                                                pcuDone.setDateTime(pcuInQueueWithoutBO.getDateTime());
////                                                pcuDone.setActive(pcuInQueueWithoutBO.getActive());
//                                                pcuDone.setQtyDone(pcuInQueueWithoutBO.getQtyInWork());
//
//                                                pcuDoneList.add(pcuDone);
//                                            }
//
//                                            if (pcuInQueueRes == null) {
//                                                throw new WorkListException(3601);
//                                            }
//                                            tempWorkLists.addAll(setValuesOfPcuDone(pcuDoneList));
//                                            allRawData.addAll(pcuInQueueRes);
//                                        }
                                    }
                                } else {
                                    List<PcuInQueueWithoutBO> inQueueResponse = webClientBuilder.build()
                                            .post()
                                            .uri(pcuInQueueRetrieveUrl)
                                            .bodyValue(pcuCommon)
                                            .retrieve()
                                            .bodyToMono(new ParameterizedTypeReference<List<PcuInQueueWithoutBO>>() {
                                            })
                                            .block();

                                    PcuInQueue pcuInQueue = null;
//                                    List<PcuInQueue> pcuInQueueList = new ArrayList<>();
                                    for (PcuInQueueWithoutBO pcuInQueueWithoutBO : inQueueResponse) {
                                        pcuInQueue = new PcuInQueue();
//                                        PcuInWorkWithoutBO pcuInWorkBO = new PcuInWorkWithoutBO(pcuInQueueWithoutBO);
                                        pcuInQueue = boCreator(pcuInQueueWithoutBO);
                                        pcuInQueue.setSite(pcuInQueueWithoutBO.getSite());
//                                        pcuInQueue.setHandle(pcuInQueueWithoutBO.getHandle());
                                        pcuInQueue.setDateTime(pcuInQueueWithoutBO.getDateTime());
                                        pcuInQueue.setQtyInQueue(pcuInQueueWithoutBO.getQtyInQueue());
                                        pcuInQueue.setType(pcuInQueueWithoutBO.getType());
//                                        pcuInQueue.setParentStepID(pcuInQueueWithoutBO.getParentStepID());
//                                        pcuInQueue.setActive(pcuInQueueWithoutBO.getActive());

                                        pcuInQueueList.add(pcuInQueue);
                                        pcuInQueue = null;
                                    }

                                    List<PcuInQueueWithoutBO> inWorkResponse = webClientBuilder.build()
                                            .post()
                                            .uri(pcuInWorkRetrieveUrl)
                                            .bodyValue(pcuCommon)
                                            .retrieve()
                                            .bodyToMono(new ParameterizedTypeReference<List<PcuInQueueWithoutBO>>() {
                                            })
                                            .block();

                                    PcuInWork pcuInWork = null;
                                    PcuInQueue pcuInQueue1 = null;
//                                    List<PcuInQueue> pcuInWorkList = new ArrayList<>();
                                    for (PcuInQueueWithoutBO pcuInQueueWithoutBO : inWorkResponse) {
                                        pcuInQueue1 = boCreator(pcuInQueueWithoutBO);

                                        pcuInWork = new PcuInWork(pcuInQueue1);
                                        pcuInQueue1.setSite(pcuInQueueWithoutBO.getSite());
//                                        pcuInWork.setHandle(pcuInQueueWithoutBO.getHandle());
                                        pcuInQueue1.setDateTime(pcuInQueueWithoutBO.getCreatedDateTime());
                                        pcuInQueue1.setQtyInWork(pcuInQueueWithoutBO.getQtyInWork());
                                        pcuInQueue1.setType(pcuInQueueWithoutBO.getType());
//                                        pcuInWork.setParentStepID(pcuInQueueWithoutBO.getParentStepID());
//                                        pcuInWork.setActive(pcuInQueueWithoutBO.getActive());
//                                        pcuInWork.setCreatedDateTime(pcuInQueueWithoutBO.getCreatedDateTime());
//                                        pcuInWork.setModifiedDateTime(pcuInQueueWithoutBO.getModifiedDateTime());

                                        pcuInWorkList.add(pcuInQueue1);
                                        pcuInWork = null;
                                        pcuInQueue1 = null;
                                    }

//                                    List<PcuInQueueWithoutBO> doneResponse = webClientBuilder.build()
//                                            .post()
//                                            .uri(pcuInWorkRetrieveUrl)
//                                            .bodyValue(pcuCommon)
//                                            .retrieve()
//                                            .bodyToMono(new ParameterizedTypeReference<List<PcuInQueueWithoutBO>>() {
//                                            })
//                                            .block();
//                                    List<PcuDone> pcuDoneList = new ArrayList<>();
//
//                                    PcuDone pcuDone = null;
//                                    PcuInQueue pcuInQueue2 = null;
//                                    for (PcuInQueueWithoutBO pcuInQueueWithoutBO : doneResponse) {
//                                        pcuDone = new PcuDone();
//                                        pcuInQueue2 = boCreator(pcuInQueueWithoutBO);
//                                        pcuDone = new PcuDone(pcuInQueue2);
//                                        pcuDone.setSite(pcuInQueueWithoutBO.getSite());
////                                        pcuDone.setHandle(pcuInQueueWithoutBO.getHandle());
////                                        pcuDone.setDateTime(pcuInQueueWithoutBO.getDateTime());
////                                        pcuDone.setActive(pcuInQueueWithoutBO.getActive());
//                                        pcuDone.setQtyDone(pcuInQueueWithoutBO.getQtyInWork());
//
//                                        pcuDoneList.add(pcuDone);
//                                    }

//                                    if (inQueueResponse != null) {
//                                        tempWorkLists.addAll(setValues(pcuInQueueList));
//                                        allRawData.addAll(inQueueResponse); // Add raw data to the combined list
//                                    }
//
//                                    if (inWorkResponse != null) {
//                                        tempWorkLists.addAll(setValuesOfPcuInWork(pcuInWorkList));
//                                        allRawData.addAll(inWorkResponse); // Add raw data to the combined list
//                                    }

//                                    if (doneResponse != null) {
//                                        tempWorkLists.addAll(setValuesOfPcuDone(pcuDoneList));
//                                        allRawData.addAll(doneResponse); // Add raw data to the combined list
//                                    }
                                }
                                List<Object> combinedList = new ArrayList<>();
                                combinedList.addAll(pcuInQueueList);
                                combinedList.addAll(pcuInWorkList);

                                combinedList.sort((o1, o2) -> {
                                    LocalDateTime createdDateTime1 = ((PcuInQueue) o1).getDateTime();
                                    LocalDateTime createdDateTime2 = ((PcuInQueue) o2).getDateTime();
                                    return createdDateTime2.compareTo(createdDateTime1); // Descending order
                                });

                                // Limit to maxRows
                                combinedList = combinedList.stream().limit(maxRows).collect(Collectors.toList());

                                // Separate records based on type
                                List<PcuInQueue> inQueues = new ArrayList<>();
                                List<PcuInQueue> inWorks = new ArrayList<>();
                                List<PcuInQueue> dones = new ArrayList<>();

                                for (Object obj : combinedList) {
                                    PcuInQueue item = (PcuInQueue) obj;
                                    switch (item.getType()) {
                                        case "inqueue":
                                            inQueues.add(item);
                                            break;
                                        case "inwork":
                                            inWorks.add(item);
                                            break;
                                        case "done":
                                            dones.add(item);
                                            break;
                                        default:
                                            break;
                                    }
                                }

                                if (!inQueues.isEmpty()) {
                                    tempWorkLists.addAll(setValues(inQueues));
                                    allRawData.addAll(inQueues);
                                }

                                if (!inWorks.isEmpty()) {
                                    tempWorkLists.addAll(setValuesOfPcuInWork(inWorks));
                                    allRawData.addAll(inWorks);
                                }

                                if (!dones.isEmpty()) {
                                    // Logic for done pending
                                }

                            } else {

                                BatchResponse batchResponse = getBatchResponse(workListRequest, statusColumn, operVersion, maxRows);
                                tempWorkLists.addAll(batchResponse.getTempWorkLists());
                                allBatchRawData.addAll(batchResponse.getAllRawData());
                            }
                        }
                    }

//                    Map<String, ProcessOrder> processOrderMap = new HashMap<>();
//                    if (columnList.getDataAttribute().equals("ProcessOrder")) {
//                        String[] processOrderFieldNames = columnList.getDataField().split(",");
//
//                        for (Object rawData : allBatchRawData) {
//                            String batchNo = getRecipeBatch(rawData);
//                            String orderNo = getProecssOrder(rawData);
//
//                            if (batchNo != null && orderNo != null) {
//                                // Check if processOrder object already exists in the map
//                                ProcessOrder processOrder = processOrderMap.get(batchNo);
//
//
//                                // If not, make a new request and store it in the map
//                                if (processOrder == null) {
//                                    String[] orderNumber = orderNo.split(",");
//                                    processOrder = processOrderService.retrieveProcessOrder(listResponse.getSite(), orderNumber[1]);
//
//                                    processOrderMap.put(batchNo, processOrder);
//                                }
//
//                                // Process ShopOrder and add details to the response
//                                List<ColumnList> processOrderDetails = processOrder(processOrder, processOrderFieldNames);
//                                if (!processOrderDetails.isEmpty()) {
//                                    for (WorkListResponse workListResponse : tempWorkLists) {
//                                        if (workListResponse.getColumnLists().stream().anyMatch(list -> list.getDataAttribute().equals(batchNo))) {
//                                            workListResponse.getColumnLists().addAll(processOrderDetails);
//                                            break;
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }

                    if (columnList.getDataAttribute().equalsIgnoreCase("operation")) {

                        String operationFieldName[] = columnList.getDataField().split(",");
                        String operation1[] = workListRequest.getOperationBO().split(",");
                        OperationRequest operationRequest = OperationRequest.builder().site(workListRequest.getSite()).operation(operation1[1]).version(operation1[2]).build();
                        Operation operationResponse = webClientBuilder.build()
                                .post()
                                .uri(retrieveOperationUrl)
                                .bodyValue(operationRequest)
                                .retrieve()
                                .bodyToMono(Operation.class)
                                .block();
                        if (operationResponse == null || operationResponse.getOperation() == null) {
                            throw new WorkListException(1000, operation1[1], operation1[2]);
                        }
                        List<WorkListResponse> operationColumnList = setOperationValues(operationResponse, operationFieldName);
                        for (WorkListResponse tempWorkList : tempWorkLists) {
                            tempWorkList.getColumnLists().addAll(operationColumnList.get(0).getColumnLists());
                        }


                    }
                    if (columnList.getDataAttribute().equalsIgnoreCase("item")) {

                        pcuHeader = getPcuHeader(workListRequest);
                        if (pcuHeader == null || pcuHeader.getPcuBO() == null || pcuHeader.getPcuBO().isEmpty()) {
                            throw new WorkListException(4102, workListRequest.getPcuBO());
                        }
                        String[] itemBo = pcuHeader.getItemBO().split(",");
                        String item = itemBo[1];
                        Item retrieveItem = retrieveItem(pcuHeader);
                        String itemFieldName[] = columnList.getDataField().split(",");
                        List<WorkListResponse> itemCOlumnList = setItemValue(retrieveItem, itemFieldName);
                        for (WorkListResponse tempWorkList : tempWorkLists) {
                            tempWorkList.getColumnLists().addAll(itemCOlumnList.get(0).getColumnLists());
                        }

                    }

                    Map<String, ShopOrder> shopOrderMap = new HashMap<>();

                    if (columnList.getDataAttribute().equals("R_SHOP_ORDER")) {
                        String[] shopOrderFieldNames = columnList.getDataField().split(",");

                        for (Object rawData : allRawData) {
                            String pcuBO = getPcuBO(rawData);
                            String shopOrderBO = getShopOrderBO(rawData);

                            if (pcuBO != null && shopOrderBO != null) {
                                ShopOrder shopOrderData = shopOrderMap.get(pcuBO);

                                if (shopOrderData == null) {
                                    String[] shopOrder = shopOrderBO.split(",");
                                    ShopOrderRequest shopOrderRequest = ShopOrderRequest.builder()
                                            .site(listResponse.getSite())
                                            .shopOrder(shopOrder[1])
                                            .build();

                                    shopOrderData = webClientBuilder.build()
                                            .post()
                                            .uri(shopOrderRetrieveUrl)
                                            .bodyValue(shopOrderRequest)
                                            .retrieve()
                                            .bodyToMono(ShopOrder.class)
                                            .block();

                                    shopOrderMap.put(pcuBO, shopOrderData);
                                }

                                List<ColumnList> shopOrderDetails = processShopOrder(shopOrderData, shopOrderFieldNames);
                                if (!shopOrderDetails.isEmpty()) {
                                    for (WorkListResponse workListResponse : tempWorkLists) {
                                        if (workListResponse.getColumnLists().stream().anyMatch(list -> list.getDataAttribute().equals(pcuBO))) {
                                            workListResponse.getColumnLists().addAll(shopOrderDetails);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        workLists.addAll(tempWorkLists);

        workLists=processFinalWorkList(workLists);
        for (WorkListResponse workListResponse : workLists) {
            List<ColumnList> columnLists = workListResponse.getColumnLists();
            for (ColumnList columnList : columnLists) {
                String dataField = columnList.getDataField();
                columnList.setDataField(dataField.replace("BO", "").trim());

                String dataAttribute = columnList.getDataAttribute();
                if (dataAttribute.contains(",")) {
                    String[] parts = dataAttribute.split(",");
                    String formattedDataAttribute = Arrays.stream(parts, 1, parts.length)
                            .map(String::trim)
                            .collect(Collectors.joining("/"));
                    columnList.setDataAttribute(formattedDataAttribute);
                }
            }
        }
        return workLists;
    }

    private BatchResponse getBatchResponse(WorkListRequest workListRequest, Column statusColumn, Operation op, int maxRows){

        List<Object> allRawData = new ArrayList<>();
        List<WorkListResponse> tempWorkLists = new ArrayList<>();
        List<BatchNoInQueue> batchNoInQueues = null;
        List<BatchNoInWork> batchNoInWorks = null;
        List<BatchNoHold> batchNoHolds = null;

        try{
            if (statusColumn != null && statusColumn.getDetails() != null && !statusColumn.getDetails().getStatusList().isEmpty()) {

                // inQueue
                boolean hasBatchInQueue = statusColumn.getDetails().getStatusList().stream()
                        .anyMatch(detail -> detail.getStatus().equalsIgnoreCase("In Queue"));

                if (hasBatchInQueue) {
                    batchNoInQueues = getBatchInQueueRecord(workListRequest, op, maxRows);

                    if (batchNoInQueues == null) {
                        throw new WorkListException(3810);
                    }

//                        tempWorkLists.addAll(setBatchInQueueValues(batchNoInQueues));
//                        allRawData.addAll(batchNoInQueues);
                }

                //inWork
                boolean hasBatchInWork = statusColumn.getDetails().getStatusList().stream()
                        .anyMatch(detail -> detail.getStatus().equalsIgnoreCase("Active"));


                if (hasBatchInWork) {
                    batchNoInWorks = getBatchInWorkRecord(workListRequest, op, maxRows);

                    if (batchNoInWorks == null) {
                        throw new WorkListException(3811);
                    }

//                        tempWorkLists.addAll(setBatchInWorkValues(batchNoInWorks));
//                        allRawData.addAll(batchNoInWorks);
                }

                //hold
                boolean hasBatchOnHold = statusColumn.getDetails().getStatusList().stream()
                        .anyMatch(detail -> detail.getStatus().equalsIgnoreCase("hold"));


                if (hasBatchOnHold) {
                    batchNoHolds = getBatchInHoldRecord(workListRequest, op, maxRows);

                    if (batchNoHolds == null) {
                        throw new WorkListException(3813);
                    }

//                        tempWorkLists.addAll(setBatchInWorkValues(batchNoDones));
//                        allRawData.addAll(batchNoInWorks);
                }

            } else {
                // inQueue
                batchNoInQueues = getBatchInQueueRecord(workListRequest, op, maxRows);

//                if (batchNoInQueues != null) {
//                    tempWorkLists.addAll(setBatchInQueueValues(batchNoInQueues));
//                    allRawData.addAll(batchNoInQueues);
//                }

                // inWork
                batchNoInWorks = getBatchInWorkRecord(workListRequest, op, maxRows);

//                if (batchNoInWorks != null) {
//                    tempWorkLists.addAll(setBatchInWorkValues(batchNoInWorks));
//                    allRawData.addAll(batchNoInWorks);
//                }
                // hold
                batchNoHolds = getBatchInHoldRecord(workListRequest, op, maxRows);
            }

            List<Object> combinedList = new ArrayList<>();

            if (batchNoInQueues != null && !batchNoInQueues.isEmpty()) {
                combinedList.addAll(batchNoInQueues);
            }

            if (batchNoInWorks != null && !batchNoInWorks.isEmpty()) {
                combinedList.addAll(
                        batchNoInWorks.stream()
                                .map(BatchNoInQueue::new) // Convert BatchNoInWork to BatchNoInQueue
                                .collect(Collectors.toList())
                );
            }

            if (batchNoHolds != null && !batchNoHolds.isEmpty()) {
                combinedList.addAll(
                        batchNoHolds.stream()
                                .map(BatchNoInQueue::new) // Convert BatchNoInWork to BatchNoInQueue
                                .collect(Collectors.toList())
                );
            }

            if(combinedList != null && !combinedList.isEmpty()) {
                combinedList.sort((o1, o2) -> {
                    LocalDateTime createdDateTime1 = ((BatchNoInQueue) o1).getCreatedDateTime();
                    LocalDateTime createdDateTime2 = ((BatchNoInQueue) o2).getCreatedDateTime();
                    return createdDateTime2.compareTo(createdDateTime1); // Descending order
                });

                // Limit to maxRows
                combinedList = combinedList.stream().limit(maxRows).collect(Collectors.toList());
            }

            // Separate records based on type
            List<BatchNoInQueue> inQueues = new ArrayList<>();
            List<BatchNoInQueue> inWorks = new ArrayList<>();
            List<BatchNoInQueue> holds = new ArrayList<>();

            for (Object obj : combinedList) {
                BatchNoInQueue item = (BatchNoInQueue) obj;
                switch (item.getType()) {
                    case "inqueue":
                        inQueues.add(item);
                        break;
                    case "inwork":
                        inWorks.add(item);
                        break;
                    case "hold":
                        holds.add(item);
                        break;
                    default:
                        break;
                }
            }

            if (!inQueues.isEmpty()) {
                tempWorkLists.addAll(setBatchInQueueValues(inQueues));
                allRawData.addAll(inQueues);
            }

            if (!inWorks.isEmpty()) {
                tempWorkLists.addAll(setBatchInWorkValues(inWorks));
                allRawData.addAll(inWorks);
            }

            if (!holds.isEmpty()) {
                tempWorkLists.addAll(setBatchHoldValues(holds));
                allRawData.addAll(holds);
            }
        } catch (Exception e){
            throw new WorkListException(3812, e.getMessage());
        }

        return new BatchResponse(tempWorkLists, allRawData);
    }

    private List<BatchNoInQueue> getBatchInQueueRecord(WorkListRequest workListRequest, Operation op, int maxRecord) throws Exception{

        PodRequest podRequest = PodRequest.builder()
                .site(workListRequest.getSite())
                .podName(workListRequest.getPodName())
                .build();

        Pod pod= webClientBuilder.build()
                .post()
                .uri(getPodUrl)
                .bodyValue(podRequest)
                .retrieve()
                .bodyToMono(Pod.class)
                .block();

        boolean qualityCheck = false;
        boolean operatorCheck =false;
        if (pod != null && pod.getCustomDataList() != null) {
            for (CustomData customData : pod.getCustomDataList()) {
                if ("QUALITYCHECK".equalsIgnoreCase(customData.getCustomData())
                        && "true".equalsIgnoreCase(customData.getValue())) {
                    qualityCheck = true;
                    break; // Exit the loop once a match is found
                }
                if ("OPERATORCHECK".equalsIgnoreCase(customData.getCustomData())
                        && "true".equalsIgnoreCase(customData.getValue())) {
                    operatorCheck = true;
                    break; // Exit the loop once a match is found
                }
            }
        }

        BatchNoInQueueRequest batchInQueueReq = BatchNoInQueueRequest.builder()
                .site(workListRequest.getSite())
                .operation(op.getOperation())
                .phaseId(workListRequest.getPhase())
                .maxRecord(maxRecord)
                .qualityCheck(qualityCheck)
                .operatorCheck(operatorCheck)
                .resource(workListRequest.getResource())
                .build();

        return batchNoInQueueService.getBatchInQueueListForWorkList(batchInQueueReq);
    }

    private List<BatchNoInWork> getBatchInWorkRecord(WorkListRequest workListRequest, Operation op, int maxRecord) throws Exception {
        BatchNoInWorkRequest batchNoInWorkRequest = BatchNoInWorkRequest.builder()
                .site(workListRequest.getSite())
                .operation(op.getOperation())
                .phaseId(workListRequest.getPhase())
                .resource(workListRequest.getResource())
                .maxRecord(maxRecord)
                .build();
        return batchNoInWorkService.getBatchInWorkList(batchNoInWorkRequest);
    }

    private List<BatchNoHold> getBatchInHoldRecord(WorkListRequest workListRequest, Operation op, int maxRecord) throws Exception {
        BatchNoInWorkRequest batchNoInWorkRequest = BatchNoInWorkRequest.builder()
                .site(workListRequest.getSite())
                .operation(op.getOperation())
                .phaseId(workListRequest.getPhase())
                .resource(workListRequest.getResource())
                .maxRecord(maxRecord)
                .build();
        return batchNoHoldService.getBatchHoldList(batchNoInWorkRequest);
    }

// method under development
//    private List<BatchNoInWork> getBatchInHoldRecord(WorkListRequest workListRequest, Operation op) throws Exception {
//        BatchNoInWorkRequest batchNoInWorkRequest = BatchNoInWorkRequest.builder()
//                .site(workListRequest.getSite())
//                .operation(op.getOperation())
//                .phaseId(workListRequest.getPhase())
//                .resource(workListRequest.getResource())
//                .build();
//        return batchNoInWorkService.getBatchInWorkList(batchNoInWorkRequest);// method change for hold
//    }

    public List<WorkListResponse> setBatchInQueueValues(List<BatchNoInQueue> batchNoInQueues) throws Exception {

        List<WorkListResponse> workLists = new ArrayList<>();

        for (BatchNoInQueue batchNoInQueue : batchNoInQueues) {

            List<ColumnList> columnLists = new ArrayList<>();
            String material = null;

            for (String columnName : batchNoInQueue.getFieldNames()) {
                if (processColumnExistsInListResponse(columnName, "inqueue")) {
                    try {
                        Field field = BatchNoInQueue.class.getDeclaredField(columnName);
                        field.setAccessible(true);
                        Object fieldValue = field.get(batchNoInQueue);

                        if (fieldValue != null) {
                            ColumnList columnList = new ColumnList();
                            columnList.setDataField(columnName);
                            columnList.setDataAttribute(fieldValue.toString());
                            columnLists.add(columnList);

                            if(columnName.equalsIgnoreCase("material")) {
                                material = fieldValue.toString();
                                columnName = "productName";
                                if(processColumnExistsInListResponse(columnName, null)){
                                    Item itemRequest = Item.builder().site(batchNoInQueue.getSite()).item(material).build();

                                    try {
                                        Item itemResponse = webClientBuilder.build()
                                                .post()
                                                .uri(itemServiceUrl)
                                                .bodyValue(itemRequest)
                                                .retrieve()
                                                .bodyToMono(Item.class)
                                                .block();
                                        if(itemResponse == null)
                                            throw new WorkListException(356, material);

                                        ColumnList columnList1 = new ColumnList();
                                        columnList1.setDataField(columnName);
                                        columnList1.setDataAttribute(itemResponse.getDescription());
                                        columnLists.add(columnList1);

                                    } catch(Exception e) {
                                        throw new WorkListException(155);
                                    }
                                }
                            }
                        }
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();

                    }
                }
            }

            ColumnList columnList = new ColumnList();
            columnList.setDataField("Status");
            columnList.setDataAttribute("In Queue");
            columnLists.add(columnList);
            WorkListResponse workListResponse = new WorkListResponse();
            workListResponse.setColumnLists(columnLists);
            workLists.add(workListResponse);
        }
        return workLists;
    }

    public List<WorkListResponse> setBatchInWorkValues(List<BatchNoInQueue> batchNoInWorks) throws Exception {
        List<WorkListResponse> workLists = new ArrayList<>();
        List<BatchNoInWork> batchNoInWorkList = batchNoInWorks.stream()
                .map(BatchNoInWork::new)
                .collect(Collectors.toList());

        for (BatchNoInWork batchNoInWork : batchNoInWorkList) {
            List<ColumnList> columnLists = new ArrayList<>();
            List<String> fieldNames = batchNoInWork.getFieldNames();

            for (String columnName : fieldNames) {
                if (processColumnExistsInListResponse(columnName, "inwork")) {
                    try {
                        Field field = BatchNoInWork.class.getDeclaredField(columnName);
                        field.setAccessible(true);
                        Object fieldValue = field.get(batchNoInWork);

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

            ColumnList columnList = new ColumnList();
            columnList.setDataField("Status");
            columnList.setDataAttribute("active");
            columnLists.add(columnList);

            WorkListResponse workListResponse = new WorkListResponse();
            workListResponse.setColumnLists(columnLists);
            workLists.add(workListResponse);
        }

        return workLists;
    }

    public List<WorkListResponse> setBatchHoldValues(List<BatchNoInQueue> batchNoHolds) throws Exception {
        List<WorkListResponse> workLists = new ArrayList<>();
        List<BatchNoHold> batchNoHoldList = batchNoHolds.stream()
                .map(BatchNoHold::new)
                .collect(Collectors.toList());

        for (BatchNoHold batchNoHold : batchNoHoldList) {
            List<ColumnList> columnLists = new ArrayList<>();
            List<String> fieldNames = batchNoHold.getFieldNames();

            for (String columnName : fieldNames) {
                if (processColumnExistsInListResponse(columnName, "hold")) {
                    try {
                        Field field = BatchNoHold.class.getDeclaredField(columnName);
                        field.setAccessible(true);
                        Object fieldValue = field.get(batchNoHold);

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

            ColumnList columnList = new ColumnList();
            columnList.setDataField("Status");
            columnList.setDataAttribute("hold");
            columnLists.add(columnList);

            WorkListResponse workListResponse = new WorkListResponse();
            workListResponse.setColumnLists(columnLists);
            workLists.add(workListResponse);
        }

        return workLists;


    }

    private List<WorkListResponse> processFinalWorkList(List<WorkListResponse> workLists) {
        for (WorkListResponse workListResponse : workLists) {
            List<ColumnList> columnLists = workListResponse.getColumnLists();
            ColumnList removeChidROuteColumn=null;

            for (ColumnList columnList : columnLists) {
                // Check if dataField is "qtyInQueue" and update it to "qty"
                if ("qtyInQueue".equals(columnList.getDataField())) {
                    columnList.setDataField("qty");
                }
                if("qtyInWork".equals(columnList.getDataField())){
                    columnList.setDataField("qty");
                }
                if("qtyToComplete".equals(columnList.getDataField())){
                    columnList.setDataField("qty");
                }
                if("holdQty".equals(columnList.getDataField())){
                    columnList.setDataField("qty");
                }
                if("baseUom".equals(columnList.getDataField())){
                    columnList.setDataField("uom");
                }
                if(columnList.getDataField().equalsIgnoreCase("childRouterBO")){
                    removeChidROuteColumn=columnList;
                }
            }
            if(removeChidROuteColumn!=null) {
                columnLists.remove(removeChidROuteColumn);
                for(ColumnList list: columnLists){
                    if("routerBO".equalsIgnoreCase(list.getDataField())){
                        list.setDataAttribute(removeChidROuteColumn.getDataAttribute());
                    }
                }
            }
        }
        return workLists;
    }




    private List<WorkListResponse> getWorkInstructionWorkList(WorkListRequest workListRequest) {
        List<WorkListResponse> workListResponses=webClientBuilder.build()
                .post()
                .uri(getWorkInstructionWorkListUrl)
                .bodyValue(workListRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<WorkListResponse>>() {
                })
                .block();
        return workListResponses;
    }

    private BomComponentList getBomComponentList(PcuHeader pcuHeader, WorkListRequest workListRequest) {
        String bom = null;
        String revision=null;
        String pcuBomBo=pcuHeader.getBomList().get(0).getPcuBomBO();
        Item item=retrieveItem(pcuHeader);

        if(pcuBomBo!=null&&!pcuBomBo.isEmpty()) {
            String bomBo[] = pcuBomBo.split(",");
            if(bomBo[1]!=null&&!bomBo[1].isEmpty()){
                bom=bomBo[1];
                revision=bomBo[2];

            }else{
                if(item!=null&& item.getItem()!=null&& !item.getItem().isEmpty()){
                    bom=item.getBom();
                    revision= item.getBomVersion();
                }
            }
        }else{
            if(item!=null&& item.getItem()!=null&& !item.getItem().isEmpty()){
                bom=item.getBom();
                revision= item.getBomVersion();
            }
        }
        String[] operationBO=workListRequest.getOperationBO().split(",");
        IsExist isExist = IsExist.builder().site(workListRequest.getSite()).pcuBO(pcuHeader.getPcuBO()).bom(bom).revision(revision).operation(operationBO[1]).build();
        BomComponentList bomComponentList= webClientBuilder.build()
                .post()
                .uri(getComponentListUrl)
                .bodyValue(isExist)
                .retrieve()
                .bodyToMono(BomComponentList.class)
                .block();

        return bomComponentList;
    }

    private String getShopOrderBO(Object rawData) {
        if (rawData instanceof PcuInQueue) {
            return ((PcuInQueue) rawData).getShopOrderBO();
        } else if (rawData instanceof PcuInWork) {
            return ((PcuInWork) rawData).getShopOrderBO();
        } else if (rawData instanceof PcuDone) {
            return ((PcuDone) rawData).getShopOrderBO();
        }
        return null;
    }
    private String getPcuBO(Object rawData) {
        if (rawData instanceof PcuInQueue) {
            return ((PcuInQueue) rawData).getPcuBO();
        } else if (rawData instanceof PcuInWork) {
            return ((PcuInWork) rawData).getPcuBO();
        } else if (rawData instanceof PcuDone) {
            return ((PcuDone) rawData).getPcuBO();
        }
        return null;
    }

    private String getRecipeBatch(Object rawData) {
        if (rawData instanceof BatchNoInQueue) {
            return ((BatchNoInQueue) rawData).getBatchNo();
        } else if (rawData instanceof BatchNoInWork) {
            return ((BatchNoInWork) rawData).getBatchNo();
        } else if (rawData instanceof BatchNoDone) {
            return ((BatchNoDone) rawData).getBatchNo();
        }
        return null;
    }

    private String getProecssOrder(Object rawData) {
        if (rawData instanceof BatchNoInQueue) {
            return ((BatchNoInQueue) rawData).getOrderNumber();
        } else if (rawData instanceof BatchNoInWork) {
            return ((BatchNoInWork) rawData).getOrderNumber();
        } else if (rawData instanceof BatchNoDone) {
            return ((BatchNoDone) rawData).getOrderNumber();
        }
        return null;
    }

    private Item retrieveItem(PcuHeader pcuHeader) {
        String[] itemBo=pcuHeader.getItemBO().split(",");
        IsExist isExist=IsExist.builder().site(pcuHeader.getSite()).item(itemBo[1]).revision(itemBo[2]).build();
        return webClientBuilder.build()
                        .post()
                        .uri(readPcuUrl)
                        .bodyValue(isExist)
                        .retrieve()
                        .bodyToMono(Item.class)
                        .block();
    }

    private PcuHeader getPcuHeader(WorkListRequest workListRequest) {
        PcuHeader pcuHeader=webClientBuilder.build()
                .post()
                .uri(readPcuUrl)
                .bodyValue(workListRequest)
                .retrieve()
                .bodyToMono(PcuHeader.class)
                .block();
        return pcuHeader;
    }

    private List<WorkListResponse> setOperationValues(Operation operationResponse, String[] operationFieldName) {
        List<WorkListResponse> operationWorkLists = new ArrayList<>();
        List<ColumnList> columnLists = new ArrayList<>();

        for (String fieldName : operationFieldName) {
            fieldName= fieldName.toLowerCase();
            if (columnExistsInListResponse(fieldName)) {
                try {
                    String[] fieldNameOp=fieldName.split("operation");
                    fieldName=fieldNameOp[1];
                    Field operationField = Operation.class.getDeclaredField(fieldName);
                    operationField.setAccessible(true);
                    Object fieldValue = operationField.get(operationResponse);

                    if (fieldValue != null) {
                        ColumnList columnList = new ColumnList();
                        columnList.setDataField("Operation"+fieldName);
                        columnList.setDataAttribute(fieldValue.toString());
                        columnLists.add(columnList);


                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        WorkListResponse workListResponse = new WorkListResponse();
        workListResponse.setColumnLists(columnLists);

        operationWorkLists.add(workListResponse);
        return operationWorkLists;
    }
    private List<WorkListResponse> setItemValue(Item item, String[] itemFieldName) {
        List<WorkListResponse> itemWorkLists = new ArrayList<>();
        List<ColumnList> columnLists = new ArrayList<>();

        for (String fieldName : itemFieldName) {
            fieldName= fieldName.toLowerCase();
            if (columnExistsInListResponse(fieldName)) {
                try {
                    String[] fieldNameOp=fieldName.split("item");
                    fieldName=fieldNameOp[1];
                    Field itemField = Item.class.getDeclaredField(fieldName);
                    itemField.setAccessible(true);
                    Object fieldValue = itemField.get(item);

                    if (fieldValue != null) {
                        ColumnList columnList = new ColumnList();
                        columnList.setDataField("item"+fieldName);
                        columnList.setDataAttribute(fieldValue.toString());
                        columnLists.add(columnList);


                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        WorkListResponse workListResponse = new WorkListResponse();
        workListResponse.setColumnLists(columnLists);

        itemWorkLists.add(workListResponse);
        return itemWorkLists;
    }


    @Override
    public List<WorkListResponse> setValues(List<PcuInQueue> list) throws Exception {

        List<WorkListResponse> workLists = new ArrayList<>();

        for (PcuInQueue pcu : list) {
            List<ColumnList> columnLists = new ArrayList<>();
            for (String columnName : pcu.getFieldNames()) {
                if (columnExistsInListResponse(columnName)) {
                    try {
                        Field field = PcuInQueue.class.getDeclaredField(columnName);
                        field.setAccessible(true);
                        Object fieldValue = field.get(pcu);

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

            ColumnList columnList = new ColumnList();
            columnList.setDataField("Status");
            columnList.setDataAttribute("In Queue");
            columnLists.add(columnList);
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

    private boolean processColumnExistsInListResponse(String columnName, String status) {
        if (listResponse != null) {
            for (Column column : listResponse.getColumnList()) {
                String listColumn=column.getColumnName();
                if (columnName.equalsIgnoreCase(listColumn)) {
                    return true;
                }
                if (listColumn.equalsIgnoreCase("qty") && columnName.equalsIgnoreCase("qtyInQueue") && status.equals("inqueue")) {
                    return true;
                }
                if (listColumn.equalsIgnoreCase("qty") && columnName.equalsIgnoreCase("qtyToComplete") && status.equals("inwork")) {
                    return true;
                }
                if (listColumn.equalsIgnoreCase("qty") && columnName.equalsIgnoreCase("holdQty") && status.equals("hold")) {
                    return true;
                }
                if (listColumn.equalsIgnoreCase("uom") && columnName.equalsIgnoreCase("baseUom")) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<WorkListResponse> setValuesOfPcuInWork(List<PcuInQueue> list) throws Exception {
        List<WorkListResponse> workLists = new ArrayList<>();
        List<PcuInWork> pcuInWorkList = list.stream()
                .map(PcuInWork::new)
                .collect(Collectors.toList());

        for (PcuInWork pcu : pcuInWorkList) {
            List<ColumnList> columnLists = new ArrayList<>();
            List<String> fieldNames = pcu.getFieldNames();

            for (String columnName : fieldNames) {
                if (columnExistsInListResponse(columnName)) {
                    try {
                        Field field = PcuInWork.class.getDeclaredField(columnName);
                        field.setAccessible(true);
                        Object fieldValue = field.get(pcu);

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

            // Add the "active" status to the current PcuInWork object
            ColumnList columnList = new ColumnList();
            columnList.setDataField("Status");
            columnList.setDataAttribute("active");
            columnLists.add(columnList);

            // Create a new WorkList object for each PcuInWork object and set the columnLists
            WorkListResponse workListResponse = new WorkListResponse();
            workListResponse.setColumnLists(columnLists);
            workLists.add(workListResponse);
        }

        return workLists;
    }


    @Override
    public List<WorkListResponse> setValuesOfPcuDone(List<PcuDone> list) throws Exception {
        List<WorkListResponse> workLists = new ArrayList<>();

        for (PcuDone pcu : list) {
            List<ColumnList> columnLists = new ArrayList<>();

            for (String columnName : pcu.getFieldNames()) {
                if (columnExistsInListResponse(columnName)) {
                    try {
                        Field field = PcuDone.class.getDeclaredField(columnName);
                        field.setAccessible(true);
                        Object fieldValue = field.get(pcu);

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

            // Add the "done" status to the current PcuDone object
            ColumnList columnList = new ColumnList();
            columnList.setDataField("Status");
            columnList.setDataAttribute("done");
            columnLists.add(columnList);

            // Create a new WorkList object for each PcuDone object and set the columnLists
            WorkListResponse workListResponse = new WorkListResponse();
            workListResponse.setColumnLists(columnLists);
            workLists.add(workListResponse);
        }

        return workLists;
    }

    @Override
    public List<ColumnList> setShopOrder(PcuInQueue pcuInQueue) throws Exception {
        String[] shopOrder = pcuInQueue.getShopOrderBO().split(",");
        ShopOrderRequest shopOrderRequest = ShopOrderRequest.builder()
                .site(pcuInQueue.getSite())
                .shopOrder(shopOrder[1])
                .build();
        ShopOrder shopOrderData = webClientBuilder.build()
                .post()
                .uri(shopOrderRetrieveUrl)
                .bodyValue(shopOrderRequest)
                .retrieve()
                .bodyToMono(ShopOrder.class)
                .block();

        if (shopOrderData != null) {
            // List<ColumnList> shopOrderDetails = processShopOrderData(shopOrderData);
            // return shopOrderDetails;
        }

        return Collections.emptyList();
    }
    private List<ColumnList> processShopOrder(ShopOrder shopOrderData, String[] shopOrderFieldNames) throws Exception {
        if (shopOrderData != null && shopOrderData.getShopOrder() != null) {
            List<ColumnList> shopOrderDetails = new ArrayList<>();
            for (String fieldName : shopOrderFieldNames) {
                Field shopOrderField = ShopOrder.class.getDeclaredField(fieldName);
                shopOrderField.setAccessible(true);
                Object fieldValue = shopOrderField.get(shopOrderData);

                if (fieldValue != null) {
                    ColumnList columnList = new ColumnList();
                    columnList.setDataField(fieldName);
                    columnList.setDataAttribute(fieldValue.toString());
                    shopOrderDetails.add(columnList);
                }
            }
            return shopOrderDetails;
            // Do something with shopOrderDetails if needed
        }
        return Collections.emptyList();
    }

    private List<ColumnList> processOrder(ProcessOrder processOrder, String[] processOrderNames) throws Exception {
        if (processOrder != null && processOrder.getOrderNumber() != null) {
            List<ColumnList> processOrderDetails = new ArrayList<>();
            for (String fieldName : processOrderNames) {
                Field processOrderField = ProcessOrder.class.getDeclaredField(fieldName);
                processOrderField.setAccessible(true);
                Object fieldValue = processOrderField.get(processOrder);

                if (fieldValue != null) {
                    ColumnList columnList = new ColumnList();
                    columnList.setDataField(fieldName);
                    columnList.setDataAttribute(fieldValue.toString());
                    processOrderDetails.add(columnList);
                }
            }
            return processOrderDetails;
        }
        return Collections.emptyList();
    }

    @Override

    public List<WorkList> getFieldNameByCategory(String category) throws Exception {


        return workListRepository.findByPreDefinedFieldGroupContainingIgnoreCase(category);
    }
        private List<WorkListResponse> setBomComponentValues(BomComponentList bomComponentList, String[] bomComponentFieldNames, WorkListRequest workListRequest, PcuRouterHeader pcuRouterHeader) {
            List<WorkListResponse> bomComponentWorkLists = new ArrayList<>();

            for (BomComponent bomComponent : bomComponentList.getBomComponentList()) {
                List<ColumnList> columnLists = new ArrayList<>();

                for (String fieldName : bomComponentFieldNames) {
                    fieldName=fieldName.toLowerCase();
                    if (columnExistsInListResponse(fieldName)) {
                        try {
                            String concatenatedField=null;
                            if(fieldName.equalsIgnoreCase("version")){
                                 concatenatedField="componentVersion";
                            }
                            if(fieldName.equalsIgnoreCase("sequence")){
                                concatenatedField="assySequence";
                            }
                            if(fieldName.equalsIgnoreCase("description")){
                                concatenatedField="componentDescription";
                            }
                            if(fieldName.equalsIgnoreCase("componentType")){
                                concatenatedField="componenentType";
                            }
                            if(fieldName.equalsIgnoreCase("storageLocation")){
                                concatenatedField="storageLocationBo";
                            }
                            if(fieldName.equalsIgnoreCase("assyDataType")){
                                concatenatedField="assyDataTypeBo";
                            }
                            if(fieldName.equalsIgnoreCase("qtyRequired")){
                                concatenatedField="assyQty";
                            }Field bomComponentField;
                            if(concatenatedField!=null) {
                                 bomComponentField = BomComponent.class.getDeclaredField(concatenatedField);
                            }else{
                                 bomComponentField = BomComponent.class.getDeclaredField(fieldName);
                            }
                            bomComponentField.setAccessible(true);
                            Object fieldValue = bomComponentField.get(bomComponent);

                            if (fieldValue != null) {
                                ColumnList columnList = new ColumnList();
                                columnList.setDataField(fieldName);
                                columnList.setDataAttribute(fieldValue.toString());
                                columnLists.add(columnList);
                            }
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    if (fieldName.equalsIgnoreCase("OperationStepId")) {
                        String getOperationStepID = getOperationStep(workListRequest.getOperationBO(), pcuRouterHeader.getRouter().get(0).getR_route().get(0).getRoutingStepList());
                        ColumnList columnList = new ColumnList("Operation_StepId", getOperationStepID);
                        columnLists.add(columnList);
                    }
                    if(fieldName.equalsIgnoreCase("qtyRemain")){
                        Double qtyRemain=Double.parseDouble(bomComponent.getAssyQty())-Double.parseDouble(bomComponent.getAssembledQty());
                        ColumnList columnList = new ColumnList("qtyRemain", String.valueOf(qtyRemain));
                        columnLists.add(columnList);
                    }
                }

                WorkListResponse workListResponse = new WorkListResponse();
                workListResponse.setColumnLists(columnLists);
                bomComponentWorkLists.add(workListResponse);
            }

            return bomComponentWorkLists;
        }

        private boolean columnExistsInBomComponent(String fieldName) {
        try {
            BomComponent.class.getDeclaredField(fieldName);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }
    private String getOperationStep(String operationBO, List<RoutingStep> routingStepList) {
        String operationStepID="";
        String[] operation=operationBO.split(",");
        for(RoutingStep routingStep: routingStepList){
            if(routingStep.getStepType().equalsIgnoreCase("operation")){
                if(routingStep.getOperation().equals(operation[1])){
                    operationStepID=routingStep.getOperation()+"/"+routingStep.getStepId();
                }
            }else{
                for(RoutingStep step:routingStep.getRouterDetails().get(0).getRoutingStepList()){
                    if(step.getStepType().equalsIgnoreCase("operation")){
                        if(step.getOperation().equals(operation[1])){
                            operationStepID=step.getOperation()+"/"+step.getStepId();
                        }
                    }
                }
            }
        }
        return operationStepID;
    }

    @Override
    public boolean dummyWebCLient(String site){
        WorkList list = workListRepository.findByPreDefinedFieldGroup("dummy");

        // Check if the WorkList and its fieldValue are not null or empty
        if (list != null && list.getFieldValue() != null && !list.getFieldValue().isEmpty()) {
            try {
                // Create a temporary Java source file
                Path tempFile = Files.createTempFile("DynamicClass", ".java");
                PrintWriter writer = new PrintWriter(Files.newBufferedWriter(tempFile));
                writer.println(list.getFieldValue());
                writer.close();

                // Compile the Java source file dynamically
                JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                int compilationResult = compiler.run(null, null, null, tempFile.toAbsolutePath().toString());

                // Delete the temporary Java source file
                Files.deleteIfExists(tempFile);

                // Check compilation result
                if (compilationResult == 0) {
                    // Compilation successful
                    return true;
                } else {
                    // Compilation failed
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false; // Indicate failure
            }
        }
        return false; // Indicate fieldValue is null or empty
    }
}








