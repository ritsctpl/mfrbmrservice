package com.rits.groupbuilderservice.dto;

import com.rits.groupbuilderservice.model.SectionBuilder;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class GroupBuilderRequest {
    private String site;
    private String groupLabel;
    private List<SectionBuilder> sectionIds;
    private String userId;
    private Integer active;
}
