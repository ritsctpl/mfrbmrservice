package com.rits.customdataformatservice.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class MultiScanObj {
    private Boolean leadChar;
    private String fixed_length;
    private String character;
    private Integer characterlength;
}
