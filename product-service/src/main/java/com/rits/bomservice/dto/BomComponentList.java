package com.rits.bomservice.dto;

import com.rits.bomservice.model.BomComponent;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class BomComponentList {
    private List<BomComponent> bomComponentList;
}
