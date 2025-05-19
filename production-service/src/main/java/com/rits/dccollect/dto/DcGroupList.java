package com.rits.dccollect.dto;


import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DcGroupList {
    private String dataCollection;
    private String version;
    private List<Parameter> parameterList;
}
