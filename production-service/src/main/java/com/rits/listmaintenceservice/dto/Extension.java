package com.rits.listmaintenceservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Extension {
    private String site;
    private String hookPoint;
    private String activity;
    private String hookableMethod;
    private String request;
}
