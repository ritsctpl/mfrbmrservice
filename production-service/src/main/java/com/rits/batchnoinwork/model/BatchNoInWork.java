package com.rits.batchnoinwork.model;

import com.rits.batchnoinqueue.model.BatchNoInQueue;
import com.rits.worklistservice.dto.PcuInQueue;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "R_BATCH_NO_IN_WORK")
public class BatchNoInWork {
    private String site;
    @Id
    private String handle;
    private LocalDateTime dateTime;
    private String batchNo;
    private String material;
    private String materialVersion;
    private String recipe;
    private String recipeVersion;
    private String batchNoHeaderBO;
    private String batchNoRecipeHeaderBO;
    private String phaseId;
    private String operation;
    private BigDecimal quantityBaseUom;
    private BigDecimal quantityMeasuredUom;
    private String baseUom;
    private String measuredUom;
    private LocalDateTime queuedTimestamp;
    private String resource;
    private String workcenter;
    private String user;
    private BigDecimal qtyToComplete;
    private BigDecimal qtyInQueue;
    private String orderNumber;
    private boolean qualityApproval;
    private String productionVersion;
    private String type;
    private int active;

    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String createdBy;
    private String modifiedBy;

    public BatchNoInWork(BatchNoInQueue batchNoInQueue) {
        this.handle = batchNoInQueue.getHandle();
        this.site = batchNoInQueue.getSite();
        this.dateTime = batchNoInQueue.getDateTime();
        this.batchNo = batchNoInQueue.getBatchNo();
        this.batchNoHeaderBO = batchNoInQueue.getBatchNoHeaderBO();
        this.material = batchNoInQueue.getMaterial();
        this.materialVersion = batchNoInQueue.getMaterialVersion();
        this.recipe = batchNoInQueue.getRecipe();
        this.recipeVersion = batchNoInQueue.getRecipeVersion();
        this.batchNoRecipeHeaderBO = batchNoInQueue.getBatchNoRecipeHeaderBO();
        this.phaseId = batchNoInQueue.getPhaseId();
        this.operation = batchNoInQueue.getOperation();
        this.quantityBaseUom = batchNoInQueue.getQuantityBaseUom();
        this.quantityMeasuredUom = batchNoInQueue.getQuantityMeasuredUom();
        this.baseUom = batchNoInQueue.getBaseUom();
        this.measuredUom = batchNoInQueue.getMeasuredUom();
        this.queuedTimestamp = batchNoInQueue.getQueuedTimestamp();
        this.resource = batchNoInQueue.getResource();
        this.workcenter = batchNoInQueue.getWorkcenter();
        this.user = batchNoInQueue.getUser();
        this.qtyToComplete = batchNoInQueue.getQtyToComplete();
        this.qtyInQueue = batchNoInQueue.getQtyInQueue();
        this.orderNumber = batchNoInQueue.getOrderNumber();
        this.type = batchNoInQueue.getType();
        this.active = batchNoInQueue.getActive();
        this.createdDateTime = batchNoInQueue.getCreatedDateTime();
        this.modifiedDateTime = batchNoInQueue.getModifiedDateTime();
        this.createdBy = batchNoInQueue.getCreatedBy();
        this.modifiedBy = batchNoInQueue.getModifiedBy();
    }

    public java.util.List<String> getFieldNames() {
        java.util.List<String> fieldNames = new ArrayList<>();

        Field[] fields = BatchNoInWork.class.getDeclaredFields();
        for (Field field : fields) {
            fieldNames.add(field.getName());
        }

        return fieldNames;
    }
}
