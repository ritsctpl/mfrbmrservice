package com.rits.queryBuilder.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemColor {
    private String color;
    private int range;
}
