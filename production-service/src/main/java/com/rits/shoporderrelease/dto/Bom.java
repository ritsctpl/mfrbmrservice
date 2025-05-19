package com.rits.shoporderrelease.dto;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
        private long designCost;
        private List<BomComponent> bomComponentList;
        private List<BomCustomData> bomCustomDataList;
        private int active;
        private LocalDateTime createdDateTime;
        private LocalDateTime modifiedDateTime;
}