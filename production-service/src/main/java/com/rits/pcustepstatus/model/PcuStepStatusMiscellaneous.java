package com.rits.pcustepstatus.model;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuStepStatusMiscellaneous {
    private String customer;
    private String customerOrder;
    private String hold;
    private String rmaNumber;
    private String containerNumber;
    private String dateRange;
    private String ncCode;
    private String userId;
    private String reasonCode;
    private String age;
    private String originalPcu;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
