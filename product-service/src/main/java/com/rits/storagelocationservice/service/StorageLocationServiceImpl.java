package com.rits.storagelocationservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.rits.containermaintenanceservice.dto.AuditLogRequest;
import com.rits.storagelocationservice.dto.*;
import com.rits.storagelocationservice.exception.StorageLocationException;
import com.rits.storagelocationservice.model.MessageModel;
import com.rits.storagelocationservice.model.StorageLocation;
import com.rits.storagelocationservice.model.WorkCenter;
import com.rits.storagelocationservice.repository.StorageLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class StorageLocationServiceImpl implements StorageLocationService{
    private final StorageLocationRepository storageLocationRepository;
    private final WebClient.Builder webClientBuilder;
    @Value("${workcenter-service.url}/retrieveBySite")
    private String workCenterServiceUrl;

    @Value("${auditlog-service.url}/producer")
    private String auditlogUrl;

    @Override
    public StorageLocation createStorageLocation(StorageLocationRequest storageLocationRequest) throws Exception {

        long recordPresent = storageLocationRepository.countByStorageLocationAndSiteAndActive(storageLocationRequest.getStorageLocation(), storageLocationRequest.getSite(), 1);
        if (recordPresent > 0) {
            throw new StorageLocationException(1900, storageLocationRequest.getStorageLocation());
        }
        StorageLocation storageLocation = StorageLocation.builder()
                .site(storageLocationRequest.getSite())
                .storageLocation(storageLocationRequest.getStorageLocation())
                .handle("StorageLocationBO:" + storageLocationRequest.getSite() + "," + storageLocationRequest.getStorageLocation())
                .ewmManagedStorageLocation(storageLocationRequest.isEwmManagedStorageLocation())
                .description(storageLocationRequest.getDescription())
                .workCenterList(storageLocationRequest.getWorkCenterList())
                .active(1)
                .modifiedDateTime(LocalDateTime.now())
                .createdDateTime(LocalDateTime.now())
                .build();
        return storageLocationRepository.save(storageLocation);
    }

    @Override
    public StorageLocation updateStorageLocation(StorageLocationRequest storageLocationRequest) throws Exception {
        StorageLocation storageLocation = storageLocationRepository.findByStorageLocationAndSiteAndActive(storageLocationRequest.getStorageLocation(), storageLocationRequest.getSite(), 1);
        if (storageLocation != null) {
            storageLocation = StorageLocation.builder()
                    .id(storageLocation.getId())
                    .site(storageLocation.getSite())
                    .storageLocation(storageLocation.getStorageLocation())
                    .handle("StorageLocationBO:" + storageLocation.getSite() + "," + storageLocation.getStorageLocation())
                    .description(storageLocationRequest.getDescription())
                    .ewmManagedStorageLocation(storageLocationRequest.isEwmManagedStorageLocation())
                    .active(storageLocation.getActive())
                    .workCenterList(storageLocationRequest.getWorkCenterList())
                    .createdDateTime(storageLocation.getCreatedDateTime())
                    .modifiedDateTime(LocalDateTime.now())
                    .build();
            return storageLocationRepository.save(storageLocation);
        }
        throw new StorageLocationException(1901, storageLocationRequest.getStorageLocation());
    }
    @Override
    public StorageLocationResponseList getStorageLocationListByCreationDate(StorageLocationRequest storageLocationRequest) throws Exception {
        List<StorageLocationResponse> storageLocationResponses = storageLocationRepository.findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(1, storageLocationRequest.getSite());
        if (storageLocationResponses != null && !storageLocationResponses.isEmpty()) {
            return StorageLocationResponseList.builder().storageLocationList(storageLocationResponses).build();
        } else {
            throw new StorageLocationException(1901, storageLocationRequest.getStorageLocation());
        }
    }


    @Override
    public StorageLocationResponseList getStorageLocationList(StorageLocationRequest storageLocationRequest) throws Exception {
        if (storageLocationRequest.getStorageLocation() == null || storageLocationRequest.getStorageLocation().isEmpty()) {
            return  getStorageLocationListByCreationDate(storageLocationRequest);
        } else {
            List<StorageLocationResponse> storageLocationResponses = storageLocationRepository.findByStorageLocationContainingIgnoreCaseAndSiteAndActive(storageLocationRequest.getStorageLocation(), storageLocationRequest.getSite(), 1);
            if (storageLocationResponses != null && !storageLocationResponses.isEmpty()) {
                return StorageLocationResponseList.builder().storageLocationList(storageLocationResponses).build();
            } else {
                throw new StorageLocationException(1901, storageLocationRequest.getStorageLocation());
            }
        }
    }

    @Override
    public StorageLocation retrieveStorageLocation(StorageLocationRequest storageLocationRequest) throws Exception {
        StorageLocation storageLocation = storageLocationRepository.findByStorageLocationAndSiteAndActive(storageLocationRequest.getStorageLocation(), storageLocationRequest.getSite(), 1);
        if (storageLocation != null ) {
            return storageLocation;
        } else {
            throw new StorageLocationException(1901, storageLocationRequest.getStorageLocation());
        }
    }

    @Override
    public Response deleteStorageLocation(StorageLocationRequest storageLocationRequest) throws Exception {
        if (storageLocationRepository.existsByStorageLocationAndSiteAndActive(storageLocationRequest.getStorageLocation(), storageLocationRequest.getSite(), 1)) {
            StorageLocation existingStorageLocation = storageLocationRepository.findByStorageLocationAndActive(storageLocationRequest.getStorageLocation(),1);
            existingStorageLocation.setActive(0);
            existingStorageLocation.setModifiedDateTime(LocalDateTime.now());
            storageLocationRepository.save(existingStorageLocation);
            Response response = Response.builder().message(storageLocationRequest.getStorageLocation() + " Deleted").build();
            return response;
        } else {
            throw new StorageLocationException(1901, storageLocationRequest.getStorageLocation());
        }
    }

    @Override
    public Boolean isStorageLocationExist(StorageLocationRequest storageLocationRequest) throws Exception {
        return storageLocationRepository.existsByStorageLocationAndSiteAndActive(storageLocationRequest.getStorageLocation(),storageLocationRequest.getSite() ,1);

    }

    @Override
    public StorageLocation associateWorkCenterToWorkCenterList(WorkCenterListRequest workCenterListRequest) throws Exception{
        StorageLocation storageLocation = storageLocationRepository.findByStorageLocationAndSiteAndActive(workCenterListRequest.getStorageLocation(), workCenterListRequest.getSite(), 1);
        if (storageLocation == null) {
            throw new StorageLocationException(1901, workCenterListRequest.getStorageLocation());
        }
        List<WorkCenter> workCenterList = storageLocation.getWorkCenterList();
        for (String workCenter : workCenterListRequest.getWorkCenter()) {
            boolean alreadyExists = workCenterList.stream().anyMatch(member -> member.getWorkCenter().equals(workCenter));
            if (!alreadyExists) {
                WorkCenter newWorkCenterMember = WorkCenter.builder().workCenter(workCenter).build();
                workCenterList.add(newWorkCenterMember);
            }


        }
        storageLocation.setWorkCenterList(workCenterList);
        storageLocation.setModifiedDateTime(LocalDateTime.now());
        return storageLocationRepository.save(storageLocation);
    }


    @Override
    public StorageLocation removeWorkCenterToWorkCenterList(WorkCenterListRequest workCenterListRequest) throws Exception{
        StorageLocation storageLocation = storageLocationRepository.findByStorageLocationAndSiteAndActive(workCenterListRequest.getStorageLocation(), workCenterListRequest.getSite(), 1);
        if (storageLocation != null) {
            for (String workCenter : workCenterListRequest.getWorkCenter()) {
                if (storageLocation.getWorkCenterList().removeIf(workCenterList -> workCenterList.getWorkCenter().equals(workCenter))) {
                    storageLocation.setModifiedDateTime(LocalDateTime.now());
                }
            }
        } else {
            throw new StorageLocationException(1901, workCenterListRequest.getStorageLocation());
        }
        return storageLocationRepository.save(storageLocation);
    }


    @Override
    public AvailableWorkCenters getAvailableWorkCenters(StorageLocationRequest storageLocationRequest) throws Exception{
        if( storageLocationRequest.getStorageLocation()!=null && !storageLocationRequest.getStorageLocation().isEmpty() ) {
            List<StorageLocation> storageLocations = storageLocationRepository.findBySiteAndStorageLocationAndActive(storageLocationRequest.getSite(), storageLocationRequest.getStorageLocation(), 1);
            if (storageLocations != null && !storageLocations.isEmpty()) {
                List<String> workCenters = new ArrayList<>();
                for (StorageLocation storageLocation : storageLocations) {
                    List<WorkCenter> workCenterList = storageLocation.getWorkCenterList();
                    if (workCenterList != null && !workCenterList.isEmpty()) {
                        workCenters.addAll(workCenterList.stream()
                                .map(WorkCenter::getWorkCenter)
                                .collect(Collectors.toList()));
                    }
                }
                ListOfWorkCenterRequest listOfWorkCenterRequest = new ListOfWorkCenterRequest();
                listOfWorkCenterRequest.setSite(storageLocationRequest.getSite());

                AvailableWorkCenters availableWorkCenters = webClientBuilder
                        .build()
                        .post()
                        .uri(workCenterServiceUrl)
                        .body(BodyInserters.fromValue(listOfWorkCenterRequest))
                        .retrieve()
                        .bodyToMono(AvailableWorkCenters.class)
                        .block();

                List<AvailableWorkCenterList> availableWorkCenters1=availableWorkCenters.getAvailableWorkCenterList();
                if (availableWorkCenters1 != null) {
                    availableWorkCenters1 = availableWorkCenters1.stream()
                            .filter(awc -> !workCenters.contains(awc.getWorkCenter()))
                            .collect(Collectors.toList());


                }

                return AvailableWorkCenters.builder().availableWorkCenterList(availableWorkCenters1).build();
            }
            throw new StorageLocationException(1901, storageLocationRequest.getStorageLocation());

        }
        ListOfWorkCenterRequest listOfWorkCenterRequest = new ListOfWorkCenterRequest();
        listOfWorkCenterRequest.setSite(storageLocationRequest.getSite());
        AvailableWorkCenters availableWorkCenters = webClientBuilder
                .build()
                .post()
                .uri(workCenterServiceUrl)
                .body(BodyInserters.fromValue(listOfWorkCenterRequest))
                .retrieve()
                .bodyToMono(AvailableWorkCenters.class)
                .block();
        return AvailableWorkCenters.builder().availableWorkCenterList(availableWorkCenters.getAvailableWorkCenterList()).build();
    }
}
