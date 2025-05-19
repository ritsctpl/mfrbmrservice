package com.rits.listmaintenceservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "R_LISTMAINTENANCE")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ListMaintenance {
    @Id
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
    private String createdBy;
    private String modifiedBy;

}
