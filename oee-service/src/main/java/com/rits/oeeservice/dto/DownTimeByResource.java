package com.rits.oeeservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class DownTimeByResource {
    private double downTime;
    private String resource;
}
