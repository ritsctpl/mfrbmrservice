package com.rits.templatebuilderservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Config {
    private String type;
    private String logo;
    private String pageOccurrence;
    private int margin;
    private int height;
    private String alignment;
}
