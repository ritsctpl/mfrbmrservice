package com.rits.userservice.dto;

import com.rits.userservice.model.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserRequest {
    private List<String> site;
    private String handle;
    private String defaultSite;
    private String currentSite;
    private String user;
    private String lastName;
    private String firstName;
    private String emailAddress;
    private String status;
    private String employeeNumber;
    private String hireDate;
    private String erpUser;
    private String erpPersonnelNumber;
    private String password;
    private List<UserGroup> userGroups;
    private List<WorkCenter> workCenters;
    private List<LabourTracking> labourTracking;
    private List<Supervisors> supervisor;
    private List<LabourRules> labourRules;
    private List<CustomData> customDataList;
    private String userId;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;

}
