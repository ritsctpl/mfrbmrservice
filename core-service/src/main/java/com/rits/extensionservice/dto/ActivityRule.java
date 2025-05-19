package com.rits.extensionservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ActivityRule {
    private String ruleName;
    private String setting;
}
