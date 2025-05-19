package com.rits.assyservice.dto;
import com.rits.assyservice.model.AssyData;
import lombok.*;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AssyRequest {
    private String site;
    private String pcuBO;
    private String parentPcuBO;
    private String itemBO;
    private List<AssyData.Component>   componentList;
    private boolean inventoryReturn;
    private boolean inventoryScrap;
    private String userId;
    private AssyData.Component component;

}