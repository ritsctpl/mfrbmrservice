package com.rits.managementservice.model;

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
