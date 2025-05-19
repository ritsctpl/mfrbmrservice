package com.rits.processlotservice.dto;

import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class EmployeeRequest {

    private String site;
    private String empId;
    private String userName;
    private String firstName;
    private String lastName;
    private String role;
    private String reportingManager;
    private LocalDate dateOfJoin;
    private String email;
    private String status;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String address;
    private String gender;
    private String employmentStatus;
    private String department;
    private  String salaryRange;
    private String probationPeriod;
    private String profilePictureUrl;

}
