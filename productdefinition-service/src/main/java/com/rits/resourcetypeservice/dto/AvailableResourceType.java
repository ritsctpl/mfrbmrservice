package com.rits.resourcetypeservice.dto;

import lombok.*;
import lombok.experimental.Accessors;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AvailableResourceType {
    private String resourceType;
}
