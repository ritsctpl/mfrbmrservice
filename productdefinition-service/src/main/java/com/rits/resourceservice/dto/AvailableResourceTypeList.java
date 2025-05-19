package com.rits.resourceservice.dto;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AvailableResourceTypeList {
    private List<AvailableResourceType> availableResourceType;
}
