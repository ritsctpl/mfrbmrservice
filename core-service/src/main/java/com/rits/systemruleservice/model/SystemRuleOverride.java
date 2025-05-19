package com.rits.systemruleservice.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SystemRuleOverride {
    private String systemRuleSetting;
    private OverridingGbo overridingGbo;
}
