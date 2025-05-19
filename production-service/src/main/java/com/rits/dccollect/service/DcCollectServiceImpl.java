package com.rits.dccollect.service;

import com.rits.dccollect.dto.*;
import com.rits.dccollect.exception.DcCollectException;
import com.rits.dccollect.model.DcCollectMessageModel;
import com.rits.dccollect.model.ParametricMeasures;
import com.rits.dccollect.model.ParametricPreSave;
import com.rits.dccollect.repository.DcCollectRepositorySave;
import com.rits.dccollect.repository.DcCollectRepositorySaveDraft;
import com.rits.productionlogservice.dto.ProductionLogRequest;
import com.rits.productionlogservice.producer.ProductionLogProducer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DcCollectServiceImpl implements DcCollectService{

    private final DcCollectRepositorySave dcCollectRepositorySave;

    private final DcCollectRepositorySaveDraft dcCollectRepositorySaveDraft;
    private final ApplicationEventPublisher eventPublisher;
    private final WebClient.Builder webClientBuilder;
    private final MessageSource localMessageSource;


    @Value("${datacollection-service.url}/retrieveByOperationAndResourceAndPcu")
    private String getDcGroupListUrl;

    @Value("${datacollection-service.url}/retrieve")
    private String getParameterListUrl;

    @Value("${datacollection-service.url}/update")
    private String updateDataCollection;

    @Value("${datacollection-service.url}/retrieve")
    private String getDataCollectionUrl;
    @Value("${datacollection-service.url}/retrieveByResource")
    private String getDataCollectionByResourceUrl;

    @Value("${datacollection-service.url}/retrieveCurrentDCGroup")
    private String getCurrentDataCollectionUrl;

    @Value("${datacollection-service.url}/retrieveAllDataCollection")
    private String getAllDataCollection;

    @Value("${datacollection-service.url}/retrieve")
    private String getCurrentDCByName;
    @Value("${auditlog-service.url}/producer")
    private String auditlogUrl;

    @Value("${operation-service.url}/retrieve")
    private String operationRetrieve;

    @Value("${item-service.url}/retrieve")
    private String retrieveItem;

    @Value("${productionlog-service.url}/save")
    private String productionLogUrl;

    @Value("${shoporder-service.url}/retrieveByPcu")
    private String shopOrderUrl;
    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }


    public List<DcGroupList> getDataCollectionGroupList(DcCollectRequest dcCollectRequest) throws Exception
    {
        RetrieveRequest retrieveRequest = RetrieveRequest.builder().site(dcCollectRequest.getSite()).pcu(dcCollectRequest.getPcu()).resource(dcCollectRequest.getResource()).operation(dcCollectRequest.getOperation()).build();
        List<DcGroupList> dcGroupList = webClientBuilder.build()
                .post()
                .uri(getDcGroupListUrl)
                .bodyValue(retrieveRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<DcGroupList>>() { })
                .block();
        if(dcGroupList == null && dcGroupList.isEmpty())
        {
            throw new DcCollectException(2300,dcGroupList);
        }
        return dcGroupList;
    }

    @Override
    public List<Parameter> getParameterList(DcCollectRequest dcCollectRequest) throws Exception
    {
        RetrieveRequest retrieveRequest = RetrieveRequest.builder().site(dcCollectRequest.getSite()).dataCollection(dcCollectRequest.getDataCollection()).version(dcCollectRequest.getVersion()).build();
        DataCollection dataCollection = webClientBuilder.build()
                .post()
                .uri(getParameterListUrl)
                .bodyValue(retrieveRequest)
                .retrieve()
                .bodyToMono(DataCollection.class)
                .block();
        if(dataCollection.getParameterList() == null || dataCollection.getParameterList().isEmpty())
        {
            throw new DcCollectException(2300,dataCollection.getParameterList());
        }
        return dataCollection.getParameterList();
    }

    @Override
    public DataCollection retrievedDataCollection(DcCollectRequest dcCollectRequest)throws Exception
    {
        DataCollectionRequest dataCollectionRequest = DataCollectionRequest.builder().dataCollection(dcCollectRequest.getDataCollection()).version(dcCollectRequest.getVersion()).site(dcCollectRequest.getSite()).build();
        DataCollection dataCollection = webClientBuilder.build()
                .post()
                .uri(getDataCollectionUrl)
                .bodyValue(dataCollectionRequest)
                .retrieve()
                .bodyToMono(DataCollection.class)
                .block();
        if(dataCollection == null)
        {
            throw new DcCollectException(2301,dcCollectRequest.getDataCollection());
        }
        return dataCollection;
    }



    @Override
    public DcCollectMessageModel logDc(List<DcCollectRequest> dcCollectRequestList) throws Exception {
        List<ParametricMeasures> newParametricMeasuresList = new ArrayList<>();
        List<Parameter> parameterList ;
        DcCollectRequest iteratedDcCollectRequest = null;
        Item itemRecord = null;
        Operation operationRecord = null;
        ShopOrder shopOrder = null;
        int count = 0;
        List<String> para = new ArrayList<>();
        para.add(dcCollectRequestList.get(0).getParameterName());

        for(DcCollectRequest dcCollectRequest : dcCollectRequestList) {
           // if(dcCollectRequest.getActualValue() != ""){

            if(!para.contains(dcCollectRequest.getParameterName())){
                count++;
                para.add(dcCollectRequest.getParameterName());
            }
            iteratedDcCollectRequest = dcCollectRequest;
            parameterList = getParameterList(dcCollectRequest);
            DataCollection retrievedRecord = retrievedDataCollection(dcCollectRequest);
            if (parameterList == null || parameterList.isEmpty()) {
                throw new DcCollectException(2300, parameterList);
            }
            OperationRequest operationRetrieveRequest = OperationRequest.builder().site(dcCollectRequest.getSite()).operation(dcCollectRequest.getOperation()).build();
            operationRecord = webClientBuilder.build()
                    .post()
                    .uri(operationRetrieve)
                    .bodyValue(operationRetrieveRequest)
                    .retrieve()
                    .bodyToMono(Operation.class)
                    .block();
            if(operationRecord==null|| operationRecord.getOperation()==null|| operationRecord.getOperation().isEmpty()){
                throw new DcCollectException(2908,dcCollectRequest.getOperation());
            }

            ShopOrderRequest shopOrderRequest = ShopOrderRequest.builder().site(dcCollectRequest.getSite()).pcu(dcCollectRequest.getPcu()).build();
            shopOrder = webClientBuilder.build()
                    .post()
                    .uri(shopOrderUrl)
                    .bodyValue(shopOrderRequest)
                    .retrieve()
                    .bodyToMono(ShopOrder.class)
                    .block();
            if(shopOrder==null|| shopOrder.getShopOrder()==null|| shopOrder.getShopOrder().isEmpty()){
                throw new DcCollectException(3200,dcCollectRequest.getPcu());
            }
            ItemRequest itemRetrieveRequest = ItemRequest.builder().site(dcCollectRequest.getSite()).item(shopOrder.getPlannedMaterial()).build();
            itemRecord = webClientBuilder.build()
                    .post()
                    .uri(retrieveItem)
                    .bodyValue(itemRetrieveRequest)
                    .retrieve()
                    .bodyToMono(Item.class)
                    .block();
            if(itemRecord==null|| itemRecord.getItem()==null|| itemRecord.getItem().isEmpty()){
                throw new DcCollectException(3209,dcCollectRequest.getPcu());
            }
            String routing = "";
            String routingVersion="";
            if(shopOrder!=null && shopOrder.getPlannedRouting()!=null && !shopOrder.getPlannedRouting().isEmpty())
            {
                routing = shopOrder.getPlannedRouting();
                routingVersion = shopOrder.getRoutingVersion();
            }
            if(itemRecord!=null && itemRecord.getRouting()!=null && !itemRecord.getRouting().isEmpty())
            {
                routing = itemRecord.getRouting();
                routingVersion = itemRecord.getRoutingVersion();
            }
            if(dcCollectRequest.getActualValue() != "" && dcCollectRequest.getActualValue() != null ) {
                if (!parameterValidation(dcCollectRequest)) {
                    throw new DcCollectException(2304, dcCollectRequest.getActualValue());
                }
            }
//            for (Parameter parameter : parameterList) {
            ParametricMeasures saveParametricMeasures = ParametricMeasures.builder()
                    .parameterBo(parameterList.get(count).getParameterName())
                    .dcGroupBO("DcGroupBO:" + dcCollectRequest.getSite() + "," + dcCollectRequest.getDataCollection()+"," + dcCollectRequest.getVersion())
                    .description(parameterList.get(count).getDescription())
                    .pcuBO("PcuBO:"+dcCollectRequest.getSite()+","+dcCollectRequest.getPcu())
                    .measureStatus(parameterList.get(count).getStatus())
                    .measureType(parameterList.get(count).getType())
                    .actualValue(dcCollectRequest.getActualValue())
                    .operationBO("OperationBO:"+dcCollectRequest.getSite()+","+operationRecord.getOperation()+","+operationRecord.getRevision())
                    .itemBO("ItemBO:"+dcCollectRequest.getSite()+","+itemRecord.getItem()+","+itemRecord.getRevision())
                    .unitOfMeasure(parameterList.get(count).getUnitOfMeasure())
                    .shopOrder(shopOrder.getShopOrder())
                    .workCenter(operationRecord.getWorkCenter())
                    .stepID("")
                    .dcGroupDescription(retrievedRecord.getDescription())
                    .subStepID("")
                    .resource(dcCollectRequest.getResource())
                    .routingBO("RoutingBO:"+dcCollectRequest.getSite()+","+routing+","+routingVersion)
                    .highLimit(parameterList.get(count).getMaxValue())
                    .lowLimit(parameterList.get(count).getMinValue())
                    .expected(parameterList.get(count).getTargetValue())
                    .userBO("UserBO:"+dcCollectRequest.getSite()+","+dcCollectRequest.getUserBO())
                    .originalTestDateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();
            newParametricMeasuresList.add(saveParametricMeasures);
        //    }
        }

        String handle = "";

        if(!StringUtils.isEmpty(iteratedDcCollectRequest.getPcu()))
            handle += "PcuBO:" + iteratedDcCollectRequest.getSite() + "," + iteratedDcCollectRequest.getPcu() + ",";

        if(!StringUtils.isEmpty(iteratedDcCollectRequest.getOperation()))
            handle += "OperationBO:" + iteratedDcCollectRequest.getSite() + "," + iteratedDcCollectRequest.getOperation() + ",";

        if(!StringUtils.isEmpty(iteratedDcCollectRequest.getWorkCenter()))
            handle += "WorkCenterBO:" + iteratedDcCollectRequest.getSite() + "," + iteratedDcCollectRequest.getWorkCenter() + ",";

        if(!StringUtils.isEmpty(iteratedDcCollectRequest.getResource()))
            handle += "ResourceBO:" + iteratedDcCollectRequest.getSite() + "," + iteratedDcCollectRequest.getResource() + ",";

        if(!StringUtils.isEmpty(iteratedDcCollectRequest.getDataCollection()) && !StringUtils.isEmpty(iteratedDcCollectRequest.getVersion()))
            handle += "ParametricPreSave:" + iteratedDcCollectRequest.getSite() + "," + iteratedDcCollectRequest.getDataCollection() + "," + iteratedDcCollectRequest.getVersion();

        if(iteratedDcCollectRequest!=null){
            DcSaveParametricMeasures dcGroupList = DcSaveParametricMeasures.builder()
//                    .handle("PcuBO:"+iteratedDcCollectRequest.getSite()+","+iteratedDcCollectRequest.getPcu()+","+"OperationBO:"+iteratedDcCollectRequest.getSite()+","+iteratedDcCollectRequest.getOperation()+","+"WorkCenterBO:"+iteratedDcCollectRequest.getSite()+","+iteratedDcCollectRequest.getWorkCenter()+","+"ResourceBO:"+iteratedDcCollectRequest.getSite()+","+iteratedDcCollectRequest.getResource()+","+"ParametricMeasures:" + iteratedDcCollectRequest.getSite() + "," + iteratedDcCollectRequest.getDataCollection()+","+ iteratedDcCollectRequest.getVersion()+","+LocalDateTime.now())
                    .handle(handle)
                    .site(iteratedDcCollectRequest.getSite())
                    .dataCollection(iteratedDcCollectRequest.getDataCollection())
                    .version(iteratedDcCollectRequest.getVersion())
                    .parametricMeasuresList(newParametricMeasuresList)
                    .active(1)
                    .createdDateTime(LocalDateTime.now())
                    .build();
            if (!newParametricMeasuresList.isEmpty()) {
                dcCollectRepositorySave.save(dcGroupList);
                ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                        .site(iteratedDcCollectRequest.getSite())
                        .eventType("DATA_COLLECTION")
                        .userId("UserBO:" + iteratedDcCollectRequest.getSite() + "," + iteratedDcCollectRequest.getUserBO())
                        .pcu("PcuBO:" + iteratedDcCollectRequest.getSite() + "," + iteratedDcCollectRequest.getPcu())
                        .shopOrderBO("ShopOrderBO:" + iteratedDcCollectRequest.getSite() + "," + shopOrder.getShopOrder())
                        .operation_bo("OperationBO:" + iteratedDcCollectRequest.getSite() + "," + operationRecord.getOperation() + "," + operationRecord.getRevision())
                        .workcenterId("WorkCenterBO:" + iteratedDcCollectRequest.getSite() + "," + iteratedDcCollectRequest.getWorkCenter())
                        .dataField(iteratedDcCollectRequest.getParameterName())
                        .dataValue(iteratedDcCollectRequest.getActualValue())
                        .resourceId("ResourceBO:" + iteratedDcCollectRequest.getSite() + "," + iteratedDcCollectRequest.getResource())
                        .itemBO("ItemBO:" + iteratedDcCollectRequest.getSite() + "," + itemRecord.getItem() + "," + itemRecord.getRevision())
                        .eventData("PcuBO:" + iteratedDcCollectRequest.getSite() + "," + iteratedDcCollectRequest.getPcu() + " Data Collected successfully")
                        .build();
//                Boolean productionLogged = webClientBuilder.build()
//                        .post()
//                        .uri(productionLogUrl)
//                        .bodyValue(productionLogRequest)
//                        .retrieve()
//                        .bodyToMono(Boolean.class)
//                        .block();
                eventPublisher.publishEvent(new ProductionLogProducer(productionLogRequest));
            }
            }

        if (!newParametricMeasuresList.isEmpty()) {
            String collectedMessage=getFormattedMessage(10,dcCollectRequestList.get(0).getDataCollection());
            return DcCollectMessageModel.builder().messageDetails(new MessageDetails(collectedMessage,"S")).build();
        }
        String errorMessage=getFormattedMessage(11,dcCollectRequestList.get(0).getDataCollection());
        return DcCollectMessageModel.builder().messageDetails(new MessageDetails(errorMessage,"S")).build();
    }

    @Override
    public List<DcSaveParametricMeasures> retrieveByDcGroupAndVersion(DcCollectRequest dcCollectRequest) throws Exception
    {
        List<DcSaveParametricMeasures> retrievedRecord = dcCollectRepositorySave.findTop1ByActiveAndSiteAndDataCollectionAndVersionAndParametricMeasuresList_PcuBOOrderByCreatedDateTimeDesc(1,dcCollectRequest.getSite(),dcCollectRequest.getDataCollection(),dcCollectRequest.getVersion(),"PcuBO:"+dcCollectRequest.getSite()+","+dcCollectRequest.getPcu());
        if(retrievedRecord!=null && !retrievedRecord.isEmpty())
        {
            for(DcSaveParametricMeasures dcSaveParametricMeasures:retrievedRecord)
            {
                for(ParametricMeasures parametricMeasures :dcSaveParametricMeasures.getParametricMeasuresList() )
                {
                    if(parametricMeasures.getUserBO() != null && !parametricMeasures.getUserBO().isEmpty()) {
                        String[] userBO = parametricMeasures.getUserBO().split(",");
                        if (userBO.length == 2) {
                            String user = userBO[1];
                            parametricMeasures.setUserBO(user);
                        }
                    }
                }
            }

            return retrievedRecord;
        }
        else{
            throw new DcCollectException(2301,dcCollectRequest.getDataCollection());
        }
    }

    @Override
    public List<DcSaveParametricMeasures> retrieve(String site, String pcu, String dataCollection, String version)throws Exception
    {
        List<DcSaveParametricMeasures> dcSaveParametricMeasures = new ArrayList<>();
        if(pcu!=null && !pcu.isEmpty() && (dataCollection==null || dataCollection.isEmpty()))
        {
            dcSaveParametricMeasures.addAll(dcCollectRepositorySave.findByActiveAndSiteAndParametricMeasuresListPcuBO(1,site,"PcuBO:"+site+","+pcu));
        }
        if((pcu==null || pcu.isEmpty()) && dataCollection!=null && !dataCollection.isEmpty() && version!=null && !version.isEmpty())
        {
            dcSaveParametricMeasures.addAll(dcCollectRepositorySave.findByActiveAndSiteAndDataCollectionAndVersion(1,site,dataCollection,version));
        }
        if(pcu!=null && !pcu.isEmpty() && dataCollection!=null && !dataCollection.isEmpty() && version!=null && !version.isEmpty())
        {
            dcSaveParametricMeasures.addAll(dcCollectRepositorySave.findByActiveAndSiteAndParametricMeasuresListPcuBOAndDataCollectionAndVersion(1,site,"PcuBO:"+site+","+pcu,dataCollection,version));
        }
        for(DcSaveParametricMeasures dcSaveParametricMeasuress : dcSaveParametricMeasures)
        {
            for(ParametricMeasures parametricMeasures : dcSaveParametricMeasuress.getParametricMeasuresList())
            {
                parametricMeasures.setDcGroupBO(dcSaveParametricMeasuress.getDataCollection()+"/"+dcSaveParametricMeasuress.getVersion());
                if(parametricMeasures.getOperationBO() != null && !parametricMeasures.getOperationBO().isEmpty()) {
                    String[] operationBO = parametricMeasures.getOperationBO().split(",");
                    if (operationBO.length == 3) {
                        String operation = operationBO[1];
                        String operationVersion = operationBO[2];
                        parametricMeasures.setOperationBO(operation + "/" + operationVersion);
                    }
                }
                if(parametricMeasures.getItemBO() != null && !parametricMeasures.getItemBO().isEmpty()) {
                    String[] itemBO = parametricMeasures.getItemBO().split(",");
                    if (itemBO.length == 3) {
                        String item = itemBO[1];
                        String itemVersion = itemBO[2];
                        parametricMeasures.setItemBO(item + "/" + itemVersion);
                    }
                }
                if(parametricMeasures.getPcuBO() != null && !parametricMeasures.getPcuBO().isEmpty()) {
                    String[] pcuBO = parametricMeasures.getPcuBO().split(",");
                    if (pcuBO.length == 2) {
                        String pcus = pcuBO[1];
                        parametricMeasures.setPcuBO(pcus);
                    }
                }
                if(parametricMeasures.getUserBO() != null && !parametricMeasures.getUserBO().isEmpty()) {
                    String[] userBO = parametricMeasures.getUserBO().split(",");
                    if (userBO.length == 2) {
                        String user = userBO[1];
                        parametricMeasures.setUserBO(user);
                    }
                }
            }
        }
        return dcSaveParametricMeasures;
    }

    @Override
    public MessageModel saveDraft(List<DcCollectRequest> dcCollectRequestList) throws Exception {

        List<ParametricPreSave> parametricPreSaveList = new ArrayList<>();
        List<Parameter> parameterList ;
        MessageModel messageModel = new MessageModel();
        int count = 0;
        DcParametricPreSave dcParametricPreSave;
        DcCollectRequest dcCollectRequest = null;

        List<String> para = new ArrayList<>();
        para.add(dcCollectRequestList.get(0).getParameterName());


        for (DcCollectRequest dataCollectionRequest : dcCollectRequestList) {
//            if(dataCollectionRequest.getActualValue() != "") {
                dcCollectRequest = dataCollectionRequest;
                if (!para.contains(dataCollectionRequest.getParameterName())) {
                    count++;
                    para.add(dataCollectionRequest.getParameterName());
                }
                DataCollection dataCollection = retrievedDataCollection(dataCollectionRequest);
                if (dataCollection == null || dataCollection.getDataCollection() == null) {
                    throw new DcCollectException(2300, dataCollectionRequest.getDataCollection());
                }
                parameterList = dataCollection.getParameterList();
                if (parameterList == null || parameterList.isEmpty()) {
                    throw new DcCollectException(2300, parameterList);
                }
//            if(!parameterValidation(dataCollectionRequest)){
//                throw new DcCollectException(2304,dataCollectionRequest.getActualValue());
//            }
//                if (dataCollectionRequest.getOperation() != null && !dataCollectionRequest.getOperation().isEmpty()) {
//                    OperationRequest operationRetrieveRequest = OperationRequest.builder().site(dataCollectionRequest.getSite()).operation(dataCollectionRequest.getOperation()).build();
//                    Operation operationRecord = webClientBuilder.build()
//                            .post()
//                            .uri(operationRetrieve)
//                            .bodyValue(operationRetrieveRequest)
//                            .retrieve()
//                            .bodyToMono(Operation.class)
//                            .block();
                    ParametricPreSave createParametricPreSave = ParametricPreSave.builder()
                            .parameterBo(parameterList.get(count).getParameterName())
                            .dcGroupBO("DcGroupBO:" + dataCollectionRequest.getSite() + "," + dataCollectionRequest.getDataCollection() + "," + dataCollectionRequest.getVersion())
                            .description(parameterList.get(count).getDescription())
                            .measureStatus(parameterList.get(count).getStatus())
                            .measureType(parameterList.get(count).getType())
                            .actualValue(dataCollectionRequest.getActualValue())
                            .unitOfMeasure(parameterList.get(count).getUnitOfMeasure())
                            .highLimit(parameterList.get(count).getMaxValue())
                            .lowLimit(parameterList.get(count).getMinValue())
                            .expected(parameterList.get(count).getTargetValue())
                            .userBO(parameterList.get(count).getUserBO())
                            .build();
                    parametricPreSaveList.add(createParametricPreSave);
//                }
//                    dcParametricPreSave = DcParametricPreSave.builder()
//                            .handle("PcuBO:" + dataCollectionRequest.getSite() + "," + dataCollectionRequest.getPcu() + "," + "OperationBO:" + dataCollectionRequest.getSite() + "," + dataCollectionRequest.getOperation() + "," + "WorkCenterBO:" + dataCollectionRequest.getSite() + "," + dataCollectionRequest.getWorkCenter() + "," + "ResourceBO:" + dataCollectionRequest.getSite() + "," + dataCollectionRequest.getResource() + "," + "ParametricPreSave:" + dataCollectionRequest.getSite() + "," + dataCollectionRequest.getDataCollection() + "," + dataCollectionRequest.getVersion())
//                            .site(dataCollectionRequest.getSite())
//                            .dataCollection(dataCollectionRequest.getDataCollection())
//                            .version(dataCollectionRequest.getVersion())
//                            .parametricPreSaves(parametricPreSaveList)
//                            .active(1)
//                            .createdDateTime(LocalDateTime.now())
//                            .modifiedDataTime(LocalDateTime.now())
//                            .build();
//                    dcCollectRepositorySaveDraft.save(dcParametricPreSave);
//                    messageModel.setDcParametricPreSave(dcParametricPreSave);
//                count++;
//                }
//            }
        }

        String handle = "";

        if(!StringUtils.isEmpty(dcCollectRequest.getPcu()))
            handle += "PcuBO:" + dcCollectRequest.getSite() + "," + dcCollectRequest.getPcu() + ",";

        if(!StringUtils.isEmpty(dcCollectRequest.getOperation()))
            handle += "OperationBO:" + dcCollectRequest.getSite() + "," + dcCollectRequest.getOperation() + ",";

        if(!StringUtils.isEmpty(dcCollectRequest.getWorkCenter()))
            handle += "WorkCenterBO:" + dcCollectRequest.getSite() + "," + dcCollectRequest.getWorkCenter() + ",";

        if(!StringUtils.isEmpty(dcCollectRequest.getResource()))
            handle += "ResourceBO:" + dcCollectRequest.getSite() + "," + dcCollectRequest.getResource() + ",";

        if(!StringUtils.isEmpty(dcCollectRequest.getDataCollection()) && !StringUtils.isEmpty(dcCollectRequest.getVersion()))
            handle += "ParametricPreSave:" + dcCollectRequest.getSite() + "," + dcCollectRequest.getDataCollection() + "," + dcCollectRequest.getVersion();

        if(dcCollectRequest != null) {
            dcParametricPreSave = DcParametricPreSave.builder()
                    .handle(handle)
                    .site(dcCollectRequest.getSite())
                    .dataCollection(dcCollectRequest.getDataCollection())
                    .version(dcCollectRequest.getVersion())
                    .parametricPreSaves(parametricPreSaveList)
                    .active(1)
                    .createdDateTime(LocalDateTime.now())
                    .modifiedDataTime(LocalDateTime.now())
                    .build();
            dcCollectRepositorySaveDraft.save(dcParametricPreSave);
            messageModel.setDcParametricPreSave(dcParametricPreSave);
        }
        MessageDetails messageDetails = MessageDetails.builder().msg("Saved As Draft Successfully").msg_type("S").build();
        messageModel.setMessage_details(messageDetails);
        return messageModel;
    }

    @Override
    public Boolean parameterValidation(DcCollectRequest dcCollectRequest)throws Exception
    {
        DataCollection dataCollection = retrievedDataCollection(dcCollectRequest);
        for(Parameter parameter : dataCollection.getParameterList())
        {
            if(parameter.getParameterName().equals(dcCollectRequest.getParameterName())) {
                if (parameter.getType().equalsIgnoreCase("Numeric")) {
                    if (parameter.getMinValue() != null && !parameter.getMinValue().isEmpty() && parameter.getMaxValue() != null && !parameter.getMaxValue().isEmpty()) {

                        if( !(Double.valueOf(parameter.getMinValue()) <= Double.valueOf(dcCollectRequest.getActualValue()) && Double.valueOf(dcCollectRequest.getActualValue()) <= Double.valueOf(parameter.getMaxValue()))) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public DcParametricPreSave retrievePreSaved (DcCollectRequest dcCollectRequest)throws Exception
    {
//        if(dcCollectRequest.getOperation()!=null && !dcCollectRequest.getOperation().isEmpty()) {
//            OperationRequest operationRetrieveRequest = OperationRequest.builder().site(dcCollectRequest.getSite()).operation(dcCollectRequest.getOperation()).build();
//            Operation operationRecord = webClientBuilder.build()
//                    .post()
//                    .uri(operationRetrieve)
//                    .bodyValue(operationRetrieveRequest)
//                    .retrieve()
//                    .bodyToMono(Operation.class)
//                    .block();
//            if(operationRecord!=null)
//            {
//                dcCollectRequest.setOperation(operationRecord.getOperation());
//                dcCollectRequest.setOperationVersion(operationRecord.getRevision());
//            }
//        }
//        String handle = "PcuBO:"+dcCollectRequest.getSite()+","+dcCollectRequest.getPcu()+","+"OperationBO:"+dcCollectRequest.getSite()+","+dcCollectRequest.getOperation()+","+"WorkCenterBO:"+dcCollectRequest.getSite()+","+dcCollectRequest.getWorkCenter()+","+"ResourceBO:"+dcCollectRequest.getSite()+","+dcCollectRequest.getResource()+","+"ParametricPreSave:" + dcCollectRequest.getSite() + "," + dcCollectRequest.getDataCollection()+","+ dcCollectRequest.getVersion();

        String handle = "";

        if(!StringUtils.isEmpty(dcCollectRequest.getPcu()))
            handle += "PcuBO:" + dcCollectRequest.getSite() + "," + dcCollectRequest.getPcu() + ",";

        if(!StringUtils.isEmpty(dcCollectRequest.getOperation()))
            handle += "OperationBO:" + dcCollectRequest.getSite() + "," + dcCollectRequest.getOperation() + ",";

        if(!StringUtils.isEmpty(dcCollectRequest.getWorkCenter()))
            handle += "WorkCenterBO:" + dcCollectRequest.getSite() + "," + dcCollectRequest.getWorkCenter() + ",";

        if(!StringUtils.isEmpty(dcCollectRequest.getResource()))
            handle += "ResourceBO:" + dcCollectRequest.getSite() + "," + dcCollectRequest.getResource() + ",";

        if(!StringUtils.isEmpty(dcCollectRequest.getDataCollection()) && !StringUtils.isEmpty(dcCollectRequest.getVersion()))
            handle += "ParametricPreSave:" + dcCollectRequest.getSite() + "," + dcCollectRequest.getDataCollection() + "," + dcCollectRequest.getVersion();

        DcParametricPreSave dcParametricPreSave = dcCollectRepositorySaveDraft.findByActiveAndSiteAndHandle(1,dcCollectRequest.getSite(),handle);

        if(dcParametricPreSave==null)
        {
            throw new DcCollectException(2302,dcCollectRequest.getDataCollection(),dcCollectRequest.getVersion());
        }
        return dcParametricPreSave;
    }

    @Override
    public Boolean isExists(DcCollectRequest dcCollectRequest)throws Exception
    {
//        if(dcCollectRequest.getOperation()!=null && !dcCollectRequest.getOperation().isEmpty()) {
//            OperationRequest operationRetrieveRequest = OperationRequest.builder().site(dcCollectRequest.getSite()).operation(dcCollectRequest.getOperation()).build();
//            Operation operationRecord = webClientBuilder.build()
//                    .post()
//                    .uri(operationRetrieve)
//                    .bodyValue(operationRetrieveRequest)
//                    .retrieve()
//                    .bodyToMono(Operation.class)
//                    .block();
//            if(operationRecord!=null)
//            {
//                dcCollectRequest.setOperation(operationRecord.getOperation());
//                dcCollectRequest.setOperationVersion(operationRecord.getRevision());
//            }
//        }
//        String handle = "PcuBO:"+dcCollectRequest.getSite()+","+dcCollectRequest.getPcu()+","+"OperationBO:"+dcCollectRequest.getSite()+","+dcCollectRequest.getOperation()+","+"WorkCenterBO:"+dcCollectRequest.getSite()+","+dcCollectRequest.getWorkCenter()+","+"ResourceBO:"+dcCollectRequest.getSite()+","+dcCollectRequest.getResource()+","+"ParametricMeasures:" + dcCollectRequest.getSite() + "," + dcCollectRequest.getDataCollection()+","+ dcCollectRequest.getVersion();

        String handle = "";

        if(!StringUtils.isEmpty(dcCollectRequest.getPcu()))
            handle += "PcuBO:" + dcCollectRequest.getSite() + "," + dcCollectRequest.getPcu() + ",";

        if(!StringUtils.isEmpty(dcCollectRequest.getOperation()))
            handle += "OperationBO:" + dcCollectRequest.getSite() + "," + dcCollectRequest.getOperation() + ",";

        if(!StringUtils.isEmpty(dcCollectRequest.getWorkCenter()))
            handle += "WorkCenterBO:" + dcCollectRequest.getSite() + "," + dcCollectRequest.getWorkCenter() + ",";

        if(!StringUtils.isEmpty(dcCollectRequest.getResource()))
            handle += "ResourceBO:" + dcCollectRequest.getSite() + "," + dcCollectRequest.getResource() + ",";

        if(!StringUtils.isEmpty(dcCollectRequest.getDataCollection()) && !StringUtils.isEmpty(dcCollectRequest.getVersion()))
            handle += "ParametricPreSave:" + dcCollectRequest.getSite() + "," + dcCollectRequest.getDataCollection() + "," + dcCollectRequest.getVersion();

        Integer recordCount = dcCollectRepositorySave.countByActiveAndSiteAndHandleContaining(1,dcCollectRequest.getSite(),handle);
       if(recordCount>0)
       {
           return true;
       }
        return false;
    }


    @Override
    public DcGroupResponse findDcGroupNameByName(DcCollectRequest dcCollectRequest)throws Exception
    {
        DataCollection dataCollection = retrievedDataCollection(dcCollectRequest);
        DcGroupResponse dcGroupResponse = DcGroupResponse.builder()
                .revision(dataCollection.getVersion())
                .currentRevision(dataCollection.isCurrentVersion())
                .site(dataCollection.getSite())
                .dcGroup(dataCollection.getDataCollection())
                .description(dataCollection.getDescription())
                .collectDataAt(dataCollection.getCollectDataAt())
                .collectType(dataCollection.getCollectionType())
                .passFailGroup(dataCollection.isPassOrFailGroup())
                .authenticationRequired(dataCollection.isUserAuthenticationRequired())
                .status(dataCollection.getStatus())
                .erp(dataCollection.isErpGroup())
                .dcParameterList(dataCollection.getParameterList())
                .build();
        return dcGroupResponse;
    }

    @Override
    public List<ParametricMeasures> retrieveForDataCollection(String site, String pcu)
    {
        List<ParametricMeasures> parametricMeasuresList = new ArrayList<>();
        List<DcSaveParametricMeasures> retrievedRecords = dcCollectRepositorySave.findByActiveAndSiteAndParametricMeasuresListPcuBO(1,site,"PcuBO:"+site+","+pcu);
        if(retrievedRecords!=null && !retrievedRecords.isEmpty())
        {
            for(DcSaveParametricMeasures dcSaveParametricMeasures : retrievedRecords)
            {
                parametricMeasuresList.addAll(dcSaveParametricMeasures.getParametricMeasuresList());
            }
        }
        return parametricMeasuresList;
    }

    @Override
    public List<DcGroupResponse> findDcGroupNameByResource(DcCollectRequest dcCollectRequest) throws Exception
    {
        RetrieveRequest resourceRequest = RetrieveRequest.builder().resource(dcCollectRequest.getResource()).site(dcCollectRequest.getSite()).build();
        List<DataCollection>  dataCollectionNameByResource = webClientBuilder.build()
                .post()
                .uri(getDataCollectionByResourceUrl)
                .bodyValue(resourceRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<DataCollection>>() { })
                .block();
        List<DcGroupResponse> dcGroupResponseList = new ArrayList<>();
        for(DataCollection dataCollection : dataCollectionNameByResource)
        {
            DcGroupResponse dcGroupResponse = DcGroupResponse.builder()
                    .revision(dataCollection.getVersion())
                    .currentRevision(dataCollection.isCurrentVersion())
                    .site(dataCollection.getSite())
                    .dcGroup(dataCollection.getDataCollection())
                    .description(dataCollection.getDescription())
                    .collectDataAt(dataCollection.getCollectDataAt())
                    .collectType(dataCollection.getCollectionType())
                    .passFailGroup(dataCollection.isPassOrFailGroup())
                    .authenticationRequired(dataCollection.isUserAuthenticationRequired())
                    .status(dataCollection.getStatus())
                    .erp(dataCollection.isErpGroup())
                    .dcParameterList(dataCollection.getParameterList())
                    .build();
            dcGroupResponseList.add(dcGroupResponse);
        }
        return dcGroupResponseList;
    }

    @Override
    public List<DcGroupResponse> getCurrentDataCollection(DcCollectRequest dcCollectRequest) throws Exception
    {
        RetrieveRequest currentDataCollection = RetrieveRequest.builder().site(dcCollectRequest.getSite()).build();
        List<DataCollection>  getCurrentDataCollection = webClientBuilder.build()
                .post()
                .uri(getCurrentDataCollectionUrl)
                .bodyValue(currentDataCollection)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<DataCollection>>() { })
                .block();
        List<DcGroupResponse> dcGroupResponseList = new ArrayList<>();
        for(DataCollection dataCollection : getCurrentDataCollection)
        {
            DcGroupResponse dcGroupResponse = DcGroupResponse.builder()
                    .revision(dataCollection.getVersion())
                    .currentRevision(dataCollection.isCurrentVersion())
                    .site(dataCollection.getSite())
                    .dcGroup(dataCollection.getDataCollection())
                    .description(dataCollection.getDescription())
                    .collectDataAt(dataCollection.getCollectDataAt())
                    .collectType(dataCollection.getCollectionType())
                    .passFailGroup(dataCollection.isPassOrFailGroup())
                    .authenticationRequired(dataCollection.isUserAuthenticationRequired())
                    .status(dataCollection.getStatus())
                    .erp(dataCollection.isErpGroup())
                    .dcParameterList(dataCollection.getParameterList())
                    .build();
            dcGroupResponseList.add(dcGroupResponse);
        }
        return dcGroupResponseList;
    }

    @Override
    public List<DcGroupResponse> getAllDataCollection(DcCollectRequest dcCollectRequest) throws Exception
    {
        RetrieveRequest allDataCollection = RetrieveRequest.builder().site(dcCollectRequest.getSite()).build();
        List<DataCollection>  getCurrentDataCollection = webClientBuilder.build()
                .post()
                .uri(getAllDataCollection)
                .bodyValue(allDataCollection)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<DataCollection>>() { })
                .block();
        List<DcGroupResponse> dcGroupResponseList = new ArrayList<>();
        for(DataCollection dataCollection : getCurrentDataCollection)
        {
            DcGroupResponse dcGroupResponse = DcGroupResponse.builder()
                    .revision(dataCollection.getVersion())
                    .currentRevision(dataCollection.isCurrentVersion())
                    .site(dataCollection.getSite())
                    .dcGroup(dataCollection.getDataCollection())
                    .description(dataCollection.getDescription())
                    .collectDataAt(dataCollection.getCollectDataAt())
                    .collectType(dataCollection.getCollectionType())
                    .passFailGroup(dataCollection.isPassOrFailGroup())
                    .authenticationRequired(dataCollection.isUserAuthenticationRequired())
                    .status(dataCollection.getStatus())
                    .erp(dataCollection.isErpGroup())
                    .dcParameterList(dataCollection.getParameterList())
                    .build();
            dcGroupResponseList.add(dcGroupResponse);
        }
        return dcGroupResponseList;
    }

    @Override
    public DcGroupResponse findCurrentDcGroupByName(DcCollectRequest dcCollectRequest)throws Exception
    {
        DataCollectionRequest retrieveRequest = DataCollectionRequest.builder().site(dcCollectRequest.getSite()).dataCollection(dcCollectRequest.getDataCollection()).build();
        DataCollection  getCurrentDataCollectionByName = webClientBuilder.build()
                .post()
                .uri(getCurrentDCByName)
                .bodyValue(retrieveRequest)
                .retrieve()
                .bodyToMono(DataCollection.class)
                .block();
        DcGroupResponse dcGroupResponse = DcGroupResponse.builder()
                .revision(getCurrentDataCollectionByName.getVersion())
                .currentRevision(getCurrentDataCollectionByName.isCurrentVersion())
                .site(getCurrentDataCollectionByName.getSite())
                .dcGroup(getCurrentDataCollectionByName.getDataCollection())
                .description(getCurrentDataCollectionByName.getDescription())
                .collectDataAt(getCurrentDataCollectionByName.getCollectDataAt())
                .collectType(getCurrentDataCollectionByName.getCollectionType())
                .passFailGroup(getCurrentDataCollectionByName.isPassOrFailGroup())
                .authenticationRequired(getCurrentDataCollectionByName.isUserAuthenticationRequired())
                .status(getCurrentDataCollectionByName.getStatus())
                .erp(getCurrentDataCollectionByName.isErpGroup())
                .dcParameterList(getCurrentDataCollectionByName.getParameterList())
                .build();
        return dcGroupResponse;
    }

    @Override
    public  DcGroupResponse findDcGroupByNameAndVersion(DcCollectRequest dcCollectRequest) throws Exception
    {
        DataCollectionRequest retrieveRequest = DataCollectionRequest.builder().site(dcCollectRequest.getSite()).dataCollection(dcCollectRequest.getDataCollection()).version(dcCollectRequest.getVersion()).build();
        DataCollection  getCurrentDataCollectionByNameAndVersion = webClientBuilder.build()
                .post()
                .uri(getCurrentDCByName)
                .bodyValue(retrieveRequest)
                .retrieve()
                .bodyToMono(DataCollection.class)
                .block();
        DcGroupResponse dcGroupResponse = DcGroupResponse.builder()
                .revision(getCurrentDataCollectionByNameAndVersion.getVersion())
                .currentRevision(getCurrentDataCollectionByNameAndVersion.isCurrentVersion())
                .site(getCurrentDataCollectionByNameAndVersion.getSite())
                .dcGroup(getCurrentDataCollectionByNameAndVersion.getDataCollection())
                .description(getCurrentDataCollectionByNameAndVersion.getDescription())
                .collectDataAt(getCurrentDataCollectionByNameAndVersion.getCollectDataAt())
                .collectType(getCurrentDataCollectionByNameAndVersion.getCollectionType())
                .passFailGroup(getCurrentDataCollectionByNameAndVersion.isPassOrFailGroup())
                .authenticationRequired(getCurrentDataCollectionByNameAndVersion.isUserAuthenticationRequired())
                .status(getCurrentDataCollectionByNameAndVersion.getStatus())
                .erp(getCurrentDataCollectionByNameAndVersion.isErpGroup())
                .dcParameterList(getCurrentDataCollectionByNameAndVersion.getParameterList())
                .build();
        return dcGroupResponse;
    }

}
