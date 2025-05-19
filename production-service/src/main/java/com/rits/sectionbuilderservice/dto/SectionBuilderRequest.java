package com.rits.sectionbuilderservice.dto;

import com.rits.sectionbuilderservice.model.ComponentBuilder;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SectionBuilderRequest {
    private String site;
    private String sectionLabel;
    private String instructions;
    private LocalDateTime effectiveDateTime;
    private List<ComponentBuilder> componentIds;
    private String userId;
    private Integer active;
}
