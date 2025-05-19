package com.rits.datatypeservice.dto;

import com.rits.datatypeservice.model.DataField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DataTypeRequest {
    private String site;
    private String handle;
    private String dataType;
    private  String category;
    private String  description;
    private String preSaveActivity;
    private List<DataField> dataFieldList;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String userId;
}
