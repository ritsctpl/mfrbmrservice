package com.rits.bomheaderservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Bom {
    private String site;
    private String handle;
    private String bom;
    private String revision;
    private String description;
    private String status;
    private String bomType;
    private boolean currentVersion;
    private String validFrom;
    private boolean bomTemplate;
    private boolean isUsed;
    private String designCost;
    private List<BomComponent> bomComponentList;
    private List<BomCustomData> bomCustomDataList;
}
