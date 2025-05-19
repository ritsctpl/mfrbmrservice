package com.rits.datatypeservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.rits.auditlogservice.dto.AuditLogRequest;
import com.rits.datatypeservice.dto.*;
import com.rits.datatypeservice.exception.DataTypeException;
import com.rits.datatypeservice.model.DataType;
import com.rits.datatypeservice.model.MessageDetails;
import com.rits.datatypeservice.model.MessageModel;
import com.rits.datatypeservice.repository.DataTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DataTypeServiceImpl implements DataTypeService{

    private final DataTypeRepository dataTypeRepository;
    private final WebClient.Builder webClientBuilder;
    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;
    @Value("${bom-service.url}/retrieve")
    private String retrieveBomUrl;
    @Value("${item-service.url}/retrieve")
    private String retrieveItemUrl;

//    @Value("${auditlog-service.url}/producer")
//    private String auditlogUrl;

    @Override
    public MessageModel createDataType(DataTypeRequest dataTypeRequest) throws Exception{

        long recordPresent = dataTypeRepository.countByDataTypeAndCategoryAndSiteAndActive(dataTypeRequest.getDataType(),dataTypeRequest.getCategory(), dataTypeRequest.getSite(), 1);
        if (recordPresent > 0) {
            throw new DataTypeException(1000, dataTypeRequest.getDataType());
        }
        if(dataTypeRequest.getDescription()==null || dataTypeRequest.getDescription().isEmpty()){
            dataTypeRequest.setDescription(dataTypeRequest.getDataType());
        }
        DataType dataType=DataType.builder()
                .site(dataTypeRequest.getSite())
                .dataType(dataTypeRequest.getDataType())
                .handle("DataTypeBO:" + dataTypeRequest.getSite() + "," + dataTypeRequest.getDataType())
                .category(dataTypeRequest.getCategory())
                .description(dataTypeRequest.getDescription())
                .dataFieldList(dataTypeRequest.getDataFieldList())
                .preSaveActivity(dataTypeRequest.getPreSaveActivity())
                .createdBy(dataTypeRequest.getUserId())
                .active(1)
                .createdDateTime(LocalDateTime.now())
                .build();

//        AuditLogRequest activityLog = AuditLogRequest.builder()
//                .site(dataTypeRequest.getSite())
//                .change_stamp("Create")
//                .action_code("DATATYPE-CREATE")
//                .action_detail("DATATYPE CREATED "+dataTypeRequest.getDataType())
//                .action_detail_handle("ActionDetailBO:"+dataTypeRequest.getSite()+","+"DATATYPE-CREATE"+","+dataTypeRequest.getUserId()+":"+"com.rits.datatypeservice.service")
//                .date_time(String.valueOf(LocalDateTime.now()))
//                .userId(dataTypeRequest.getUserId())
//                .txnId("DATATYPE-CREATE"+String.valueOf(LocalDateTime.now())+dataTypeRequest.getUserId())
//                .created_date_time(String.valueOf(LocalDateTime.now()))
//                .category("DATA_TYPE")
//                .build();
//
//        webClientBuilder.build()
//                .post()
//                .uri(auditlogUrl)
//                .bodyValue(activityLog)
//                .retrieve()
//                .bodyToMono(AuditLogRequest.class)
//                .block();
        return MessageModel.builder().message_details(new MessageDetails("Created SuccessFull","S")).response(dataTypeRepository.save(dataType)).build();
    }

    @Override
    public MessageModel updateDataType(DataTypeRequest dataTypeRequest) throws Exception {
        DataType dataType = dataTypeRepository.findByDataTypeAndCategoryAndActiveAndSite(dataTypeRequest.getDataType(), dataTypeRequest.getCategory(),1,dataTypeRequest.getSite());
        if (dataType != null) {
            if(dataTypeRequest.getDescription()==null || dataTypeRequest.getDescription().isEmpty()){
                dataTypeRequest.setDescription(dataTypeRequest.getDataType());
            }
            dataType = DataType.builder()
                    .id(dataType.getId())
                    .site(dataType.getSite())
                    .dataType(dataType.getDataType())
                    .handle("DataTypeBO:" + dataType.getSite() + "," + dataType.getDataType())
                    .category(dataTypeRequest.getCategory())
                    .description(dataTypeRequest.getDescription())
                    .dataFieldList(dataTypeRequest.getDataFieldList())
                    .preSaveActivity(dataTypeRequest.getPreSaveActivity())
                    .modifiedBy(dataTypeRequest.getUserId())
                    .active(dataType.getActive())
                    .createdDateTime(dataType.getCreatedDateTime())
                    .modifiedDateTime(LocalDateTime.now())
                    .build();

//            AuditLogRequest activityLog = AuditLogRequest.builder()
//                    .site(dataTypeRequest.getSite())
//                    .change_stamp("Update")
//                    .action_code("DATATYPE-UPDATE")
//                    .action_detail("DATATYPE UPDATED "+dataTypeRequest.getDataType())
//                    .action_detail_handle("ActionDetailBO:"+dataTypeRequest.getSite()+","+"DATATYPE-UPDATE"+","+dataTypeRequest.getUserId()+":"+"com.rits.datatypeservice.service")
//                    .date_time(String.valueOf(LocalDateTime.now()))
//                    .userId(dataTypeRequest.getUserId())
//                    .txnId("DATATYPE-UPDATE"+String.valueOf(LocalDateTime.now())+dataTypeRequest.getUserId())
//                    .created_date_time(String.valueOf(LocalDateTime.now()))
//                    .category("DATA_TYPE")
//                    .build();
//
//            webClientBuilder.build()
//                    .post()
//                    .uri(auditlogUrl)
//                    .bodyValue(activityLog)
//                    .retrieve()
//                    .bodyToMono(AuditLogRequest.class)
//                    .block();

            return MessageModel.builder().message_details(new MessageDetails("Updated SuccessFull","S")).response(dataTypeRepository.save(dataType)).build();
        }
        throw new DataTypeException(1001, dataTypeRequest.getDataType());
    }

    @Override
    public DataTypeResponseList getDataTypeListByCreationDate(DataTypeRequest dataTypeRequest) throws Exception {
        List<DataTypeResponse> dataTypeResponses;

        if (dataTypeRequest.getCategory() != null && !dataTypeRequest.getCategory().isEmpty()) {
            dataTypeResponses = dataTypeRepository.findByCategoryAndActiveAndSite( dataTypeRequest.getCategory(), 1,dataTypeRequest.getSite());
        } else {
            dataTypeResponses = dataTypeRepository.findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(1, dataTypeRequest.getSite());
        }

        if (dataTypeResponses != null && !dataTypeResponses.isEmpty()) {
            return DataTypeResponseList.builder().dataTypeList(dataTypeResponses).build();
        } else {
            throw new DataTypeException(1004, dataTypeRequest.getCategory());
        }
    }




    @Override
    public DataTypeResponseList getDataTypeList(DataTypeRequest dataTypeRequest) throws Exception {
        if (dataTypeRequest.getDataType() == null || dataTypeRequest.getDataType().isEmpty()) {
            return  getDataTypeListByCreationDate(dataTypeRequest);
        } else {
            List<DataTypeResponse> dataTypeResponses = dataTypeRepository.findByDataTypeContainingIgnoreCaseAndCategoryAndSiteAndActive(dataTypeRequest.getDataType(),dataTypeRequest.getCategory(), dataTypeRequest.getSite(), 1);
            if (dataTypeResponses != null && !dataTypeResponses.isEmpty()) {
                return DataTypeResponseList.builder().dataTypeList(dataTypeResponses).build();
            } else {
                throw new DataTypeException(1001, dataTypeRequest.getDataType());
            }
        }
    }
    @Override
    public DataTypeResponseList retrieveAllBySite(DataTypeRequest dataTypeRequest) throws Exception {
        List<DataTypeResponse> dataTypeResponses = dataTypeRepository.findByCategoryAndActiveAndSite(dataTypeRequest.getCategory(),1, dataTypeRequest.getSite());
        if (dataTypeResponses != null && !dataTypeResponses.isEmpty()) {
            return DataTypeResponseList.builder().dataTypeList(dataTypeResponses).build();
        } else {
            throw new DataTypeException(1001, dataTypeRequest.getDataType());
        }
    }

    @Override
    public DataType retrieveDataType(DataTypeRequest dataTypeRequest) throws Exception {
        DataType dataTypes= dataTypeRepository.findByDataTypeAndCategoryAndSiteAndActive(dataTypeRequest.getDataType(),dataTypeRequest.getCategory(), dataTypeRequest.getSite(), 1);
        if (dataTypes != null) {
            return dataTypes;
        } else {
            throw new DataTypeException(1001, dataTypeRequest.getDataType());
        }
    }


    @Override
    public MessageModel deleteDataType(DataTypeRequest dataTypeRequest) throws Exception {
        if (dataTypeRepository.existsByDataTypeAndSiteAndActive(dataTypeRequest.getDataType(), dataTypeRequest.getSite(), 1)) {
            DataType existingDataType = dataTypeRepository.findByDataTypeAndCategoryAndActive(dataTypeRequest.getDataType(),dataTypeRequest.getCategory(),1);
            existingDataType.setActive(0);
            existingDataType.setModifiedDateTime(LocalDateTime.now());
            existingDataType.setModifiedBy(dataTypeRequest.getUserId());
            dataTypeRepository.save(existingDataType);

//            AuditLogRequest activityLog = AuditLogRequest.builder()
//                    .site(dataTypeRequest.getSite())
//                    .change_stamp("Delete")
//                    .action_code("DATATYPE-DELETE")
//                    .action_detail("DATATYPE DELETED "+dataTypeRequest.getDataType())
//                    .action_detail_handle("ActionDetailBO:"+dataTypeRequest.getSite()+","+"DATATYPE-DELETE"+","+dataTypeRequest.getUserId()+":"+"com.rits.datatypeservice.service")
//                    .date_time(String.valueOf(LocalDateTime.now()))
//                    .userId(dataTypeRequest.getUserId())
//                    .txnId("DATATYPE-DELETE"+String.valueOf(LocalDateTime.now())+dataTypeRequest.getUserId())
//                    .created_date_time(String.valueOf(LocalDateTime.now()))
//                    .category("DATA_TYPE")
//                    .build();
//
//            webClientBuilder.build()
//                    .post()
//                    .uri(auditlogUrl)
//                    .bodyValue(activityLog)
//                    .retrieve()
//                    .bodyToMono(AuditLogRequest.class)
//                    .block();
            return MessageModel.builder().message_details(new MessageDetails(dataTypeRequest.getDataType()+" Deleted SuccessFull","S")).build();

        } else {
            throw new DataTypeException(1001, dataTypeRequest.getDataType());
        }
    }

    @Override
    public Boolean isDataTypeExist(DataTypeRequest dataTypeRequest) throws Exception {
        return dataTypeRepository.existsByDataTypeAndCategoryAndSiteAndActive(dataTypeRequest.getDataType(),dataTypeRequest.getCategory(),dataTypeRequest.getSite() ,1);

    }

    @Override
    public DataFieldResponseList getDataFieldList(DataTypeRequest dataTypeRequest) throws Exception {
        DataType dataType = dataTypeRepository.findByDataTypeAndCategoryAndActiveAndSite(dataTypeRequest.getDataType(),dataTypeRequest.getCategory(), 1, dataTypeRequest.getSite());
        if (dataType == null) {
            throw new DataTypeException(1001, dataTypeRequest.getDataType());
        }
        else if (dataType.getDataFieldList() != null && !dataType.getDataFieldList().isEmpty()) {

            List<DataFieldResponse> dataFieldResponses = dataType.getDataFieldList()
                    .stream()
                    .map(dataFieldList -> DataFieldResponse.builder()
                            .dataField(dataFieldList.getDataField())
                            .required(dataFieldList.isRequired())
                            .sequence(dataFieldList.getDataField())
                            .build())
                    .collect(Collectors.toList());

            return DataFieldResponseList.builder()
                    .dataFieldList(dataFieldResponses)
                    .build();
        } else {
            throw new DataTypeException(1003, dataTypeRequest.getDataType());
        }
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
            throw new DataTypeException(800);
        }
        return extensionResponse;
    }
    @Override
    public DataType  retrieveDataType(String site,String bom , String revision, String component,String category) throws Exception{
        BomRequest bomRequest = BomRequest.builder().site(site).bom(bom).revision(revision).build();
        Bom bomResponse = webClientBuilder.build()
                .post()
                .uri(retrieveBomUrl)
                .bodyValue(bomRequest)
                .retrieve()
                .bodyToMono(Bom.class)
                .block();
        if (bomResponse == null || bomResponse.getBomComponentList().isEmpty()) {
            throw new DataTypeException(200, bom, revision);
        }
        String dataType = null;

        for (BomComponent bomComponent : bomResponse.getBomComponentList())
            if (bomComponent.getComponent().equalsIgnoreCase(component)) {
                if(bomComponent.getAssemblyDataTypeBo()==null|| bomComponent.getAssemblyDataTypeBo().isEmpty())
                {
                    IsExist isExist = IsExist.builder().site(site).item(component).build();
                    Item item = retrieveItem(isExist);
                    dataType = item.getAssemblyDataType();
                } else {
                    dataType = bomComponent.getAssemblyDataTypeBo();
                }
            }
        if(dataType==null||dataType.isEmpty()){
            IsExist isExist = IsExist.builder().site(site).item(component).build();
            Item item = retrieveItem(isExist);
            dataType = item.getAssemblyDataType();
        }

        DataTypeRequest dataTypeExist = DataTypeRequest.builder().site(site).dataType(dataType).category(category).build();


        DataType dataTypeResponse = retrieveDataType(dataTypeExist);



        if (dataTypeResponse == null && dataTypeResponse.getDataFieldList().isEmpty()) {
            throw new DataTypeException(1001, dataTypeExist.getDataType());
        }
        return dataTypeResponse;
    }
    private Item retrieveItem(IsExist isExist) {
        Item item = webClientBuilder.build()
                .post()
                .uri(retrieveItemUrl)
                .bodyValue(isExist)
                .retrieve()
                .bodyToMono(Item.class)
                .block();
        if(item==null || item.getItem()==null){
            throw new DataTypeException(500,isExist.getItem());
        }
        return item;
    }
}
