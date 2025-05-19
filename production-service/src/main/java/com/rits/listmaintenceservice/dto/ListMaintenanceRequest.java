package com.rits.listmaintenceservice.dto;

import com.rits.listmaintenceservice.model.Column;
import lombok.*;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ListMaintenanceRequest {
    private String site;
    private String list;
    private String category;
    private String description;
    private String maximumNumberOfRow;
    private String type;
    private boolean allowOperatorToChangeColumnSequence;
    private boolean allowOperatorToSortRows;
    private boolean allowMultipleSelection;
    private boolean showAllActiveSfcsToOperator;
    private List<Column> columnList;
    private String userId;
}
