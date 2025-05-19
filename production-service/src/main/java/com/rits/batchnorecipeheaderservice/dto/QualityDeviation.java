package com.rits.batchnorecipeheaderservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class QualityDeviation {
    private String deviationId;
//    private String deviationName;
    private String devDescription;
    private String condition;
    private String impact;
    private String requiredAction;
}
