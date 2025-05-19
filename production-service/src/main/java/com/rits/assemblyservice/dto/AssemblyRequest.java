package com.rits.assemblyservice.dto;

import com.rits.assemblyservice.model.Component;
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
    private String parentPcuBO;
    private String itemBO;
    private List<Component>   componentList;
    private boolean inventoryReturn;
    private boolean inventoryScrap;
    private String userId;

}
