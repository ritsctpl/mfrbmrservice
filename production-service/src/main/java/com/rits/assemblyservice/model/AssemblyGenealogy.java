package com.rits.assemblyservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "R_ASSEMBLY_GENEALOGY ")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AssemblyGenealogy {
    private String site;
    private String pcuBomBO;
    @Id
    private String pcuBO;
    private String shopOrderBO;
    private String mainParent;
    private String immediateParent;
    private String itemBO;
    private String pcuRouterBO;
    private String tags;
    private String parentOrderBO;
    private String parentPcuBO;
    private int level;
    private List<Component> componentList;
    private int active;
}
