package com.rits.productionlogservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Break {
    private String uniqueId;
    private String breakId;
    private String shiftType;
    private String breakTimeStart;
    private String breakTimeEnd;
    private String meanTime;
    private String reason;
}
