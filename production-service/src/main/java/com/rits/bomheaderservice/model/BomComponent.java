package com.rits.bomheaderservice.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.lang.reflect.Field;
import java.util.ArrayList;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BomComponent {
    private int assySequence;
    private String component;
    private String componentVersion;
    private String componentType;
    private String componentDescription;
    private String assyOperation;
    private String assembledQty;
    private String assyQty;
    private String assemblyDataTypeBo;
    private String storageLocationBo;
    private int maxUsage;
    private int maxNc;
    public java.util.List<String> getFieldNames() {
        java.util.List<String> fieldNames = new ArrayList<>();

        Field[] fields = BomComponent.class.getDeclaredFields();
        for (Field field : fields) {
            fieldNames.add(field.getName());
        }

        return fieldNames;
    }


}
