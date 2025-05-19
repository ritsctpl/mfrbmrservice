package com.rits.userservice.model;

import lombok.*;

import java.time.LocalDateTime;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LabourTracking {
    private String current;
    private String validFrom;
    private String validTo;
    private String userType;
    private String primaryShift;
    private String secondaryShift;
    private String costCenter;
    private String defaultLCC;
    private String department;
    private String details;
}
