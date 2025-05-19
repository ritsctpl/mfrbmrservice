package com.rits.oeeservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class TotalCountByResource {
    private int goodCount;
    private int badCount;
    private int totalCount;
    private String resource;
}
