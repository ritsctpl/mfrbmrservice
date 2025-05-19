package com.rits.processlotservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponseList {
   private List<EmployeeResponse> employeeResponseList;
}
