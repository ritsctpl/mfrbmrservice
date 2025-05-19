package com.rits.oeeservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class FilterObjects {
    private String site;
    private String shopOrderBO;
    private String itemBO;
    private String shift;
    private String routingBO;
    private String resourceId;
    private String workCenterBO;
    private String operationBO;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String tags;
}
