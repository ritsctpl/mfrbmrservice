package com.rits.assemblyservice.dto;

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
    private String parentPcuBO;
    private String parentOrderBO;
    private List<RouterList> routerList;
    private List<BomList> bomList;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}
