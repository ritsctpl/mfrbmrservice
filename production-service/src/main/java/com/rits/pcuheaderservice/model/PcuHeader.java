package com.rits.pcuheaderservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@Document(collection = "R_PCU_HEADER")
public class PcuHeader {
    @Id
    private String handle;
    private String site;
    private String pcuBO;
    private String shopOrderBO;
    private String itemBO;
//    private int qtyToWork;  //commented it for the lot size data type change
//    private int qtyInQueue;  //commented it for the lot size data type change
//    private int qtyInHold;  //commented it for the lot size data type change
//    private int qtyDone;  //commented it for the lot size data type change
    private double qtyToWork;
    private double qtyInQueue;
    private double qtyInHold;
    private double qtyDone;
    private String parentOrderBO;
    private String parentPcuBO;
    private List<RouterList> routerList;
    private List<BomList> bomList;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}
