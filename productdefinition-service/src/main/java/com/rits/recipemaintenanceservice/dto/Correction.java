package com.rits.recipemaintenanceservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Correction {
    private String correctionId;
    private String sequence;
    private String corrDescription;
    private String condition;
    private String action;
    private String impact;
}
