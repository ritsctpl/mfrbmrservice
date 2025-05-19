package com.rits.customdataservice.model;

import java.time.LocalDateTime;
import java.util.List;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "R_CUSTOM_DATA")
public class CustomData {
    @Id
    private String handle;
    private String site;
    private String category;
    private List<CustomDataList> customDataList;
    private String createdBy;
    private String modifiedBy;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;

}
