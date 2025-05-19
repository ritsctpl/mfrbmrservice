package com.rits.barcodeservice.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ListDetails {

   private Integer sequence;
    private String dataField;
    private String character;
    private String delimeter;
    private String fixedLength;
    private String description;


}
