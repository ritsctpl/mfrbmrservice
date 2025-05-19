package com.rits.queryBuilder.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class QueryBuilderDTO {
    private String site;
    private String templateName;
    private String templateType;
    private String value;
    private String status;
    private String user;
    private String startTime;
    private String endTime;
    private Map<String, Object> filters;
}
