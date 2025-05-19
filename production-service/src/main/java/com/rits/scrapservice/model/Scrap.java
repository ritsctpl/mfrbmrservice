package com.rits.scrapservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "R_SCRAP")
public class Scrap {
    @Id
    private String scrapBO;
    private String site;
    private String pcuBO;
    private String status;
    private String operationBO;
    private String resourceBO;
    private String shopOrderBO;
    private String processLot;
    private String itemBO;
    private String routingBO;
    private String bomBO;
//    private int scrapQty;   //commented it for the lot size data type change
    private double scrapQty;
    private String userBO;
    private LocalDateTime createdDateTime;
    private int active;
    private String pcuHeaderHandle;
    private String routerHeaderHandle;
}
