package com.rits.dhrservice.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AssemblyRequest {
    private String site;
    private String pcuBO;
    private String itemBO;
}
