package com.rits.sectionbuilderservice.model;

import com.rits.sectionbuilderservice.dto.Style;
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
@Document(collection = "R_SECTION_BUILDER")
public class SectionBuilder {
    @Id
    private String handle;
    private String site;
    private String sectionLabel;
    private String instructions;
    private LocalDateTime effectiveDateTime;
    private List<ComponentBuilder> componentIds;
    private String userId;
    private Integer active;
    private String structureType;
    private Style style;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String createdBy;
    private String modifiedBy;
}
