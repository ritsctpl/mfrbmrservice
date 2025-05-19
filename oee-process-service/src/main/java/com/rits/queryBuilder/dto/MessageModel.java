package com.rits.queryBuilder.dto;

import com.rits.queryBuilder.model.*;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageModel {
    private QueryBuilder response;
    private MessageDetails message_details;
    private List<TableInfo> tableInfoList;
    private List<Map<String, Object>> queryResult;

    private DataSourceData dsResponse;
    private List<DataSourceData> dataSourceDataList;

    private ManagementDashboardResponse managementrResponse;
    private List<ManagementDashboardRes> managementList;
    private ManagementDashboardRes managementUpdateRes;
    private ManageDashboard manageResponse;
    private MessageDetails messageDetails;
    private ManageFilter filterResponse;
    private ManageColorSchema colorSchemeResponse;
    private List<ManageColorSchema> colorSchemeResponseList;
}
