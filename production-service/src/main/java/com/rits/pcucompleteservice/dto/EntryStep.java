package com.rits.pcucompleteservice.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class EntryStep {
    private String pcuBo;
    private String router;
    private String version;
    private List<RoutingStep> routingStepList;
}
