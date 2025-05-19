package com.rits.signoffservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuInWorkRequest {
    private String site;
    private String pcu;
    private String operation;
    private String operationVersion;
    private int qtyInWork;
}
