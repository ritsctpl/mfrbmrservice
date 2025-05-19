package com.rits.batchnorecipeheaderservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Scaling {
    private Boolean scalable;
    private String scalingFactor;
    private String maxBatchSize;
    private String minBatchSize;
}
