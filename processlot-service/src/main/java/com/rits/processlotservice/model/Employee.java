package com.rits.processlotservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection="R_EMPLOYEE")
public class Employee {

    @Id
    private String handle;
    private String site;
    private String empId;
    private String userName;
    private String firstName;
    private String lastName;
    private String role;
    private String reportingManager;
    private LocalDate dateOfJoin;
    private String email;
    private String phoneNumber;
    private String status;
    private LocalDate dateOfBirth;
    private String address;
    private String gender;
    private String employmentStatus;
    private String department;
    private  String salaryRange;
    private String probationPeriod;
    private String profilePictureUrl;

}
