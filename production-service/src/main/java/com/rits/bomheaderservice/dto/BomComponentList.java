package com.rits.bomheaderservice.dto;

import com.rits.bomheaderservice.model.BomComponent;
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
