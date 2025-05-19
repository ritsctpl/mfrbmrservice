package com.rits.queryBuilder.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "R_QUERY_BUILDER")
public class QueryBuilder {

    @Id
    private String id;
    private String site;
    private String templateName;
    private String templateType;
    private String value;
    private String status;

    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDatetime;
    private LocalDateTime modifiedDatetime;
    private int active;
}
