package com.rits.operationservice.dto;

import com.rits.resourcetypeservice.Model.ResourceMemberList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class ResourceTypeRequest {
    private String resourceType;
    private String site;
    private List<String> resourceTypeList;
}
