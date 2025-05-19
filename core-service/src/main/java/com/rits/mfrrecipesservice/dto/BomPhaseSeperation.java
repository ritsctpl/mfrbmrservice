package com.rits.mfrrecipesservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "R_PHASE_SEPERATION")
public class BomPhaseSeperation {
    @Id
    private String id;
    private String handle;
    private Object title;
//    private List<Object> data;
    private String BatchSize;
    private Object phaseSeperationData;
    private int active;
    private String site;
    private String dataField;
}
