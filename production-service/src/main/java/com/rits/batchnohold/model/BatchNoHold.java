package com.rits.batchnohold.model;

import com.rits.batchnoinqueue.model.BatchNoInQueue;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import org.springframework.data.annotation.Id;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Document("R_BATCH_NO_HOLD")
public class BatchNoHold {
    private String site;
    @Id
    private String handle;
    private String batchNo;
    private String status;
    private String phaseId;
    private String operation;
    private String resource;
    private String orderNumber;
    private String workcenter;
    private String material;
    private String materialVersion;
    private String recipe;
    private String recipeVersion;
    private String scrapQuantity;
    private BigDecimal holdQty;
    private String user;
    private String reasonCode;
    private String comment;
    private String batchNoHeaderHandle;
    private String batchNoRecipeHeaderHandle;
    private String type;
    private boolean qualityApproval;
    private String productionVersion;
    private String baseUom;
    private int active;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;

    public java.util.List<String> getFieldNames() {
        java.util.List<String> fieldNames = new ArrayList<>();

        Field[] fields = BatchNoHold.class.getDeclaredFields();
        for (Field field : fields) {
            fieldNames.add(field.getName());
        }

        return fieldNames;
    }

    public BatchNoHold(BatchNoInQueue batchNoInQueue) {
        this.handle = batchNoInQueue.getHandle();
        this.site = batchNoInQueue.getSite();
        this.createdDateTime = batchNoInQueue.getDateTime();
        this.batchNo = batchNoInQueue.getBatchNo();
        this.material = batchNoInQueue.getMaterial();
        this.materialVersion = batchNoInQueue.getMaterialVersion();
        this.recipe = batchNoInQueue.getRecipe();
        this.recipeVersion = batchNoInQueue.getRecipeVersion();
        this.phaseId = batchNoInQueue.getPhaseId();
        this.operation = batchNoInQueue.getOperation();
        this.resource = batchNoInQueue.getResource();
        this.workcenter = batchNoInQueue.getWorkcenter();
        this.user = batchNoInQueue.getUser();
        this.holdQty = batchNoInQueue.getQtyInQueue();
        this.orderNumber = batchNoInQueue.getOrderNumber();
        this.type = batchNoInQueue.getType();
        this.active = batchNoInQueue.getActive();
        this.modifiedDateTime = batchNoInQueue.getModifiedDateTime();
        this.createdBy = batchNoInQueue.getCreatedBy();
        this.modifiedBy = batchNoInQueue.getModifiedBy();
    }
}
