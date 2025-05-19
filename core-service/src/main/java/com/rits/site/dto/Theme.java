package com.rits.site.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Theme {
    private String logo;
    private String color;
    private String background;
    private String lineColor;
}
