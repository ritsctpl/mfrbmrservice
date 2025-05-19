package com.rits.pcucompleteservice.dto;

import lombok.*;

import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuDone {
    private String site;
    private String pcuBO;
    private String itemBO;
    private String routerBO;
    private String userBO;
    private String qtyDone;
    private String shopOrderBO;
    private String workCenter;
    private LocalDateTime dateTime;
    private String operationBO;
    private String resourceBO;
}
