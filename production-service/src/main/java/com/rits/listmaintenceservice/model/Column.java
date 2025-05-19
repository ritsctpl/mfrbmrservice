package com.rits.listmaintenceservice.model;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Column {
    private String columnSequence;
    private String columnName;
    private String rowSortOrder;
    private String width;
    private Detail details;

}
