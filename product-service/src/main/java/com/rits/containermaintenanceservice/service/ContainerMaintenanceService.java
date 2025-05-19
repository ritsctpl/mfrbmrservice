package com.rits.containermaintenanceservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.rits.containermaintenanceservice.dto.ContainerList;
import com.rits.containermaintenanceservice.dto.ContainerMaintenanceRequest;
import com.rits.containermaintenanceservice.dto.Extension;
import com.rits.containermaintenanceservice.dto.Response;
import com.rits.containermaintenanceservice.model.ContainerMaintenance;
import com.rits.containermaintenanceservice.model.MessageModel;
import com.rits.containermaintenanceservice.repository.ContainerMaintenanceRepository;

import java.util.List;

public interface ContainerMaintenanceService{

    public MessageModel create(ContainerMaintenanceRequest containerMaintenanceRequest) throws Exception;

    public Boolean isExist(ContainerMaintenanceRequest containerMaintenanceRequest) throws Exception;

    public MessageModel update(ContainerMaintenanceRequest containerMaintenanceRequest) throws Exception;

    public ContainerMaintenance retrieveByContainerMaintenance(ContainerMaintenanceRequest containerMaintenanceRequest) throws Exception;

    public ContainerList  retrieveTop50Container(ContainerMaintenanceRequest containerMaintenanceRequest) throws Exception;

    public MessageModel delete(ContainerMaintenanceRequest containerMaintenanceRequest) throws Exception;

    public String callExtension(Extension extension);
}
