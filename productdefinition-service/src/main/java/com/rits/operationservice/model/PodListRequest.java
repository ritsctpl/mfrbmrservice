package com.rits.operationservice.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PodListRequest {
    private String site;
    private String podName;
    private String defaultResource;
}
