package com.rits.operationservice.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ListOption {
    private String browseWorkList;
    private String podWorkList;
    private String assembleList;
    private String dcCollectList;
    private String toolList;
    private String workInstructionList;
    private String dcEntryList;
    private String subStepList;
}
