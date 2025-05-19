package com.rits.batchnorecipeheaderservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TriggerPoint {
    private String sequence;
    private String triggerPointId;
//    private String triggerPointName;
    private String description;
    private String condition;
    private String action;
}
