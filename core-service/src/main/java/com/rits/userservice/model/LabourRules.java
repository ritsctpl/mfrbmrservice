package com.rits.userservice.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class LabourRules {
    private String labourRule;
    private String currentValue;
}
