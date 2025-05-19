package com.rits.queryBuilder.dto;

import lombok.Data;

import java.util.List;

@Data
public class QueryBuilderDTO {
    private String site;
    private String templateName;
    private String templateType;
    private String value;
    private String status;
}
