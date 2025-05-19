package com.rits.nextnumbergeneratorservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NextNumberGeneratorRequest {
    private String handle;
    private String site;
    private String numberType;
    private String orderType;
    private String defineBy;
    private String object;
    private String objectVersion;
    private String prefix;
    private String suffix;
    private String userBO;
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
    private String userId;
    private boolean commitNextNumberChangesImmediately;
    private String item;
    private String itemGroup;
    private String containerInput;
}
