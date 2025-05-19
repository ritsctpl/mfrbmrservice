package com.rits.recipemaintenanceservice.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class WorkInstruction {

    private String workInstructionName;
    private String description;
}
