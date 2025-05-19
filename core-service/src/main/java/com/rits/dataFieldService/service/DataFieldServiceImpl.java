package com.rits.dataFieldService.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.rits.auditlogservice.dto.AuditLogRequest;
import com.rits.dataFieldService.dto.*;
import com.rits.dataFieldService.exception.DataFieldException;
import com.rits.dataFieldService.model.DataField;
import com.rits.dataFieldService.model.MessageDetails;
import com.rits.dataFieldService.model.MessageModel;
import com.rits.dataFieldService.repository.DataFieldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class DataFieldServiceImpl implements DataFieldService {
    private final DataFieldRepository dataFieldRepository;
    private final WebClient.Builder webClientBuilder;
    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;
//    @Value("${auditlog-service.url}/producer")
//    private String auditlogUrl;




    @Override
    public DataFieldResponseList getDataFieldListByCreationDate(String site) throws Exception {
        List<DataFieldResponse> dataFieldResponseList = dataFieldRepository.findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(1, site);
        return DataFieldResponseList.builder().dataFieldList(dataFieldResponseList).build();
    }

    @Override
    public DataFieldResponseList getDataFieldList(String dataField, String site) throws Exception {
        if (dataField != null && !dataField.isEmpty()) {
            List<DataFieldResponse> dataFieldResponseList = dataFieldRepository.findByActiveAndSiteAndDataFieldContainingIgnoreCase(1, site, dataField);

            if (dataFieldResponseList.isEmpty()) {
                throw new DataFieldException(402, dataField);
            }
            return DataFieldResponseList.builder().dataFieldList(dataFieldResponseList).build();
        } else {
            return getDataFieldListByCreationDate(site);
        }
    }

    @Override
    public DataField retrieveDataField(String dataField, String site) throws Exception {
        if (dataFieldRepository.existsByActiveAndSiteAndDataField(1, site, dataField)) {
            return dataFieldRepository.findByActiveAndSiteAndDataField(1, site, dataField);
        } else {
            throw new DataFieldException(402, dataField);
        }

    }


    @Override
    public MessageModel createDataField(DataFieldRequest dataFieldRequest) throws Exception {
        if (dataFieldRepository.existsByActiveAndSiteAndDataField(1, dataFieldRequest.getSite(), dataFieldRequest.getDataField())) {
            throw new DataFieldException(401, dataFieldRequest.getDataField());
        } else {
            if (dataFieldRequest.getDescription() == null || dataFieldRequest.getDescription().isEmpty()) {
                dataFieldRequest.setDescription(dataFieldRequest.getDataField());
            }

            DataField dataField = DataField.builder()
                    .handle("DataFieldBo:" + dataFieldRequest.getDataField() + "," + dataFieldRequest.getSite())
                    .site(dataFieldRequest.getSite())
                    .dataField(dataFieldRequest.getDataField())
                    .type(dataFieldRequest.getType())
                    .qmSelectedSet(dataFieldRequest.isQmSelectedSet())
                    .description(dataFieldRequest.getDescription())
                    .trackable(dataFieldRequest.isTrackable())
                    .fieldLabel(dataFieldRequest.getFieldLabel())
                    .maskGroup(dataFieldRequest.getMaskGroup())
                    .browseIcon(dataFieldRequest.isBrowseIcon())
                    .mfrRef(dataFieldRequest.isMfrRef())
                    .preSaveActivity(dataFieldRequest.getPreSaveActivity())
                    .listDetails(dataFieldRequest.getListDetails())
                    .createdBy(dataFieldRequest.getUserId())
                    .active(1)
                    .createdDateTime(LocalDateTime.now())
                    .build();

//            AuditLogRequest activityLog = AuditLogRequest.builder()
//                    .site(dataFieldRequest.getSite())
//                    .change_stamp("Create")
//                    .action_code("DATAFIELD-CREATE")
//                    .action_detail("DATAFIELD CREATED "+dataFieldRequest.getDataField())
//                    .action_detail_handle("ActionDetailBO:"+dataFieldRequest.getSite()+","+"DATAFIELD-CREATE"+","+dataFieldRequest.getUserId()+":"+"com.rits.dataFieldService.service")
//                    .date_time(String.valueOf(LocalDateTime.now()))
//                    .userId(dataFieldRequest.getUserId())
//                    .txnId("DATAFIELD-CREATE"+String.valueOf(LocalDateTime.now())+dataFieldRequest.getUserId())
//                    .created_date_time(String.valueOf(LocalDateTime.now()))
//                    .category("DATA_FIELD")
//                    .build();
//
//            webClientBuilder.build()
//                    .post()
//                    .uri(auditlogUrl)
//                    .bodyValue(activityLog)
//                    .retrieve()
//                    .bodyToMono(AuditLogRequest.class)
//                    .block();

            return MessageModel.builder().message_details(new MessageDetails(dataFieldRequest.getDataField()+" Created SuccessFully","S")).response(dataFieldRepository.save(dataField)).build();
        }
    }

    @Override
    public MessageModel deleteDataField(String dataField, String site,String userId) throws Exception {

        if (dataFieldRepository.existsByActiveAndSiteAndDataField(1, site, dataField)) {
            DataField existingDataField = dataFieldRepository.findByActiveAndSiteAndDataField(1, site, dataField);
            existingDataField.setActive(0);
            existingDataField.setModifiedBy(userId);
            existingDataField.setModifiedDateTime(LocalDateTime.now());
            dataFieldRepository.save(existingDataField);
//            AuditLogRequest activityLog = AuditLogRequest.builder()
//                    .site(site)
//                    .change_stamp("Delete")
//                    .action_code("DATAFIELD-DELETE")
//                    .action_detail("DATAFIELD DELETED "+dataField)
//                    .action_detail_handle("ActionDetailBO:"+site+","+"DATAFIELD-DELETE"+","+userId+":"+"com.rits.dataFieldService.service")
//                    .date_time(String.valueOf(LocalDateTime.now()))
//                    .userId(userId)
//                    .txnId("DATAFIELD-DELETE"+String.valueOf(LocalDateTime.now())+userId)
//                    .created_date_time(String.valueOf(LocalDateTime.now()))
//                    .category("DATA_FIELD")
//                    .build();
//
//            webClientBuilder.build()
//                    .post()
//                    .uri(auditlogUrl)
//                    .bodyValue(activityLog)
//                    .retrieve()
//                    .bodyToMono(AuditLogRequest.class)
//                    .block();
            return MessageModel.builder().message_details(new MessageDetails(dataField+" deleted SuccessFully","S")).build();

        } else {
            throw new DataFieldException(402, dataField);
        }
    }

    @Override
    public MessageModel updateDataField(DataFieldRequest dataFieldRequest) throws Exception {
        if (dataFieldRepository.existsByActiveAndSiteAndDataField(1, dataFieldRequest.getSite(), dataFieldRequest.getDataField())) {
            if (dataFieldRequest.getDescription() == null || dataFieldRequest.getDescription().isEmpty()) {
                dataFieldRequest.setDescription(dataFieldRequest.getDataField());
            }
            DataField existingDataField = dataFieldRepository.findByActiveAndSiteAndDataField(1, dataFieldRequest.getSite(), dataFieldRequest.getDataField());
            DataField updatedDataField = DataField.builder()
                    .handle(existingDataField.getHandle())
                    .site(existingDataField.getSite())
                    .dataField(existingDataField.getDataField())
                    .trackable(dataFieldRequest.isTrackable())
                    .type(dataFieldRequest.getType())
                    .qmSelectedSet(dataFieldRequest.isQmSelectedSet())
                    .description(dataFieldRequest.getDescription())
                    .fieldLabel(dataFieldRequest.getFieldLabel())
                    .maskGroup(dataFieldRequest.getMaskGroup())
                    .browseIcon(dataFieldRequest.isBrowseIcon())
                    .mfrRef(dataFieldRequest.isMfrRef())
                    .preSaveActivity(dataFieldRequest.getPreSaveActivity())
                    .listDetails(dataFieldRequest.getListDetails())
                    .active(1)
                    .createdDateTime(existingDataField.getCreatedDateTime())
                    .modifiedBy(dataFieldRequest.getUserId())
                    .modifiedDateTime(LocalDateTime.now())
                    .build();

//            AuditLogRequest activityLog = AuditLogRequest.builder()
//                    .site(dataFieldRequest.getSite())
//                    .change_stamp("Update")
//                    .action_code("DATAFIELD-UPDATE")
//                    .action_detail("DATAFIELD UPDATED "+dataFieldRequest.getDataField())
//                    .action_detail_handle("ActionDetailBO:"+dataFieldRequest.getSite()+","+"DATAFIELD-UPDATE"+","+dataFieldRequest.getUserId()+":"+"com.rits.dataFieldService.service")
//                    .date_time(String.valueOf(LocalDateTime.now()))
//                    .userId(dataFieldRequest.getUserId())
//                    .txnId("DATAFIELD-UPDATE"+String.valueOf(LocalDateTime.now())+dataFieldRequest.getUserId())
//                    .created_date_time(String.valueOf(LocalDateTime.now()))
//                    .category("DATA_FIELD")
//                    .build();
//
//            webClientBuilder.build()
//                    .post()
//                    .uri(auditlogUrl)
//                    .bodyValue(activityLog)
//                    .retrieve()
//                    .bodyToMono(AuditLogRequest.class)
//                    .block();

            return MessageModel.builder().message_details(new MessageDetails(dataFieldRequest.getDataField()+" updated SuccessFully","S")).response(dataFieldRepository.save(updatedDataField)).build();

        } else {
            throw new DataFieldException(402, dataFieldRequest.getDataField());
        }
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
            throw new DataFieldException(800);
        }
        return extensionResponse;
    }

    @Override
    public boolean isExist(String site, String dataField) throws Exception {
        return dataFieldRepository.existsByActiveAndSiteAndDataField(1,site,dataField);
    }
    @Override
    public boolean isTrackable(String site, String dataField) throws Exception {
        return dataFieldRepository.existsByActiveAndSiteAndDataFieldAndTrackable(1,site,dataField,true);
    }
}
