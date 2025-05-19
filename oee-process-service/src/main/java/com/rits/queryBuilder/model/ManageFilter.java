package com.rits.queryBuilder.model;

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
@Document(collection = "r_manage_filter")
public class ManageFilter {
    @Id
    private String handle;
    private String site;
    private String dashBoardName;
    private List<FilterData> filterationData;

    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private int active;

}






































































