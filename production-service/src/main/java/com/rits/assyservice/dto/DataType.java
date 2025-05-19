package com.rits.assyservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
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

    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;

}
