package com.rits.processorderrelease_old.dto;

import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GeneratePrefixAndSuffixRequest {
    private String itemGroupBO;
    private String value;
    private String object;
    private String site;
    private String itemBO;
    private String nextNumberActivity;
    private String processOrderBO;
    private int priority;
    private String bnoBO;
    private String userBO;
    private int numberBase;
    private int sequenceLength;
    private int minSequence;
    private int maxSequence;
    private int warningThreshold;
    private int incrementBy;
    private int currentSequence;
    private String numberType;
    private String orderType;
    private String defineBy;
    private String objectVersion;
    private String prefix;
    private String suffix;
    private String nonStartObject;
    private String nonStartVersion;
}
