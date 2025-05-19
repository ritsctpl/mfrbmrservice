package com.rits.pcudoneservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuDoneRequestNoBO {
    private String site;
    private String pcu;
    private String item;
    private String itemVersion;
    private String router;
    private String routerVersion;
    private String user;
    private String qtyDone;
    private String shopOrder;
    private String workCenter;
    private String operation;
    private String operationVersion;
    private String resource;
}
