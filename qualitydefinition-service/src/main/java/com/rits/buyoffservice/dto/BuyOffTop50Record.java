package com.rits.buyoffservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BuyOffTop50Record {
    private String buyOff;
    private String version;
    private String description;
}
