package com.rits.inventoryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "R_DATA_TYPE")
public class DataType {
    @Id
    private String id;

    private String site;
    private String handle;
    private String dataType;
    private  String category;
    private String  description;

    private String preSaveActivity;

    private List<DataField> dataFieldList;
    private String createdBy;
    private String modifiedBy;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;

}
