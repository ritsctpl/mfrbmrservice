package com.rits.dhrservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ToolLogRequest {
    private String site;
    private String pcuBO;
}
