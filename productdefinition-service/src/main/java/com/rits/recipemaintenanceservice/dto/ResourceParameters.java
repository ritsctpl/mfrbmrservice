package com.rits.recipemaintenanceservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ResourceParameters {
    private int rpm;
    private String duration;
    private String pressure;
}
