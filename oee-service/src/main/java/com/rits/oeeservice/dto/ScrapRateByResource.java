package com.rits.oeeservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ScrapRateByResource {
    private String resource;
    private int scrap;
}
