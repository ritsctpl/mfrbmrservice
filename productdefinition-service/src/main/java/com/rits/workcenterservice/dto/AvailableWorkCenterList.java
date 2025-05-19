package com.rits.workcenterservice.dto;

import lombok.*;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AvailableWorkCenterList {
    private List<AvailableWorkCenter> availableWorkCenterList;
}
