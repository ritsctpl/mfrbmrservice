package com.rits.managementservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "r_manage_dashboard")
public class ManageDashboard {
    @Id
    private String handle;
    private String dashBoardName;
    private String site;
    private List<DashBoardData> dashBoardDataList;
    private List<FilterData> filterDataList;

    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private int active;
}





































































