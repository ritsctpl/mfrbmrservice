package com.rits.resourceservice.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AssociateResourceToResourceType {
    private String site;
    private List<String> resourceType;
    private String resource;
}
