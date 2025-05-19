package com.rits.componentbuilderservice.dto;

import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ComponentRequest {
    private String site;
    private String componentLabel;
    private String dataType;
    private String unit;
    private String defaultValue;
    private Boolean required;
    private String validation;
    private String userId;
}
