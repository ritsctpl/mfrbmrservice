package com.rits.queryBuilder.dto;

import com.rits.queryBuilder.model.DashBoardData;
import com.rits.queryBuilder.model.FilterData;
import lombok.*;

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
