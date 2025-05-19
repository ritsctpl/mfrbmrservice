package com.rits.datacollectionservice.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Parameter {
    private String sequence;
    private String parameterName;
    private String description;
    private String type;
    private String prompt;
    private String status;
    private boolean allowMissingValues;
    private boolean displayDataValues;
    private String falseValue;
    private String trueValue;
    private String dataField;
    private String formula;
    private String minValue;
    private String maxValue;
    private String targetValue;
    private boolean softLimitCheckOnMinOrMaxValue;
    private boolean overrideMinOrMax;
    private boolean autoLogNconMinOrMaxOverride;
    private String certification;
    private String ncCode;
    private String mask;
    private String unitOfMeasure;
    private String requiredDataEntries;
    private String optionalDataEntries;
}
