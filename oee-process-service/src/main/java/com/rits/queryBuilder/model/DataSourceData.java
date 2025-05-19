package com.rits.queryBuilder.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "r_datasource")
public class DataSourceData {

    @Id
    private String dataSourceId;
    private String handle;
    private String dataSourceName;
    private boolean status;
    private String site;
    private String host;
    private String port;
    private String dataBase;
    private String username;
    private String password;
    private String url;
    private LocalDateTime createdDatetime;
    private LocalDateTime modifiedDatetime;
    private String createdBy;
    private String modifiedBy;
    private int active;

}
