package com.rits.resourcetypeservice.dto;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AvailableResourceTypeList {
    private List<AvailableResourceType> availableResourceTypeList;
}
