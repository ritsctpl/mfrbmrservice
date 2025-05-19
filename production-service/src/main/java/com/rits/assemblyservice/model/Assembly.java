package com.rits.assemblyservice.model;

import com.rits.assemblyservice.dto.Ancestry;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
@Document(collection = "R_PCU_ASSY_DATA ")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Assembly {
    private String site;
    private String pcuBomBO;
    @Id
    private String pcuBO;
    private String shopOrderBO;
    private String itemBO;
    private String pcuRouterBO;
    private String mainParent;
    private String immediateParent;
    private String tags;
    private String parentOrderBO;
    private String parentPcuBO;
    private int level;
    private List<Component> componentList;
    /* Added for PCU add or Update Component.  - Senthil POC. ancestry
     * */
    private List<Ancestry> ancestry;
    private int active;
}
