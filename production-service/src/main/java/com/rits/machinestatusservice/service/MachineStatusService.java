package com.rits.machinestatusservice.service;

import com.rits.machinestatusservice.dto.MachineStatusRequest;
import com.rits.machinestatusservice.model.MachineLog;
import com.rits.machinestatusservice.model.MachineStatus;
import com.rits.machinestatusservice.model.MachineStatusMessageModel;

import java.util.List;

public interface MachineStatusService {
    public MachineStatusMessageModel logMachineStatus(MachineStatusRequest machineStatusRequest) throws Exception;

    List<MachineStatus> getMachineStatuses(String site, String resource, String shiftStartTime, String createdDate);
    List<MachineLog> getMachineStatusesPostgre(String siteId, String resourceId, String shiftStartTime, String createdDate);

    List<MachineStatus> getMachineStatus(String site, String resource);

    public List<MachineStatus> getActiveMachineStatus(MachineStatusRequest machineStatusRequest);

    MachineStatus getActiveMachineStatusByEvent(MachineStatusRequest machineStatusRequest);

    public MachineStatusMessageModel logMachineStatus(Object productionLog) throws Exception;
}
