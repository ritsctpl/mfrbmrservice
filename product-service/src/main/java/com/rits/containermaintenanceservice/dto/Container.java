package com.rits.containermaintenanceservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Container {
    private String container;
    private String description;
    private String status;
}
