package com.rits.userservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "R_USER")
public class User {
    private List<String> site;
    @Id
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
    private String password;
    private String erpPersonnelNumber;
    private List<UserGroup> userGroups;
    private List<WorkCenter> workCenters;
    private List<LabourTracking> labourTracking;
    private List<Supervisors> supervisor;
    private List<LabourRules> labourRules;
    private List<CustomData> customDataList;
    private String createdBy;
    private String modifiedBy;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}
