package com.rits.machinestatusservice.dto;

import lombok.*;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class MachineStatusResponse {
   private List<MachineStatusRequest> machinestatuslist;
}
