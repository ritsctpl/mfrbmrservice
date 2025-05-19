package com.rits.machinestatusservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MachineStatusMessageDetails {
    private String msg;
    private String msg_type;
}
