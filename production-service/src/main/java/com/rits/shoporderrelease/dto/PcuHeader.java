package com.rits.shoporderrelease.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class PcuHeader {
    private String handle;
    private String site;
    private String pcuBO;
    private String shopOrderBO;
    private String itemBO;
    private int qtyToWork;
    private int qtyInQueue;
    private int qtyInHold;
    private int qtyDone;
    private List<RouterList> routerList;
    private List<BomList> bomList;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}
