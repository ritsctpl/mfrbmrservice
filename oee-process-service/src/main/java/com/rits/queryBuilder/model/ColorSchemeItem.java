package com.rits.queryBuilder.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ColorSchemeItem {
    private String handleRef;
    private ColorScheme colorScheme;
}
