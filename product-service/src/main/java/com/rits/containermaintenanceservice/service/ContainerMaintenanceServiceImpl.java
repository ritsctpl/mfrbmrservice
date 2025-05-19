package com.rits.containermaintenanceservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.rits.containermaintenanceservice.Exception.ContainerMaintenanceException;
import com.rits.containermaintenanceservice.dto.*;
import com.rits.containermaintenanceservice.model.ContainerMaintenance;
import com.rits.containermaintenanceservice.model.MessageDetails;
import com.rits.containermaintenanceservice.model.MessageModel;
import com.rits.containermaintenanceservice.repository.ContainerMaintenanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContainerMaintenanceServiceImpl implements ContainerMaintenanceService{

    private final ContainerMaintenanceRepository containerMaintenanceRepository;

    private final WebClient.Builder webClientBuilder;

    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;

    @Value("${auditlog-service.url}/producer")
    private String auditlogUrl;

    public MessageModel create(ContainerMaintenanceRequest containerMaintenanceRequest) throws Exception
    {
        if(isExist(containerMaintenanceRequest))
        {
            throw new ContainerMaintenanceException(2600,containerMaintenanceRequest.getContainer());
        }
        if(containerMaintenanceRequest.getDescription()==null || containerMaintenanceRequest.getDescription().isEmpty())
        {
            containerMaintenanceRequest.setDescription(containerMaintenanceRequest.getContainer());
        }

        ContainerMaintenance newContainerMaintenance = ContainerMaintenance.builder()
                .site(containerMaintenanceRequest.getSite())
                .container(containerMaintenanceRequest.getContainer())
                .handle("ContainerMaintenanceBO: "+containerMaintenanceRequest.getSite()+" , "+containerMaintenanceRequest.getContainer())
                .description(containerMaintenanceRequest.getDescription())
                .containerCategory(containerMaintenanceRequest.getContainerCategory())
                .status(containerMaintenanceRequest.getStatus())
                .containerDataType(containerMaintenanceRequest.getContainerDataType())
                .sfcDataType(containerMaintenanceRequest.getSfcDataType())
                .sfcPackOrder(containerMaintenanceRequest.getSfcPackOrder())
                .handlingUnitManaged(containerMaintenanceRequest.getHandlingUnitManaged())
                .generateHandlingUnitNumber(containerMaintenanceRequest.getGenerateHandlingUnitNumber())
                .totalMinQuantity(containerMaintenanceRequest.getTotalMinQuantity())
                .totalMaxQuantity(containerMaintenanceRequest.getTotalMaxQuantity())
                .packLevelList(containerMaintenanceRequest.getPackLevelList())
                .documentsList(containerMaintenanceRequest.getDocumentsList())
                .dimensionsList(containerMaintenanceRequest.getDimensionsList())
                .customDataList(containerMaintenanceRequest.getCustomDataList())
                .active(1)
                .createdDateTime(LocalDateTime.now())
                .modifiedDateTime(LocalDateTime.now())
                .build();

        return MessageModel.builder().message_details(new MessageDetails(containerMaintenanceRequest.getContainer()+" Created SuccessFully","S")).response(containerMaintenanceRepository.save(newContainerMaintenance)).build();

    }

    public Boolean isExist(ContainerMaintenanceRequest containerMaintenanceRequest) throws Exception
    {
        return containerMaintenanceRepository.existsByContainerAndSiteAndActiveEquals(containerMaintenanceRequest.getContainer(),containerMaintenanceRequest.getSite(),1);
    }

    public MessageModel update(ContainerMaintenanceRequest containerMaintenanceRequest) throws Exception
    {
        if(isExist(containerMaintenanceRequest))
        {
            ContainerMaintenance existingRecord = containerMaintenanceRepository.findByContainerAndSiteAndActiveEquals(containerMaintenanceRequest.getContainer(),containerMaintenanceRequest.getSite(),1);
            if(containerMaintenanceRequest.getDescription()==null || containerMaintenanceRequest.getDescription().isEmpty())
            {
                containerMaintenanceRequest.setDescription(containerMaintenanceRequest.getContainer());
            }
            ContainerMaintenance updatedContainerMaintenance = ContainerMaintenance.builder()
                    .site(existingRecord.getSite())
                    .container(existingRecord.getContainer())
                    .handle(existingRecord.getHandle())
                    .description(containerMaintenanceRequest.getDescription())
                    .containerCategory(containerMaintenanceRequest.getContainerCategory())
                    .status(containerMaintenanceRequest.getStatus())
                    .containerDataType(containerMaintenanceRequest.getContainerDataType())
                    .sfcDataType(containerMaintenanceRequest.getSfcDataType())
                    .sfcPackOrder(containerMaintenanceRequest.getSfcPackOrder())
                    .handlingUnitManaged(containerMaintenanceRequest.getHandlingUnitManaged())
                    .generateHandlingUnitNumber(containerMaintenanceRequest.getGenerateHandlingUnitNumber())
                    .totalMinQuantity(containerMaintenanceRequest.getTotalMinQuantity())
                    .totalMaxQuantity(containerMaintenanceRequest.getTotalMaxQuantity())
                    .packLevelList(containerMaintenanceRequest.getPackLevelList())
                    .documentsList(containerMaintenanceRequest.getDocumentsList())
                    .dimensionsList(containerMaintenanceRequest.getDimensionsList())
                    .customDataList(containerMaintenanceRequest.getCustomDataList())
                    .active(1)
                    .createdDateTime(existingRecord.getCreatedDateTime())
                    .modifiedDateTime(LocalDateTime.now())
                    .build();
            return MessageModel.builder().message_details(new MessageDetails(containerMaintenanceRequest.getContainer()+" updated SuccessFully","S")).response(containerMaintenanceRepository.save(updatedContainerMaintenance)).build();
        }
        throw new ContainerMaintenanceException(2601,containerMaintenanceRequest.getContainer());
    }

    public ContainerMaintenance retrieveByContainerMaintenance(ContainerMaintenanceRequest containerMaintenanceRequest) throws Exception
    {
        if(isExist(containerMaintenanceRequest))
        {
            ContainerMaintenance existingContainerMaintenance = containerMaintenanceRepository.findByContainerAndSiteAndActiveEquals(containerMaintenanceRequest.getContainer(),containerMaintenanceRequest.getSite(),1);
            return  existingContainerMaintenance;
        }
        throw new ContainerMaintenanceException(2601,containerMaintenanceRequest.getContainer());
    }

    public ContainerList retrieveTop50Container(ContainerMaintenanceRequest containerMaintenanceRequest) throws Exception
    {
        List<Container> containerList = containerMaintenanceRepository.findTop50BySiteAndActiveOrderByCreatedDateTimeDesc(containerMaintenanceRequest.getSite(),1);
        ContainerList containerMaintenanceList = new ContainerList(containerList);
        return containerMaintenanceList;
    }

    public MessageModel delete(ContainerMaintenanceRequest containerMaintenanceRequest) throws Exception
    {

        if(isExist(containerMaintenanceRequest))
        {
            ContainerMaintenance existingRecord = containerMaintenanceRepository.findByContainerAndSiteAndActiveEquals(containerMaintenanceRequest.getContainer(),containerMaintenanceRequest.getSite(),1);
            existingRecord.setActive(0);
            containerMaintenanceRepository.save(existingRecord);
            return MessageModel.builder().message_details(new MessageDetails(containerMaintenanceRequest.getContainer()+" deleted SuccessFully","S")).build();
        }
            throw new ContainerMaintenanceException(2601, containerMaintenanceRequest.getContainer());
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
            throw new ContainerMaintenanceException(800);
        }
        return extensionResponse;
    }
}
