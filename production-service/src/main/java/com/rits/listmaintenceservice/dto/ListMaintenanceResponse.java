package com.rits.listmaintenceservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ListMaintenanceResponse {

    private String list;
    private String description;
    private String category;
}
