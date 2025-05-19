package com.rits.managementservice.model;

import lombok.*;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ColorScheme {
    private List<String> lineColor;
    private List<ItemColor> itemColor;

    public ColorScheme(ColorScheme colorScheme) {
        this.lineColor = colorScheme.getLineColor();
        this.itemColor = colorScheme.getItemColor();
    }
}
