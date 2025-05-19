package com.rits.listmaintenceservice.service;

import com.rits.listmaintenceservice.dto.*;
import com.rits.listmaintenceservice.exception.ListMaintenanceException;
import com.rits.listmaintenceservice.model.Column;
import com.rits.listmaintenceservice.model.ListMaintenance;
import com.rits.listmaintenceservice.model.MessageDetails;
import com.rits.listmaintenceservice.model.MessageModel;
import com.rits.listmaintenceservice.repository.ListMaintenanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ListMaintenanceServiceImpl implements ListMaintenanceService{
    private final ListMaintenanceRepository listMaintenanceRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;

    @Value("${datafield-service.url}/isExist")
    private String datafieldUrl;
    @Override
    public MessageModel createListMaintenance(ListMaintenanceRequest listMaintenanceRequest) throws Exception {
        if (listMaintenanceRepository.existsByActiveAndSiteAndListAndCategory(1, listMaintenanceRequest.getSite(),listMaintenanceRequest.getList(),listMaintenanceRequest.getCategory())) {
            throw new ListMaintenanceException(3701, listMaintenanceRequest.getList());
        }
        if(listMaintenanceRequest.getDescription()==null || listMaintenanceRequest.getDescription().isEmpty()){
            listMaintenanceRequest.setDescription(listMaintenanceRequest.getList());
        }
//        if (listMaintenanceRequest.getColumnList() != null && !listMaintenanceRequest.getColumnList().isEmpty()) {
//            for (Column column : listMaintenanceRequest.getColumnList()) {
//                DataFieldRequest dataFieldRequest = DataFieldRequest.builder()
//                        .site(listMaintenanceRequest.getSite())
//                        .dataField(column.getColumnName())
//                        .build();
//
//                Boolean dataFieldExist = webClientBuilder.build()
//                        .post()
//                        .uri(datafieldUrl)
//                        .bodyValue(dataFieldRequest)
//                        .retrieve()
//                        .bodyToMono(Boolean.class)
//                        .block();
//
//                if (!dataFieldExist) {
//                    throw new ListMaintenanceException(400,column.getColumnName());
//                }
//            }
//        }
        ListMaintenance listMaintenance= ListMaintenance.builder()
                .site(listMaintenanceRequest.getSite())
                .list(listMaintenanceRequest.getList())
                .handle("ListMaintenanceBo:"+listMaintenanceRequest.getSite()+","+listMaintenanceRequest.getList()+","+listMaintenanceRequest.getCategory())
                .category(listMaintenanceRequest.getCategory())
                .description(listMaintenanceRequest.getDescription())
                .maximumNumberOfRow(listMaintenanceRequest.getMaximumNumberOfRow())
                .type(listMaintenanceRequest.getType())
                .allowOperatorToChangeColumnSequence(listMaintenanceRequest.isAllowOperatorToChangeColumnSequence())
                .allowOperatorToSortRows(listMaintenanceRequest.isAllowOperatorToSortRows())
                .allowMultipleSelection(listMaintenanceRequest.isAllowMultipleSelection())
                .showAllActiveSfcsToOperator(listMaintenanceRequest.isShowAllActiveSfcsToOperator())
                .columnList(listMaintenanceRequest.getColumnList())
                .active(1)
                .createdDateTime(LocalDateTime.now())
                .createdBy(listMaintenanceRequest.getUserId())
                .build();
        return MessageModel.builder().message_details(new MessageDetails(listMaintenanceRequest.getList()+" created SuccessFully","S")).response(listMaintenanceRepository.save(listMaintenance)).build();

    }

    @Override
    public MessageModel updateListMaintenance(ListMaintenanceRequest listMaintenanceRequest) throws Exception {
        if (listMaintenanceRepository.existsByActiveAndSiteAndListAndCategory(1, listMaintenanceRequest.getSite(),listMaintenanceRequest.getList(),listMaintenanceRequest.getCategory())) {
            if(listMaintenanceRequest.getDescription()==null || listMaintenanceRequest.getDescription().isEmpty()){
                listMaintenanceRequest.setDescription(listMaintenanceRequest.getList());
            }
//            if (listMaintenanceRequest.getColumnList() != null && !listMaintenanceRequest.getColumnList().isEmpty()) {
//                for (Column column : listMaintenanceRequest.getColumnList()) {
//                    DataFieldRequest dataFieldRequest = DataFieldRequest.builder()
//                            .site(listMaintenanceRequest.getSite())
//                            .dataField(column.getColumnName())
//                            .build();
//
//                    Boolean dataFieldExist = webClientBuilder.build()
//                            .post()
//                            .uri(datafieldUrl)
//                            .bodyValue(dataFieldRequest)
//                            .retrieve()
//                            .bodyToMono(Boolean.class)
//                            .block();
//
//                    if (!dataFieldExist) {
//                        throw new ListMaintenanceException(400,column.getColumnName());
//                    }
//                }
//            }
            ListMaintenance existingListMaintenance=listMaintenanceRepository.findByActiveAndSiteAndListAndCategory(1,listMaintenanceRequest.getSite(),listMaintenanceRequest.getList(),listMaintenanceRequest.getCategory());
            ListMaintenance listMaintenance= ListMaintenance.builder()
                    .site(existingListMaintenance.getSite())
                    .list(existingListMaintenance.getList())
                    .handle(existingListMaintenance.getHandle())
                    .category(listMaintenanceRequest.getCategory())
                    .description(listMaintenanceRequest.getDescription())
                    .maximumNumberOfRow(listMaintenanceRequest.getMaximumNumberOfRow())
                    .type(listMaintenanceRequest.getType())
                    .allowOperatorToChangeColumnSequence(listMaintenanceRequest.isAllowOperatorToChangeColumnSequence())
                    .allowOperatorToSortRows(listMaintenanceRequest.isAllowOperatorToSortRows())
                    .allowMultipleSelection(listMaintenanceRequest.isAllowMultipleSelection())
                    .showAllActiveSfcsToOperator(listMaintenanceRequest.isShowAllActiveSfcsToOperator())
                    .columnList(listMaintenanceRequest.getColumnList())
                    .active(1)
                    .createdDateTime(existingListMaintenance.getCreatedDateTime())
                    .updatedDateTime(LocalDateTime.now())
                    .modifiedBy(listMaintenanceRequest.getUserId())
                    .build();
            return MessageModel.builder().message_details(new MessageDetails(listMaintenanceRequest.getList()+" updated SuccessFully","S")).response(listMaintenanceRepository.save(listMaintenance)).build();

        }
            throw new ListMaintenanceException(3702, listMaintenanceRequest.getList());
    }

    @Override
    public MessageModel deleteListMaintenance(String site, String list,String category) throws Exception {
        if (!listMaintenanceRepository.existsByActiveAndSiteAndListAndCategory(1,site,list,category)) {
           throw new ListMaintenanceException(3702,list);
        }
        ListMaintenance existingListMaintenance=listMaintenanceRepository.findByActiveAndSiteAndListAndCategory(1,site,list,category);
        existingListMaintenance.setActive(0);
        existingListMaintenance.setUpdatedDateTime(LocalDateTime.now());
        listMaintenanceRepository.save(existingListMaintenance);
        return MessageModel.builder().message_details(new MessageDetails(list+" deleted SuccessFully","S")).build();

    }

    @Override
    public ListMaintenance retrieveListMaintenance(String site, String list,String category) throws Exception {
        ListMaintenance existingListMaintenance=listMaintenanceRepository.findByActiveAndSiteAndListAndCategory(1,site,list,category);
        if(existingListMaintenance==null){
            throw new ListMaintenanceException(3702,list);
        }
        return existingListMaintenance;
    }

    @Override
    public ListMaintenanceResponseList getAllListMaintenance(String site, String list) throws Exception {
        List<ListMaintenanceResponse> listMaintenanceResponses;
        if(list!=null && !list.isEmpty()){
            listMaintenanceResponses = listMaintenanceRepository.findByActiveAndSiteAndListContainingIgnoreCase(1, site, list);
            if(listMaintenanceResponses.isEmpty()){
                throw new ListMaintenanceException(3702,list);
            }
            return ListMaintenanceResponseList.builder().listMaintenanceList(listMaintenanceResponses).build();
        }else {
            return getAllListMaintenanceByCreatedDate(site);
        }
    }

    @Override
    public ListMaintenanceResponseList getAllListMaintenanceByCreatedDate(String site) throws Exception {
        List<ListMaintenanceResponse> listMaintenanceResponses = listMaintenanceRepository.findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(1, site);
        return ListMaintenanceResponseList.builder().listMaintenanceList(listMaintenanceResponses).build();
    }

    @Override
    public ListMaintenanceResponseList getAllListByCategory(String site, String category) throws Exception {
        List<ListMaintenanceResponse> listMaintenanceResponses = listMaintenanceRepository.findByActiveAndSiteAndCategory(1, site,category);

        return ListMaintenanceResponseList.builder().listMaintenanceList(listMaintenanceResponses).build();

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
        if (extensionResponse==null|| extensionResponse.isEmpty()) {
            throw new ListMaintenanceException(800);
        }
        return extensionResponse;
    }
}
