package com.rits.routingservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "PCUINQUEUE")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuInQueue {
    private String site;
    @Id
    private String handle;
    private LocalDateTime dateTime;
    private String pcuBO;
    private String itemBO;
    private String routerBO;
    private String operationBO;
    private String resourceBO;
    private String stepID;
    private String userBO;
    private String qtyToComplete;
    private String qtyInQueue;
    private String shopOrderBO;
    private String childRouterBO;
    private String parentStepID;
    private int active;
}