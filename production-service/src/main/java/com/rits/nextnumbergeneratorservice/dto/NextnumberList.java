package com.rits.nextnumbergeneratorservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class NextnumberList {
    private String inventory;
    private double qty;
    private Integer currentSequence;
}
