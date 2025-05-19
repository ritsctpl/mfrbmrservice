package com.rits.pcurouterheaderservice.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuBoWithoutBO {
    private String handle;
    private String site;
    private String pcu;
    private String shopOrder;
    private String item;
    private String qtyToWork;
    private String qtyInQueue;
    private String qtyInHold;
    private String qtyDone;
    private String user;
//    private List<RouterList> router;
//    private List<BomList> bomList;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}
