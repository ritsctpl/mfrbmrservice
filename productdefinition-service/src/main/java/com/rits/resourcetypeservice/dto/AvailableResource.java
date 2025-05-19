package com.rits.resourcetypeservice.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AvailableResource {
    private List<Resource> resourceList;
}
