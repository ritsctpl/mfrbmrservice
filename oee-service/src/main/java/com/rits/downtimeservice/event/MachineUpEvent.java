package com.rits.downtimeservice.event;


import com.rits.downtimeservice.model.MachineStatus;

public class MachineUpEvent {

    private MachineStatus message;

    public MachineUpEvent(MachineStatus message) {
        this.message = message;
    }
    public MachineStatus  getSendResult() {
        return message;
    }
}
