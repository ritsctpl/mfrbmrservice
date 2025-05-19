package com.rits.oeeservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverallHistoryRequest {

    private String site;
    private String workcenter;
    private String type;
}
