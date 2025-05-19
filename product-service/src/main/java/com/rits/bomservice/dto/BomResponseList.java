package com.rits.bomservice.dto;


import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class BomResponseList {
    private List<BomResponse> bomList;
}
