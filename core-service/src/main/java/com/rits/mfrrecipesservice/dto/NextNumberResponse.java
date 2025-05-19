package com.rits.mfrrecipesservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NextNumberResponse {

    private String nextNumber;
    private double qty;
    private int currentSequence;

}
