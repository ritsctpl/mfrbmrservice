package com.rits.systemruleservice.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemRuleGroupRequest {
    private String ref;
    private String ruleGroup;
    private String description;
    private String longDescription;
}
