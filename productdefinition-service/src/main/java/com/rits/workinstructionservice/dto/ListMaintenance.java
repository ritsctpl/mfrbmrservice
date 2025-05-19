package com.rits.workinstructionservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ListMaintenance {
    private String handle;
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
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime updatedDateTime;

}
