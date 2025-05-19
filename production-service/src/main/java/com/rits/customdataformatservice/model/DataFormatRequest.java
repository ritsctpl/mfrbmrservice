package com.rits.customdataformatservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DataFormatRequest {

    private String datastring;
    private String site;
    private String user;

}