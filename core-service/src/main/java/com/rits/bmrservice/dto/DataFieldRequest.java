package com.rits.bmrservice.dto;

import com.rits.dataFieldService.model.ListDetails;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Setter
public class DataFieldRequest {
    private String site;
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
    private String userId;
}
