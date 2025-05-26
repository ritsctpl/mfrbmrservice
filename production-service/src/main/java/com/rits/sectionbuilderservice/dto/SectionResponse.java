package com.rits.sectionbuilderservice.dto;

import java.time.LocalDateTime;
import lombok.*;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionResponse {
    private String handle;
    private String sectionLabel;
    private String instructions;
    private LocalDateTime effectiveDateTime;
}
