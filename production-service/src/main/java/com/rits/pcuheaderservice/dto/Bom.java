package com.rits.pcuheaderservice.dto;

import com.rits.bomheaderservice.model.BomComponent;
import com.rits.bomheaderservice.model.BomCustomData;
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
@Document(collection = "R_BOM")
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
