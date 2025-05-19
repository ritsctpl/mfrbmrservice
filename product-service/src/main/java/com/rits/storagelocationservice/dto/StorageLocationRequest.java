package com.rits.storagelocationservice.dto;

import com.rits.storagelocationservice.model.WorkCenter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StorageLocationRequest {
    private String site;
    private String handle;
    private String description;
    private String storageLocation;
    private boolean ewmManagedStorageLocation;
    private List<WorkCenter> workCenterList;
    private int active;
    private LocalDateTime modifiedDateTime;
    private LocalDateTime createdDateTime;
    private String userId;
}
