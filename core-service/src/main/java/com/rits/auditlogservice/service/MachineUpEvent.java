package com.rits.auditlogservice.service;



public class MachineUpEvent {

    private MachineStatus message;

    public MachineUpEvent(MachineStatus message) {
        this.message = message;
    }
    public MachineStatus  getSendResult() {
        return message;
    }
}
