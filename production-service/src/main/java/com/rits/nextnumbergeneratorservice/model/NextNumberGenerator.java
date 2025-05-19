package com.rits.nextnumbergeneratorservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "R_NEXTNUMBER")
public class NextNumberGenerator {
    @Id
    private String handle;
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
    private int active;
    private String itemBO;
    private String itemGroupBO;
    private String item;
    private String itemGroup;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private boolean commitNextNumberChangesImmediately;
    private String containerInput;
}
