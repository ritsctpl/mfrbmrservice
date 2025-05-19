package com.rits.queryBuilder.dto;

import lombok.Data;

@Data
public class DataSourceDTO {
    private String dataSourceId;
    private String dataSourceName;
    private String site;
    private String host;
    private String port;
    private String dataBase;
    private String username;
    private String password;
    private String user;
    private boolean status;
}