package com.rits.toollogservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ToolLogRequest {
    private String site;
    private String toolNumber;
    private String itemBO;
    private String routerBO;
    private String operationBO;
    private String pcuBO;
    private String shopOrderBO;
    private String resourceBO;
    private String workCenterBO;
    private String toolGroupBO;
    private int loggedQty;
    private String comments;
    private String userId;
}
