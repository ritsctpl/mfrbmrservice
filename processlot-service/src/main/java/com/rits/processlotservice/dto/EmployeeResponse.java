package com.rits.processlotservice.dto;

import com.rits.processlotservice.model.Employee;
import lombok.*;

import java.time.LocalDate;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeResponse {
    private String site;
    private String empId;
    private String userName;
    private String role;
    private String reportingManager;
    private LocalDate dateOfJoin;
    private String email;
    private String phoneNumber;
    private String status;
    public EmployeeResponse(Employee employee){
      this.site=employee.getSite();
      this.empId=employee.getEmpId();
      this.userName=employee.getUserName();
      this.role=employee.getRole();
      this.reportingManager=employee.getReportingManager();
      this.dateOfJoin=employee.getDateOfJoin();
      this.email=employee.getEmail();
      this.phoneNumber=employee.getPhoneNumber();
      this.status=employee.getStatus();
    }
}
