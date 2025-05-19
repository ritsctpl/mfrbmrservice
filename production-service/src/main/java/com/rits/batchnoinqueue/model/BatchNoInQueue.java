package com.rits.batchnoinqueue.model;

import com.rits.batchnodoneservice.model.BatchNoDone;
import com.rits.batchnohold.model.BatchNoHold;
import com.rits.batchnoinwork.model.BatchNoInWork;
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
@Document(collection = "R_BATCH_NO_IN_QUEUE")
public class BatchNoInQueue {

    @Id
    private String handle;
    private String site;
    private LocalDateTime dateTime;
    private String batchNo;
    private String batchNoHeaderBO;
    private String material;
    private String materialVersion;
    private String recipe;
    private String recipeVersion;
    private String batchNoRecipeHeaderBO;
    private String phaseId;
    private String phaseSequence;
    private String operation;
    private String opSequence;
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

    public java.util.List<String> getFieldNames() {
        java.util.List<String> fieldNames = new ArrayList<>();

        Field[] fields = BatchNoInQueue.class.getDeclaredFields();
        for (Field field : fields) {
            fieldNames.add(field.getName());
        }

        return fieldNames;
    }

    public BatchNoInQueue(BatchNoInWork batchNoInWork) {
        this.handle = batchNoInWork.getHandle();
        this.site = batchNoInWork.getSite();
        this.dateTime = batchNoInWork.getDateTime();
        this.batchNo = batchNoInWork.getBatchNo();
        this.batchNoHeaderBO = batchNoInWork.getBatchNoHeaderBO();
        this.material = batchNoInWork.getMaterial();
        this.materialVersion = batchNoInWork.getMaterialVersion();
        this.recipe = batchNoInWork.getRecipe();
        this.recipeVersion = batchNoInWork.getRecipeVersion();
        this.batchNoRecipeHeaderBO = batchNoInWork.getBatchNoRecipeHeaderBO();
        this.phaseId = batchNoInWork.getPhaseId();
        this.operation = batchNoInWork.getOperation();
        this.quantityBaseUom = batchNoInWork.getQuantityBaseUom();
        this.quantityMeasuredUom = batchNoInWork.getQuantityMeasuredUom();
        this.baseUom = batchNoInWork.getBaseUom();
        this.measuredUom = batchNoInWork.getMeasuredUom();
        this.queuedTimestamp = batchNoInWork.getQueuedTimestamp();
        this.resource = batchNoInWork.getResource();
        this.workcenter = batchNoInWork.getWorkcenter();
        this.user = batchNoInWork.getUser();
        this.qtyToComplete = batchNoInWork.getQtyToComplete();
        this.qtyInQueue = batchNoInWork.getQtyInQueue();
        this.orderNumber = batchNoInWork.getOrderNumber();
        this.type = batchNoInWork.getType();
        this.active = batchNoInWork.getActive();
        this.createdDateTime = batchNoInWork.getCreatedDateTime();
        this.modifiedDateTime = batchNoInWork.getModifiedDateTime();
        this.createdBy = batchNoInWork.getCreatedBy();
        this.modifiedBy = batchNoInWork.getModifiedBy();
        this.baseUom = batchNoInWork.getBaseUom();
        this.productionVersion = batchNoInWork.getProductionVersion();
    }
    
    public BatchNoInQueue(BatchNoHold batchNohold) {
        this.handle = batchNohold.getHandle();
        this.site = batchNohold.getSite();
        this.dateTime = batchNohold.getCreatedDateTime();
        this.batchNo = batchNohold.getBatchNo();
        this.material = batchNohold.getMaterial();
        this.materialVersion = batchNohold.getMaterialVersion();
        this.recipe = batchNohold.getRecipe();
        this.recipeVersion = batchNohold.getRecipeVersion();
        this.phaseId = batchNohold.getPhaseId();
        this.operation = batchNohold.getOperation();
        this.resource = batchNohold.getResource();
        this.workcenter = batchNohold.getWorkcenter();
        this.user = batchNohold.getUser();
        this.qtyInQueue = batchNohold.getHoldQty(); // new used
        this.type = batchNohold.getType();
        this.orderNumber = batchNohold.getOrderNumber();
        this.active = batchNohold.getActive();
        this.createdDateTime = batchNohold.getCreatedDateTime();
        this.modifiedDateTime = batchNohold.getModifiedDateTime();
        this.createdBy = batchNohold.getCreatedBy();
        this.modifiedBy = batchNohold.getModifiedBy();
        this.baseUom = batchNohold.getBaseUom();
        this.productionVersion = batchNohold.getProductionVersion();
    }
}






