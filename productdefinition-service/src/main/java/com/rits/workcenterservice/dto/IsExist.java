package com.rits.workcenterservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class IsExist {
    private String site;
    private String resource;
}
