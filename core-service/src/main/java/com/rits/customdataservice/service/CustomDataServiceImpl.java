package com.rits.customdataservice.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.rits.auditlogservice.dto.AuditLogRequest;
import com.rits.customdataservice.Exception.CustomDataException;
import com.rits.customdataservice.dto.*;
import com.rits.customdataservice.model.MessageDetails;
import com.rits.customdataservice.model.MessageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import com.rits.customdataservice.model.CustomData;
import com.rits.customdataservice.model.CustomDataList;
import com.rits.customdataservice.repository.CustomDataRepository;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class CustomDataServiceImpl implements CustomDataService {

    private final CustomDataRepository repository;
    private final WebClient.Builder webClientBuilder;

    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;

//    @Value("${auditlog-service.url}/producer")
//    private String auditlogUrl;


    @Override
    public MessageModel createCustomData(CustomDataRequest customDataRequest) throws Exception {
        CustomData customData = CustomData.builder()
                .site(customDataRequest.getSite())
                .handle("CustomDataBO:" + customDataRequest.getSite() + "," + customDataRequest.getCategory())
                .category(customDataRequest.getCategory())
                .createdBy(customDataRequest.getUserId())
                .customDataList(customDataRequest.getCustomDataList())
                .active(1)
                .createdDateTime(LocalDateTime.now())
                .build();

//        AuditLogRequest activityLog = AuditLogRequest.builder()
//                .site(customDataRequest.getSite())
//                .change_stamp("Create")
//                .action_code("CUSTOMDATA-CREATE")
//                .action_detail("CUSTOMDATA CREATED "+customDataRequest.getCategory())
//                .action_detail_handle("ActionDetailBO:"+customDataRequest.getSite()+","+"CUSTOMDATA-CREATE"+","+customDataRequest.getUserId()+":"+"com.rits.customdataservice.service")
//                .date_time(String.valueOf(LocalDateTime.now()))
//                .userId(customDataRequest.getUserId())
//                .txnId("CUSTOMDATA-CREATE"+String.valueOf(LocalDateTime.now())+customDataRequest.getUserId())
//                .created_date_time(String.valueOf(LocalDateTime.now()))
//                .build();
//
//        webClientBuilder.build()
//                .post()
//                .uri(auditlogUrl)
//                .bodyValue(activityLog)
//                .retrieve()
//                .bodyToMono(AuditLogRequest.class)
//                .block();
        if (customData.getCategory() != "" && !repository.existsBySiteAndCategoryAndActiveEquals(customDataRequest.getSite(), customDataRequest.getCategory(), 1)) {
            return MessageModel.builder().message_details(new MessageDetails(customDataRequest.getCategory() + " Created SuccessFully", "S")).response(repository.save(customData)).build();
        } else {
            throw new CustomDataException(2200, customDataRequest.getCategory());
        }
    }

    @Override
    public List<CustomDataList> retrieveCustomDataListByCategory(CustomDataRequest customDataRequest) throws Exception {
        List<CustomDataList> customDataList = new ArrayList<>();
        if (repository.existsBySiteAndCategoryAndActiveEquals(customDataRequest.getSite(), customDataRequest.getCategory(), 1)) {
            CustomData existingRecord = repository.findBySiteAndCategoryAndActiveEquals(customDataRequest.getSite(), customDataRequest.getCategory(), 1);
            customDataList = existingRecord.getCustomDataList();
            return customDataList;
        } else {
            return customDataList;
        }
    }

    @Override
    public MessageModel updateCustomData(CustomDataRequest customDataRequest) throws Exception {
        if (repository.existsBySiteAndCategoryAndActiveEquals(customDataRequest.getSite(), customDataRequest.getCategory(), 1)) {
            CustomData existingRecord = repository.findBySiteAndCategoryAndActiveEquals(customDataRequest.getSite(), customDataRequest.getCategory(), 1);
            CustomData updatedCustomData = CustomData.builder()
                    .site(existingRecord.getSite())
                    .handle(existingRecord.getHandle())
                    .active(1)
                    .createdDateTime(existingRecord.getCreatedDateTime())
                    .customDataList(customDataRequest.getCustomDataList())
                    .category(customDataRequest.getCategory())
                    .modifiedBy(customDataRequest.getUserId())
                    .modifiedDateTime(LocalDateTime.now())
                    .build();


//            AuditLogRequest activityLog = AuditLogRequest.builder()
//                    .site(customDataRequest.getSite())
//                    .change_stamp("Update")
//                    .action_code("CUSTOMDATA-UPDATE")
//                    .action_detail("CUSTOMDATA UPDATED "+customDataRequest.getCategory())
//                    .action_detail_handle("ActionDetailBO:"+customDataRequest.getSite()+","+"CUSTOMDATA-UPDATE"+","+customDataRequest.getUserId()+":"+"com.rits.customdataservice.service")
//                    .date_time(String.valueOf(LocalDateTime.now()))
//                    .userId(customDataRequest.getUserId())
//                    .txnId("CUSTOMDATA-UPDATE"+String.valueOf(LocalDateTime.now())+customDataRequest.getUserId())
//                    .created_date_time(String.valueOf(LocalDateTime.now()))
//                    .build();
//
//            webClientBuilder.build()
//                    .post()
//                    .uri(auditlogUrl)
//                    .bodyValue(activityLog)
//                    .retrieve()
//                    .bodyToMono(AuditLogRequest.class)
//                    .block();
            return MessageModel.builder().message_details(new MessageDetails(customDataRequest.getCategory() + " updated SuccessFully", "S")).response(repository.save(updatedCustomData)).build();
        } else {
            return createCustomData(customDataRequest);
        }
    }

    @Override
    public MessageModel deleteCustomData(CustomDataRequest customDataRequest) throws Exception {
        if (repository.existsBySiteAndCategoryAndActiveEquals(customDataRequest.getSite(), customDataRequest.getCategory(), 1)) {
            CustomData existingRecord = repository.findBySiteAndCategoryAndActiveEquals(customDataRequest.getSite(), customDataRequest.getCategory(), 1);
            existingRecord.setActive(0);
            existingRecord.setModifiedDateTime(LocalDateTime.now());
            existingRecord.setModifiedBy(customDataRequest.getUserId());
            repository.save(existingRecord);

//            AuditLogRequest activityLog = AuditLogRequest.builder()
//                    .site(customDataRequest.getSite())
//                    .change_stamp("Delete")
//                    .action_code("CUSTOMDATA-DELETE")
//                    .action_detail("CUSTOMDATA DELETED "+customDataRequest.getCategory())
//                    .action_detail_handle("ActionDetailBO:"+customDataRequest.getSite()+","+"CUSTOMDATA-DELETE"+","+customDataRequest.getUserId()+":"+"com.rits.customdataservice.service")
//                    .date_time(String.valueOf(LocalDateTime.now()))
//                    .userId(customDataRequest.getUserId())
//                    .txnId("CUSTOMDATA-DELETE"+String.valueOf(LocalDateTime.now())+customDataRequest.getUserId())
//                    .created_date_time(String.valueOf(LocalDateTime.now()))
//                    .build();
//
//            webClientBuilder.build()
//                    .post()
//                    .uri(auditlogUrl)
//                    .bodyValue(activityLog)
//                    .retrieve()
//                    .bodyToMono(AuditLogRequest.class)
//                    .block();
            return MessageModel.builder().message_details(new MessageDetails(customDataRequest.getCategory() + " deleted SuccessFully", "S")).build();
        } else {
            throw new CustomDataException(2201, customDataRequest.getCategory());
        }
    }

    @Override
    public CategoryList retrieveTop50(CustomDataRequest customDataRequest) throws Exception {
        List<Category> categoryList = repository.findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(1, customDataRequest.getSite());
        CategoryList retrievedCategory = new CategoryList(categoryList);
        return retrievedCategory;
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
            throw new CustomDataException(800);
        }
        return extensionResponse;
    }

}
