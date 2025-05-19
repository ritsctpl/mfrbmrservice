package com.rits.systemruleservice.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Request {
    private String systemRule;
    private String valueObjectType;
    private String longDescription;
    private String description;
    private int sequence;
    private String ruleType;
    private SystemRuleGroupRef systemRuleGroupRef;
    private SystemRuleSettingList systemRuleSetting;
    private SystemRuleOverride systemRuleOverride;
    private SystemRuleContext systemRuleContext;
}
