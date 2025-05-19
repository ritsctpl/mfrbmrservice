package com.rits.lineclearanceservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssociatedLocation {
    private String workcenterId;
    private String resourceId;
    private Boolean enable;
}