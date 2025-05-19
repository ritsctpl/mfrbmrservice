package com.rits.managementservice.dto;

import com.rits.managementservice.model.ManageColorSchema;
import com.rits.managementservice.model.ManageDashboard;
import com.rits.managementservice.model.ManageFilter;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageModel {
    private ManagementDashboardResponse response;
    private List<ManagementDashboardRes> managementList;
    private ManagementDashboardRes managementUpdateRes;
    private ManageDashboard manageResponse;
    private MessageDetails messageDetails;
    private ManageFilter filterResponse;
    private ManageColorSchema colorSchemeResponse;
    private List<ManageColorSchema> colorSchemeResponseList;
}
