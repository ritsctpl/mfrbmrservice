package com.rits.recipemaintenanceservice.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DataCollection {

    private String dcGroup;
    private String description;
}
