package com.rits.managementservice.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ColorSchemeList {
    private List<String> lineColor;
    private List<ItemColor> itemColor;
}
