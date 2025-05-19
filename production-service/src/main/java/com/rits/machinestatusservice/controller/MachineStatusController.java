package com.rits.machinestatusservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.listmaintenceservice.dto.ListMaintenanceRequest;
import com.rits.listmaintenceservice.exception.ListMaintenanceException;
import com.rits.machinestatusservice.dto.MachineStatusRequest;
import com.rits.machinestatusservice.model.MachineStatus;
import com.rits.machinestatusservice.model.MachineStatusMessageModel;
import com.rits.machinestatusservice.service.MachineStatusService;
import com.rits.pcucompleteservice.model.MessageModel;
import com.rits.productionlogservice.dto.ProductionLogRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/machinestatus-service")
public class MachineStatusController {
    private final MachineStatusService machineStatusService;
    private final ObjectMapper objectMapper;
    @PostMapping("logmachineStatus")
    public MachineStatusMessageModel createMachineStatus(@RequestBody MachineStatusRequest machineStatusRequest){

    try{
        MachineStatusMessageModel response = machineStatusService.logMachineStatus(machineStatusRequest);
        return response;
    } catch (ListMaintenanceException listMaintenanceException) {
        throw listMaintenanceException;
    } catch (Exception e) {
        throw new RuntimeException(e);
    }

    }

    @PostMapping(value = "/logmachineStatusByJson", consumes = MediaType.APPLICATION_JSON_VALUE)
    public MachineStatusMessageModel createMachineStatus(@RequestBody String jsonPayload) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonPayload);
            Object targetObject = null;

            if (rootNode.has("eventType") && "MC_DOWN".equals(rootNode.get("eventType").asText())) {
                targetObject = objectMapper.treeToValue(rootNode, ProductionLogRequest.class);
            } else if (rootNode.has("eventType") && "MC_UP".equals(rootNode.get("eventType").asText())) {

                targetObject = objectMapper.treeToValue(rootNode, ProductionLogRequest.class);
            } else{
                targetObject = objectMapper.treeToValue(rootNode, ProductionLogRequest.class);
            }

            return machineStatusService.logMachineStatus(targetObject);

        } catch (Exception e) {
            e.printStackTrace();
            return null; // Consider a better error handling strategy
        }
    }

    @PostMapping("getActiveMachineLog")
    public List<MachineStatus> getActiveMachineLog(MachineStatusRequest machineStatusRequest){
        List<MachineStatus> machineStatuses = new ArrayList<>();
        try{
           return  machineStatuses = machineStatusService.getActiveMachineStatus(machineStatusRequest);

        } catch (ListMaintenanceException listMaintenanceException) {
            throw listMaintenanceException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping("getMcDownRecord")
    public MachineStatus getActiveMachineLogByEvent(@RequestBody MachineStatusRequest machineStatusRequest){
        try{
            return machineStatusService.getActiveMachineStatusByEvent(machineStatusRequest);
        } catch (ListMaintenanceException listMaintenanceException) {
            throw listMaintenanceException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping("getMachineStatus")
    public List<MachineStatus> getMachineStatus(@RequestBody MachineStatusRequest machineStatusRequest){
        List<MachineStatus> machineStatuses = new ArrayList<>();
        try{
            return  machineStatuses = machineStatusService.getMachineStatus(machineStatusRequest.getSite(), machineStatusRequest.getResource());

        } catch (ListMaintenanceException listMaintenanceException) {
            throw listMaintenanceException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    @PostMapping("/getActiveMCDown")
    @ResponseStatus(HttpStatus.CREATED)
    public List<MachineStatus> getAvailability(@RequestBody MachineStatusRequest machineStatusRequest) throws Exception {
        List<MachineStatus> machineStatuses = new ArrayList<>();
        try{
            return  machineStatuses = machineStatusService.getMachineStatuses(machineStatusRequest.getSite(), machineStatusRequest.getResource(),machineStatusRequest.getShiftStartTime(),machineStatusRequest.getCreatedDate());

        } catch (ListMaintenanceException listMaintenanceException) {
            throw listMaintenanceException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
