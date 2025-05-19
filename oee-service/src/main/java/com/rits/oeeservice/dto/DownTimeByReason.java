package com.rits.oeeservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class DownTimeByReason {
    private double downTime;
    private String reasonCode;
}
