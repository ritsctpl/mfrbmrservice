package com.rits.systemruleservice.model;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SystemRuleTypes {
    private String systemTypes;
    private String siteValue;
    public boolean isOverridable;
    private String globalValue;
}

