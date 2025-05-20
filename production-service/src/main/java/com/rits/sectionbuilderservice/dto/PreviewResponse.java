package com.rits.sectionbuilderservice.dto;

import com.rits.componentbuilderservice.model.Component;
import com.rits.sectionbuilderservice.model.ComponentBuilder;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PreviewResponse {
    private String handle;
    private String site;
    private String sectionLabel;
    private String instructions;
    private LocalDateTime effectiveDateTime;
    private List<ComponentBuilder> componentIds;
    private List<Map<String, Object>> componentList;
    private String userId;
    private Integer active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String createdBy;
    private String modifiedBy;
}
