package com.rits.machinestatusservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MinutesList {
    private List<PlannedMinutes> plannedMinutesList;

}
