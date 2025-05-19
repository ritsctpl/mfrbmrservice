package com.rits.listmaintenceservice.dto;

import lombok.*;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ListMaintenanceResponseList {
    private List<ListMaintenanceResponse> listMaintenanceList;
}
