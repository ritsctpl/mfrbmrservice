package com.rits.sectionbuilderservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Style {
private Boolean marginsEnabled;
private String textAlignment;
private String tableAlignment;
private int splitColumns;
}
