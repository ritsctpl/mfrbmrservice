package com.rits.groupbuilderservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "R_GROUP_BUILDER")
public class GroupBuilder {
    @Id
    private String handle;
    private String site;
    private String groupLabel;
    private List<SectionBuilder> sectionIds;
    private String userId;
    private Integer active;

    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String createdBy;
    private String modifiedBy;
}
