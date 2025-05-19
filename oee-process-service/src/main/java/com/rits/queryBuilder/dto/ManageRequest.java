package com.rits.queryBuilder.dto;

import com.rits.queryBuilder.model.DashBoardData;
import com.rits.queryBuilder.model.FilterData;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ManageRequest {
    private String site;
    private String user;
    private String dashBoardName;

    private String category;
    private List<DashBoardData> dashBoardDataList;
    private List<FilterData> filterDataList;

    public ManageRequest(ManageRequest original){

        if (original != null) {
            this.dashBoardName = original.dashBoardName;
            this.site = original.site;
            // Deep copy the dashBoardDataList
            if (original.dashBoardDataList != null) {
                this.dashBoardDataList = new ArrayList<>();
                for (DashBoardData data : original.dashBoardDataList) {
                    this.dashBoardDataList.add(new DashBoardData(data)); // Deep copy DashBoardData
                }
            }
        }
    }
}
