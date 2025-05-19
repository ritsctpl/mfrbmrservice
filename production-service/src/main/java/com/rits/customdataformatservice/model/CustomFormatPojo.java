package com.rits.customdataformatservice.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CustomFormatPojo {
    private String handle;
    private String code;
    private String description;
    private String character;
    private String data_field;
    private String leading_char;
    private int fixed_len;
    private Boolean validated;
}
