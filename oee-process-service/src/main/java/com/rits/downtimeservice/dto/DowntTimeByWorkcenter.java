package com.rits.downtimeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DowntTimeByWorkcenter {
    private String workcenterId;
    private Long downtimeDuration;
}
