package com.rits.cycletimeservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProductionLogRequest {
    private String site;
    private LocalDateTime startDateTime;
}
