package com.rits.dhrservice.dto;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Assembly {
    private String site;
    private String pcuBomBO;
    private String pcuBO;
    private String shopOrderBO;
    private String itemBO;
    private String pcuRouterBO;
    private String tags;
    private String parentOrderBO;
    private String parentPcuBO;
    private int level;
    private List<Component> componentList;
    private int active;
}
