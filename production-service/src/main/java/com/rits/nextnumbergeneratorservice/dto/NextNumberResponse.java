package com.rits.nextnumbergeneratorservice.dto;

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

    public NextNumberResponse(String nextNumber, int currentSequence){
        this.nextNumber = nextNumber;
        this.currentSequence = currentSequence;
    }
}
