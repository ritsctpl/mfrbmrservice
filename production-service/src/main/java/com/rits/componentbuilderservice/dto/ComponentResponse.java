package com.rits.componentbuilderservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ComponentResponse {
    private String handle;
    private String componentLabel;
    private String dataType;
    private String defaultValue;
    private Boolean required;
}
