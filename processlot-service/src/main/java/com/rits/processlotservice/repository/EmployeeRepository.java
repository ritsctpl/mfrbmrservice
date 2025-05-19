package com.rits.processlotservice.repository;

import com.rits.processlotservice.dto.EmployeeResponse;
import com.rits.processlotservice.model.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface EmployeeRepository extends MongoRepository<Employee,String> {
    Employee findBySiteAndEmpIdAndUserName(String site,String empId,String userName);
    List<EmployeeResponse> findBySite(String site);
}
