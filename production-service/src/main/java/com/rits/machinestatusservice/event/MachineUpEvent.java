package com.rits.machinestatusservice.event;

import com.rits.machinestatusservice.model.MachineStatus;

public class MachineUpEvent {

    private MachineStatus message;

    public MachineUpEvent(MachineStatus message) {
        this.message = message;
    }
    public MachineStatus  getSendResult() {
        return message;
    }
}
