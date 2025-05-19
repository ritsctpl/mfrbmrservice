package com.rits.managementservice.dto;

import com.rits.managementservice.model.DashBoardData;
import com.rits.managementservice.model.FilterData;
import com.rits.managementservice.model.ManageFilter;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ManagementDashboardResponse {
    private String handle;
    private String dashBoardName;
    private String site;
    private List<DashBoardData> dashBoardDataList;
    private List<FilterData> filterDataList;
}
