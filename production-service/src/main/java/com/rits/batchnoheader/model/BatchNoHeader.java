package com.rits.batchnoheader.model;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Document(collection = "R_BATCHNO_HEADER")
public class BatchNoHeader {

    @Id
    private String handle;
    private String site;
    private String batchNo;
    private String orderNo;
    private String material;
    private String materialVersion;
    private String status;
    private BigDecimal totalQuantity;
    private BigDecimal qtyToWorkOrder;
    private BigDecimal qtyInQueue;
    private BigDecimal qtyInHold;
    private BigDecimal qtyDone;
    private String recipeName;
    private String recipeVersion;
    private String baseUom;
    private String measuredUom;
    private BigDecimal conversionFactor;
    private BigDecimal releasedQuantityBaseUom;
    private BigDecimal releasedQuantityMeasuredUom;

    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String createdBy;
    private String modifiedBy;
}
