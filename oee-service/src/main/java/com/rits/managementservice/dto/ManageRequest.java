package com.rits.managementservice.dto;

import com.rits.managementservice.model.DashBoardData;
import com.rits.managementservice.model.FilterData;
import com.rits.managementservice.model.ManageDashboard;
import com.rits.managementservice.model.ManageFilter;
import lombok.*;
import java.time.LocalDateTime;
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
//        this.site = request.getSite();
//        this.user = request.getUser();
//        this.dashBoardName = request.getDashBoardName();
//        this.category = request.getCategory();
//        this.dashBoardDataList = request.getDashBoardDataList();
//        this.filterDataList = request.getFilterDataList();

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
