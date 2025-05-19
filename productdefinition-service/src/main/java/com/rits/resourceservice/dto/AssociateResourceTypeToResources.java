package com.rits.resourceservice.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AssociateResourceTypeToResources {
    private String resourceType;
    private List<String> resource;
    private String site;
}
