package com.rits.groupbuilderservice.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SectionBuilder {
    private String handle;
    private String sectionLabel;
}
