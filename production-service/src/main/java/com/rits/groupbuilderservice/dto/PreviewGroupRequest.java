package com.rits.groupbuilderservice.dto;
import lombok.*;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PreviewGroupRequest {
    private List<String> sectionsIds;
}