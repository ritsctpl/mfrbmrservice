package com.rits.customdataformatservice.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CustomDataObject {
    protected String code;
    protected String dataField;
    protected String value;
}
