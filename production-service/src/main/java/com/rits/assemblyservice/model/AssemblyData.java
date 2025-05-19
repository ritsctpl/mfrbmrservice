package com.rits.assemblyservice.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AssemblyData {
    private String sequence;
    private String dataField;
    private String value;
    //changed from dataAttribute to value. coz jana asked me too
}
