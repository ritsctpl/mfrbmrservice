package com.rits.shoporderrelease.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NextNumberGeneratorRequest {
    private String site;
    private String numberType;
    private String orderType;
    private String defineBy;
    private String object;
    private String objectVersion;
    private String prefix;
    private String suffix;
    private int numberBase;
    private int sequenceLength;
    private int minSequence;
    private int maxSequence;
    private int warningThreshold;
    private int incrementBy;
    private int currentSequence;
    private String resetSequenceNumber;
    private String nextNumberActivity;
    private boolean createContinuousSfcOnImport;
    private String sampleNextNumber;
}
