package com.rits.batchnorecipeheaderservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Resource {
    private String sequence;
    private String resourceId;
//    private String resourceName;
    private String description;
    private String workCenterId;
    private ResourceParameters parameters;
}
