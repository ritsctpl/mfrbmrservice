package com.rits.inventoryservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IsTrackable {
    private String site;
    private String dataField;
}
