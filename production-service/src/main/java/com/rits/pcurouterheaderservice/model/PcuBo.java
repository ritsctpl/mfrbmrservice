package com.rits.pcurouterheaderservice.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuBo {
    private String handle;
    private String site;
    private String pcuBO;
    private String shopOrderBO;
    private String itemBO;
    private String qtyToWork;
    private String qtyInQueue;
    private String  qtyInHold;
    private String qtyDone;
    private String userBO;
    private List<RouterList> router;
    private List<BomList> bomList;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}
