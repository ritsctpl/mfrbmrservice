package com.rits.queryBuilder.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableInfo {
    private String table;
    private List<String> columns;

}
