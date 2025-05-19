package com.rits.managementservice.model;

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
