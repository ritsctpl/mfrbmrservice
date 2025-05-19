package com.rits.assemblyservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class DataFieldResponse {
    private String site;
    private String handle;
    private String dataField;
    private String type;
    private boolean qmSelectedSet;
    private String description;
    private String fieldLabel;
    private String maskGroup;
    private boolean browseIcon;
    private boolean trackable;
    private boolean mfrRef;
    private String preSaveActivity;
    private List<ListDetails> listDetails;
    private String createdBy;
    private String modifiedBy;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;

}
