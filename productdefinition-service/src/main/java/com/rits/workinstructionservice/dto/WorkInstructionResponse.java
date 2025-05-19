package com.rits.workinstructionservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class WorkInstructionResponse {
    private String workInstruction;
    private String revision;
    private String description;
    private String instructionType;
    private String status;
    private boolean currentVersion;

}
