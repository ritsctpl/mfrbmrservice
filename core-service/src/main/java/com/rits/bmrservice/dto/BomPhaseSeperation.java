package com.rits.bmrservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BomPhaseSeperation {
    private String handle;
    private Object title;
//    private List<Object> data;
    private String BatchSize;
    private Object phaseSeperationData;
    private int active;
    private String site;
}
