package com.rits.pcudoneservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuDoneRequest {
    private String site;
    private String pcuBO;
    private String itemBO;
    private String routerBO;
    private String userBO;
    private String qtyDone;
    private String shopOrderBO;
    private String workCenter;
    private String operationBO;
    private String resourceBO;
}
