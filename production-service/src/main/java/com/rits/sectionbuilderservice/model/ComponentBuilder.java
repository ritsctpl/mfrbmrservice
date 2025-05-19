package com.rits.sectionbuilderservice.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ComponentBuilder {
    private String handle;
    private String componentLabel;
}
