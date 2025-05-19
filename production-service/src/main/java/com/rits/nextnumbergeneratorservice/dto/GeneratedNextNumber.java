package com.rits.nextnumbergeneratorservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GeneratedNextNumber {
    private String nextNum;
    private int currentSeq;
}
