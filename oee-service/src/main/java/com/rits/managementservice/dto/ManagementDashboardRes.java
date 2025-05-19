package com.rits.managementservice.dto;

import com.rits.managementservice.model.DashBoardData;
import com.rits.managementservice.model.FilterData;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ManagementDashboardRes {
    private String site;
    private String dashBoardName;
    private List<DashBoardData> dashBoardDataList;
    private List<FilterData> filterDataList;
}
