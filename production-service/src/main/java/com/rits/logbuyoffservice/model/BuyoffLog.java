package com.rits.logbuyoffservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "R_LOG_BUY_OFF")
public class BuyoffLog {
    private String site;
    @Id
    private String handle;
    private String buyOffBO;
    private String description;
    private String pcu;
    private int buyOffLogId;
    private String buyOffAction;
    private String comments;
    private String state;
    //private String operationBO;
    private String userId;
    //private String itemBO;
    private String router;
    private String routerVersion;
    private String stepId;
    private String shopOrder;
    private String customerOrderBO;
    private String processLotBO;
    private String resourceId;
    private LocalDateTime dateTime;
    private int active;
    private String quantity;
    private String qtyToComplete;

    private String batchNo;
    private String orderNumber;
    private String recipe;
    private String recipeVersion;
    private String operation;
    private String operationVersion;
    private String item;
    private String itemVersion;
    //recipe, recipeVersion, batchNo,operation, operationVersion, item, itemVersion, orderNumber
}
