package com.rits.batchnorecipeheaderservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Correction {
    private String correctionId;
//    private String correctionName;
    private String corrDescription;
    private String condition;
    private String action;
    private String impact;
}
