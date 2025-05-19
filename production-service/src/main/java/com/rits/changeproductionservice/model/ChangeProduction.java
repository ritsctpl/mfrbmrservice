package com.rits.changeproductionservice.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "R_CHANGE_PRODUCTION_LOG")
public class ChangeProduction {
    private String pcu;
    private String pcuStatus;
    private String shopOrder;
    private String operation;
    private String resource;
    private String bom;
    private String bomVersion;
    private String item;
    private String itemVersion;
    private String routing;
    private String routingVersion;
    private String newShopOrder;
    private String newOperation;
    private String newResource;
    private String newBom;
    private String newBomVersion;
    private String newItem;
    private String newItemVersion;
    private String newRouting;
    private String newRoutingVersion;
    private String comments;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private int active;
}
