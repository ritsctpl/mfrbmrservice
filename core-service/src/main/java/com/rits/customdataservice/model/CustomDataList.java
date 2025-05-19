package com.rits.customdataservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CustomDataList {
    private String sequence;
    private String customData;
    private String fieldLabel;
    private boolean required;
}
