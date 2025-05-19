package com.rits.resourcetypeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ResourceTypeList {
    private String resourceType;
    private String resourceTypeDescription;
}
