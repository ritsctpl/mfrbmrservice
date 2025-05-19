package com.rits.changeproductionservice.dto;

import com.rits.changeproductionservice.model.OriginalRoutingDetails;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ChangeProductionRequest {
    private String site;
    private List<OriginalRoutingDetails> originalRoutingDetailsList;
    private String newShopOrder;
    private String newOperation;
    private String newResource;
    private String newBomType;
    private String newBom;
    private String newBomVersion;
    private String newItem;
    private String newItemVersion;
    private String newRouting;
    private String newRoutingVersion;
    private String userBO;
    private boolean placeInQueueAtOperation;
    private boolean placeInQueueAtFirstOperation;
    private boolean placeInQueueAtCurrentOperation;
    private boolean placeInQueueAtFirstUncompletedOperation;
    private boolean skipActiveSFCs;
    private String comments;
}
