package com.rits.overallequipmentefficiency.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UniqueItemVersion {
    private String itemId;
    private String itemVersion;
}