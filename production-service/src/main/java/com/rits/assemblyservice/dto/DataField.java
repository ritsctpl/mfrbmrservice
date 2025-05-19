package com.rits.assemblyservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DataField {
    private String sequence;
    private String dataField;
    private boolean required;
    private String description;
    private String dataType;
    private boolean browseIcon;
    private List<DetailsList> details;
}
