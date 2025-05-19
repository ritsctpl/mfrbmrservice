package com.rits.systemruleservice.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Reference {
    private String type;
    private String ruleGroup;
    private String value;
}
