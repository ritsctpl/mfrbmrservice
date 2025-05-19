package com.rits.recipemaintenanceservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Resource {
    private String sequence;
    private String resourceId;
    private String description;
    private String workCenterId;
    private ResourceParameters parameters;
}
