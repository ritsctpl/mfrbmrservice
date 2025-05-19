package com.rits.systemruleservice.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SystemRuleSettingList {
    private String systemRuleSetting;
    private String systemRuleOverridable;
    private String site;

}
