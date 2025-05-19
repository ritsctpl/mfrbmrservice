package com.rits.resourceservice.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ResourceTypeRequest {
    private String resourceType;
    private String site;
    private String resource;
    private List<String> resourceTypeList;

    public ResourceTypeRequest(String site) {
        this.site = site;
    }

    public ResourceTypeRequest(String resource, List<String> resourceTypeList, String site) {
        this.resource = resource;
        this.resourceTypeList = resourceTypeList;
        this.site = site;
    }

}
