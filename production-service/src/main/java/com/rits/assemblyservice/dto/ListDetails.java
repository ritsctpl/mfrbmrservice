package com.rits.assemblyservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ListDetails {
    private boolean defaultLabel;
    private String sequence;
    private String fieldValue;
    private String labelValue;
    private String type;
    private String width;
    private String api;
    private String parameters;
    private List<Values> values;


}

