package com.rits.customdataformatservice.dto;

import lombok.*;
import org.springframework.data.annotation.Id;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CustomDataFormatRequest {

    private String site;
    private String code;
    private String description;
    private String character;
    private String dataField;
    private String leadingCharacter;
    private int fixedLength;
    private int active;
    private int sequence;

}
