package com.rits.queryBuilder.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class FilterData {
    private String filterName;
    private String type;
    private String keyName;
    private String retriveFeild;
    private boolean status;
    private String controller;
    private String endpoint;
}
