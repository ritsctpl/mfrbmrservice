package com.rits.dhrservice.dto;

import com.rits.assemblyservice.model.AssemblyData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Component {
    private String stepId;
    private String sequence;
    private String component;
    private String qty;
    private String  inventoryBO;
    private LocalDateTime assembledDate;
    private String assembledBy;
    private boolean removed;
    private LocalDateTime removedDate;
    private String removedBy;
    private String operation;
    private String resourceBO;
    private boolean nonBom;
    private List<AssemblyData> assemblyDataList;
    private String timeBased;
    private String uniqueID;
    private LocalDateTime modifiedDateTime;
    private LocalDateTime partitionDate;
    private String removedOperationBO;
    private String removedResourceBO;
    private String planned;
    private LocalDateTime createdDateTime;
    private List<Component> childComponent;

}
