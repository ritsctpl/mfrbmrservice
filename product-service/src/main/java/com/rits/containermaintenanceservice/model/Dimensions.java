package com.rits.containermaintenanceservice.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Dimensions {
    private String height;
    private String width;
    private String length;
    private String maximumFileWeight;
    private String containerWeight;
}
