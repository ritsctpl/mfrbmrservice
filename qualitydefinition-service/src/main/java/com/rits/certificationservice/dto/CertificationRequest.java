package com.rits.certificationservice.dto;

import com.rits.certificationservice.model.CustomData;
import com.rits.certificationservice.model.UserGroup;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CertificationRequest {
    private String site;
    private String certification;
    private String description;
    private String durationType;
    private String duration;
    private String status;
    private String maxNumberOfExtensions;
    private String maxExtensionDuration;
    private List<UserGroup> userGroupList;
    private List<CustomData> customDataList;
    private String userId;
}
