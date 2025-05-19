package com.rits.nextnumbergeneratorservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NextNumberRequestHolder {
    private List<String> inventoryList;
    private String site;
    private String mfrNo;
    private String bmrNo;
}