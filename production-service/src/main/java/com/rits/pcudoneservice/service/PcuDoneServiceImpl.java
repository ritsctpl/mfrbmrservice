package com.rits.pcudoneservice.service;

import com.rits.Utility.BOConverter;
import com.rits.pcudoneservice.dto.*;
import com.rits.pcudoneservice.exception.PcuDoneException;
import com.rits.pcudoneservice.model.MessageDetails;
import com.rits.pcudoneservice.model.MessageModel;
import com.rits.pcudoneservice.model.PcuDone;
import com.rits.pcudoneservice.model.PcuDoneNoBO;
import com.rits.pcudoneservice.repository.PcuDoneRepository;
import com.rits.productionlogservice.dto.ProductionLogRequest;
import com.rits.productionlogservice.model.ProductionLog;
import com.rits.productionlogservice.model.ProductionLogMongo;
import com.rits.productionlogservice.producer.ProductionLogProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PcuDoneServiceImpl implements PcuDoneService {
    private final PcuDoneRepository pcuDoneRepository;
    private final WebClient.Builder webClientBuilder;
    private final ApplicationEventPublisher eventPublisher;
    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;
    @Value("${productionlog-service.url}/producer")
    private String productionLogUrl;
    @Value("${item-service.url}/retrieve")
    private String retrieveItemUrl;
    @Value("${shoporder-service.url}/retrieve")
    private String retrieveShopOrderUrl;
    @Value("${bom-service.url}/retrieve")
    private String retrieveBomUrl;
    @Value("${datatype-service.url}/retrieve")
    private String retrieveDataTypeUrl;
    @Value("${inventory-service.url}/create")
    private String createDataTypeUrl;
//    @Value("${shift-service.url}/getBreakHours")
//    private String getShiftBreakHoursUrl;
    @Value("${productionlog-service.url}/retrieveFirstPcuRecord")
    private String retrieveFirstStartLogUrl;

    @Override
    public PcuDoneRequest convertToPcuDoneRequest(PcuDoneRequestNoBO requestNoBO) {
        PcuDoneRequest request = new PcuDoneRequest();

        request.setSite(requestNoBO.getSite());

        if (requestNoBO.getPcu() != null && !requestNoBO.getPcu().isEmpty()) {
            request.setPcuBO(BOConverter.retrievePcuBO(requestNoBO.getSite(), requestNoBO.getPcu()));
        }

        if (requestNoBO.getItem() != null && !requestNoBO.getItem().isEmpty()) {
            request.setItemBO(BOConverter.retrieveItemBO(requestNoBO.getSite(), requestNoBO.getItem(), requestNoBO.getItemVersion()));
        }

        if (requestNoBO.getRouter() != null && !requestNoBO.getRouter().isEmpty()) {
            request.setRouterBO(BOConverter.retrieveRouterBO(requestNoBO.getSite(), requestNoBO.getRouter(), requestNoBO.getRouterVersion()));
        }

        if (requestNoBO.getUser() != null && !requestNoBO.getUser().isEmpty()) {
            request.setUserBO(BOConverter.retrieveUserBO(requestNoBO.getSite(), requestNoBO.getUser()));
        }

        if (requestNoBO.getShopOrder() != null && !requestNoBO.getShopOrder().isEmpty()) {
            request.setShopOrderBO(BOConverter.retrieveShopOrderBO(requestNoBO.getSite(), requestNoBO.getShopOrder()));
        }

        if (requestNoBO.getOperation() != null && !requestNoBO.getOperation().isEmpty()) {
            request.setOperationBO(BOConverter.retrieveOperationBO(requestNoBO.getSite(), requestNoBO.getOperation(), requestNoBO.getOperationVersion()));
        }

        if (requestNoBO.getResource() != null && !requestNoBO.getResource().isEmpty()) {
            request.setResourceBO(BOConverter.retriveResourceBO(requestNoBO.getSite(), requestNoBO.getResource()));
        }

        if (requestNoBO.getQtyDone() != null && !requestNoBO.getQtyDone().isEmpty()) {
            request.setQtyDone(requestNoBO.getQtyDone());
        }

        if (requestNoBO.getWorkCenter() != null && !requestNoBO.getWorkCenter().isEmpty()) {
            request.setWorkCenter(requestNoBO.getWorkCenter());
        }

        return request;
    }

    @Override
    public  PcuDoneNoBO convertToPcuDoneNoBO(PcuDone pcuDone) {
        if (pcuDone == null) {
            throw new IllegalArgumentException("PcuDone cannot be null");
        }

        PcuDoneNoBO pcuDoneNoBO = new PcuDoneNoBO();

        if (pcuDone.getSite() != null && !pcuDone.getSite().isEmpty()) {
            pcuDoneNoBO.setSite(pcuDone.getSite());
        }

        if (pcuDone.getHandle() != null && !pcuDone.getHandle().isEmpty()) {
            pcuDoneNoBO.setHandle(pcuDone.getHandle());
        }

        pcuDoneNoBO.setDateTime(pcuDone.getDateTime());

        if (pcuDone.getPcuBO() != null && !pcuDone.getPcuBO().isEmpty()) {
            pcuDoneNoBO.setPcu(BOConverter.getPcu(pcuDone.getPcuBO()));
        }

        if (pcuDone.getItemBO() != null && !pcuDone.getItemBO().isEmpty()) {
            pcuDoneNoBO.setItem(BOConverter.getItem(pcuDone.getItemBO()));
        }

        if (pcuDone.getRouterBO() != null && !pcuDone.getRouterBO().isEmpty()) {
            pcuDoneNoBO.setRouter(BOConverter.getRouter(pcuDone.getRouterBO()));
        }

        if (pcuDone.getUserBO() != null && !pcuDone.getUserBO().isEmpty()) {
            pcuDoneNoBO.setUser(BOConverter.getUser(pcuDone.getUserBO()));
        }

        if (pcuDone.getQtyDone() != null && !pcuDone.getQtyDone().isEmpty()) {
            pcuDoneNoBO.setQtyDone(pcuDone.getQtyDone());
        }

        if (pcuDone.getShopOrderBO() != null && !pcuDone.getShopOrderBO().isEmpty()) {
            pcuDoneNoBO.setShopOrder(BOConverter.getShopOrder(pcuDone.getShopOrderBO()));
        }

        pcuDoneNoBO.setActive(pcuDone.getActive());

        if (pcuDone.getWorkCenter() != null && !pcuDone.getWorkCenter().isEmpty()) {
            pcuDoneNoBO.setWorkCenter(pcuDone.getWorkCenter());
        }

        return pcuDoneNoBO;
    }

    @Override
    public MessageModel insert(PcuDoneRequest pcuDoneRequest) throws Exception {

         String itembo[]=pcuDoneRequest.getItemBO().split(",");
        String item=itembo[1];
        String version=itembo[2];
        String shopOrder[]=pcuDoneRequest.getShopOrderBO().split(",");
        InventoryLocation inventoryLocation=InventoryLocation.builder()
                .shopOrder(shopOrder[1])
                .workCenter(pcuDoneRequest.getWorkCenter())
                .build();
        List<InventoryLocation> inventoryLocationList=new ArrayList<>();
        inventoryLocationList.add(inventoryLocation);

       /* GetAssemblyDataTypeRequest getAssemblyDataTypeRequest=GetAssemblyDataTypeRequest.builder().site(pcuDoneRequest.getSite()).shopOrder(shopOrder[1]).build();

        ShopOrder shopOrderResponse = webClientBuilder.build()
                .post()
                .uri(retrieveShopOrderUrl)
                .bodyValue(getAssemblyDataTypeRequest)
                .retrieve()
                .bodyToMono(ShopOrder.class)
                .block();
        if (shopOrderResponse == null||shopOrderResponse.getShopOrder()==null) {
            throw new PcuDoneException(3200,getAssemblyDataTypeRequest.getShopOrder());
        }

        getAssemblyDataTypeRequest.setItem(item);
        getAssemblyDataTypeRequest.setRevision(version);
        Item itemResponse = webClientBuilder.build()
                .post()
                .uri(retrieveItemUrl)
                .bodyValue(getAssemblyDataTypeRequest)
                .retrieve()
                .bodyToMono(Item.class)
                .block();
        if (itemResponse == null||itemResponse.getItem()==null) {
            throw new PcuDoneException(3209,getAssemblyDataTypeRequest.getItem());
        }

        String bom=null;
        String bomRevision=null;
        if(shopOrderResponse.getPlannedBom()==null||shopOrderResponse.getPlannedBom().isEmpty()|| shopOrderResponse.getPlannedBom().equals(" ")){
            if(itemResponse.getBom()!=null){
                bom=itemResponse.getBom();
                bomRevision=itemResponse.getBomVersion();
            }
        }else{
            bom=shopOrderResponse.getPlannedBom();
            bomRevision=shopOrderResponse.getBomVersion();
        }


        getAssemblyDataTypeRequest.setBom(bom);
        getAssemblyDataTypeRequest.setRevision(bomRevision);
        Bom bomResponse = webClientBuilder.build()
                .post()
                .uri(retrieveBomUrl)
                .bodyValue(getAssemblyDataTypeRequest)
                .retrieve()
                .bodyToMono(Bom.class)
                .block();
        if (bomResponse == null||bomResponse.getBom()==null) {
            throw new PcuDoneException(200,bom,bomRevision);
        }
        String assemblyDataType=null;
        for( BomComponent bomComponent:bomResponse.getBomComponentList()){
            if(bomComponent.getComponent().equals(item)){
                assemblyDataType=bomComponent.getAssemblyDataTypeBo();
                break;
            }
        }
        if(assemblyDataType==null||assemblyDataType.isEmpty()||assemblyDataType.equals(" ")){
            assemblyDataType=itemResponse.getAssemblyDataType();
        }
        getAssemblyDataTypeRequest.setDataType(assemblyDataType);
        getAssemblyDataTypeRequest.setCategory("Assembly");
       DataType dataTypeResponse = webClientBuilder.build()
                .post()
                .uri(retrieveDataTypeUrl)
                .bodyValue(getAssemblyDataTypeRequest)
                .retrieve()
                .bodyToMono(DataType.class)
                .block();



        if (dataTypeResponse == null && dataTypeResponse.getDataFieldList().isEmpty()) {
            throw new PcuDoneException(4307, getAssemblyDataTypeRequest.getDataType());
        }
        List<InventoryDataDetails> inventoryDataDetails = new ArrayList<>();
        if(dataTypeResponse != null && dataTypeResponse.getDataFieldList()!=null && !dataTypeResponse.getDataFieldList().isEmpty()) {
            List<String> dataFieldName = new ArrayList<>();
            for (DataField dataField : dataTypeResponse.getDataFieldList()) {
                dataFieldName.add(dataField.getDataField());
            }

            int sequence = 1;
            for (String dataField : dataFieldName) {
                InventoryDataDetails inventoryDataDetail = InventoryDataDetails.builder().dataField(dataField).sequence(sequence).build();
                inventoryDataDetails.add(inventoryDataDetail);
                sequence++;
            }
        }*/
        String inventoryBO[]=pcuDoneRequest.getPcuBO().split(",");
        InventoryRequest request=InventoryRequest.builder()
                .site(pcuDoneRequest.getSite())
                .inventoryId(inventoryBO[1])
                .qty(Double.parseDouble(pcuDoneRequest.getQtyDone()))
                .remainingQty(pcuDoneRequest.getQtyDone())
                .status("active")
                .item(item)
                .version(version)
                .inventoryIdLocation(inventoryLocationList)
                //        .inventoryIdDataDetails(inventoryDataDetails)
                .build();
        MessageModel inventoryMessageModel = webClientBuilder.build()
                .post()
                .uri(createDataTypeUrl)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(MessageModel.class)
                .block();



        if (inventoryMessageModel == null ||inventoryMessageModel.getMessage_details()==null|| inventoryMessageModel.getMessage_details().getMsg_type()==null) {
            throw new PcuDoneException(3224, request.getInventoryId());
        }
        PcuDone pcuDone = PcuDone.builder()
                .site(pcuDoneRequest.getSite())
                .handle("PCUDoneBo:"+pcuDoneRequest.getSite()+","+pcuDoneRequest.getPcuBO())
                .dateTime(LocalDateTime.now())
                .pcuBO(pcuDoneRequest.getPcuBO())
                .qtyDone(pcuDoneRequest.getQtyDone())
                .routerBO(pcuDoneRequest.getRouterBO())
                .userBO(pcuDoneRequest.getUserBO())
                .itemBO(pcuDoneRequest.getItemBO())
                .workCenter(pcuDoneRequest.getWorkCenter())
                .shopOrderBO(pcuDoneRequest.getShopOrderBO())
                .active(1)
                .build();

        PcuDone savedPcuDone = pcuDoneRepository.save(pcuDone);

        PcuDoneNoBO pcuDoneNoBO = convertToPcuDoneNoBO(savedPcuDone);

        MessageModel messageModel = MessageModel.builder().response(pcuDoneNoBO).message_details(new MessageDetails(pcuDoneNoBO.getPcu() + " Done SuccessFully", "S")).build();
//        MinutesList shiftRecordList = getShiftBreakHours(pcuDoneRequest.getSite());
//        Minutes minutesRecord = null;
//        for(Minutes shift : shiftRecordList.getMinutesList())
//        {
//            if(shift.getShiftType().equalsIgnoreCase("general"))
//            {
//                minutesRecord = shift;
//                break;
//            }
//        }
        ProductionLog retrievedRecord =  retrieveFirstStartLog(pcuDoneRequest.getPcuBO(),pcuDoneRequest.getShopOrderBO(), pcuDoneRequest.getSite());
        ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                .eventType("doneSfcBatch")
                .userId(pcuDoneRequest.getUserBO())
                .pcu(pcuDoneRequest.getPcuBO())
                .batchNo((pcuDoneRequest.getPcuBO() != null) ? BOConverter.getPcu(pcuDoneRequest.getPcuBO()) : "")
                .orderNumber((pcuDoneRequest.getShopOrderBO() != null) ? BOConverter.getShopOrder(pcuDoneRequest.getShopOrderBO()) : "")
                .shopOrderBO(pcuDoneRequest.getShopOrderBO())
                .routerBO(pcuDoneRequest.getRouterBO())
                .itemBO(pcuDoneRequest.getItemBO())
                .qty((int) Double.parseDouble(pcuDoneRequest.getQtyDone()))
                .operation_bo(pcuDoneRequest.getOperationBO())
                .resourceId(pcuDoneRequest.getResourceBO())
//                .shiftName(minutesRecord.getShiftName())
//                .shiftStartTime(minutesRecord.getStartTime().toString())
//                .shiftEndTime(minutesRecord.getEndTime().toString())
                .site(pcuDoneRequest.getSite())
//                .totalBreakHours(String.valueOf(minutesRecord.getMinutes()))
                .topic("production-log")
                .status("Done")
                .eventData(String.valueOf(messageModel.getMessage_details().getMsg()))
                .build();
        if(retrievedRecord != null)
        {
            long manufacturedTime = Duration.between(retrievedRecord.getCreated_datetime(),LocalDateTime.now()).toSeconds();
            productionLogRequest.setManufactureTime(Double.valueOf(manufacturedTime));
        }
        eventPublisher.publishEvent(new ProductionLogProducer(productionLogRequest));

        return messageModel;
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
            throw new PcuDoneException(800);
        }
        return extensionResponse;
    }
    public ProductionLog retrieveFirstStartLog(String pcuBO, String shopOrderBO, String site)
    {
        ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                .eventType("BATCH_START")
                .site(site)
                .pcu(BOConverter.getPcu(pcuBO))
                .shopOrderBO(BOConverter.getShopOrder(shopOrderBO))
                .build();
        ProductionLog retrievedRecord = webClientBuilder.build()
                .post()
                .uri(retrieveFirstStartLogUrl)
                .bodyValue(productionLogRequest)
                .retrieve()
                .bodyToMono(ProductionLog.class)
                .block();
        return retrievedRecord;
    }

    @Override
    public Boolean delete(String site, String pcuBO)
    {
        Boolean isDeleted = false;
        PcuDone pcuDone = pcuDoneRepository.findByActiveAndSiteAndHandle(1,site,"PCUDoneBo:"+site+","+pcuBO);
        if(pcuDone!=null)
        {
            pcuDone.setActive(0);
            pcuDoneRepository.save(pcuDone);
            isDeleted = true;
        }
        return isDeleted;
    }


    @Override
    public Boolean unDelete(String site, String pcuBO)
    {
        Boolean isDeleted = false;
        PcuDone pcuDone = pcuDoneRepository.findByActiveAndSiteAndHandle(0,site,"PCUDoneBo:"+site+","+pcuBO);
        if(pcuDone!=null)
        {
            pcuDone.setActive(1);
            pcuDoneRepository.save(pcuDone);
            isDeleted = true;
        }
        return isDeleted;
    }

    @Override
    public PcuDone retrieve(String site, String pcuBO) {
        if(pcuDoneRepository.existsByActiveAndSiteAndPcuBO(1,site,pcuBO)){
            return pcuDoneRepository.findByActiveAndSiteAndPcuBO(1,site,pcuBO);
        }else{
           throw new PcuDoneException(4102,pcuBO);
        }

    }

//    public MinutesList getShiftBreakHours(String site)
//    {
//        ShiftRequest retrieveShiftRequest = ShiftRequest.builder().site(site).build();
//        MinutesList retrievedRecord = webClientBuilder.build()
//                .post()
//                .uri(getShiftBreakHoursUrl)
//                .bodyValue(retrieveShiftRequest)
//                .retrieve()
//                .bodyToMono(MinutesList.class)
//                .block();
//        return retrievedRecord;
//    }
}
