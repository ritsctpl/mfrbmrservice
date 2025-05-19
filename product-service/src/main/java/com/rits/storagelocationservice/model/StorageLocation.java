package com.rits.storagelocationservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "R_STORAGE_LOCATION")
public class StorageLocation {
    @Id
    private String id;
    private String site;
    private String handle;
    private String description;
    private String storageLocation;
    private boolean ewmManagedStorageLocation;
    private List<WorkCenter> workCenterList;
    private int active;
    private LocalDateTime modifiedDateTime;
    private LocalDateTime createdDateTime;
}
