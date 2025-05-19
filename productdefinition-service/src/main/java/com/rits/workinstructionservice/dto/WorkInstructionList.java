package com.rits.workinstructionservice.dto;

import lombok.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class WorkInstructionList {
    private String workInstruction;
    private String revision;
    private String instructionType;
    private String description;
    private String status;
    private boolean currentVersion;


}
