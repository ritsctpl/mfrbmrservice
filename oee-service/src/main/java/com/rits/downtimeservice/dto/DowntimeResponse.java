package com.rits.downtimeservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DowntimeResponse {
    private String reasonCode;
    private List<DownTimeByShift> downTimeByShiftList;
}
