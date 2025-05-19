package com.rits.worklistservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class PcuInQueue {
    private String site;
    private String handle;
    private LocalDateTime dateTime;
    private String pcuBO;
    private String itemBO;
    private String routerBO;
    private String operationBO;
    private String resourceBO;
    private String stepID;
    private String userBO;
    private String qtyInQueue;
    private String shopOrderBO;
    private String childRouterBO;
    private String parentStepID;
    private String type;
    private int active;
    private String qtyInWork;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    public java.util.List<String> getFieldNames() {
        java.util.List<String> fieldNames = new ArrayList<>();

        Field[] fields = PcuInQueue.class.getDeclaredFields();
        for (Field field : fields) {
            fieldNames.add(field.getName());
        }

        return fieldNames;
    }
}
