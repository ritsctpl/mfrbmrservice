package com.rits.componentbuilderservice.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TableConfig {
    private String columns;
    private List<Column> columnNames;
    private String rows;
    private List<Object> rowData;
}
