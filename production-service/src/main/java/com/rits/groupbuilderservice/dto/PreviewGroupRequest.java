package com.rits.groupbuilderservice.dto;
import com.rits.groupbuilderservice.model.SectionBuilder;
import lombok.*;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PreviewGroupRequest {
    private List<SectionBuilder> sectionsIds;
}