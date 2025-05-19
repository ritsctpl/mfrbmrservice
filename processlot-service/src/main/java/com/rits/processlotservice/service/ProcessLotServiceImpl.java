package com.rits.processlotservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.rits.processlotservice.dto.*;
import com.rits.processlotservice.exception.ProcessLotException;
import com.rits.processlotservice.model.*;
import com.rits.processlotservice.repository.EmployeeRepository;
import com.rits.processlotservice.repository.ProcessLotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProcessLotServiceImpl  implements ProcessLotService{
    private final ProcessLotRepository processLotRepository;
    private final EmployeeRepository employeeRepository;
    private final WebClient.Builder webClientBuilder;
    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;


    @Override
    public MessageModel createProcessLot(ProcessLotRequest processLotRequest) throws Exception {
        if(processLotRepository.existsByActiveAndSiteAndProcessLot(1,processLotRequest.getSite(),processLotRequest.getProcessLot())){
            throw new ProcessLotException(3801,processLotRequest.getProcessLot());
        }else {
            ProcessLot processLot = ProcessLot.builder()
                    .site(processLotRequest.getSite())
                    .processLot(processLotRequest.getProcessLot())
                    .processLotMember(processLotRequest.getProcessLotMember())
                    .createdDateTime(LocalDateTime.now())
                    .createdBy(processLotRequest.getCreatedBy())
                    .active(1)
                    .build();
            return MessageModel.builder().message_details(new MessageDetails(processLotRequest.getProcessLot() + " created SuccessFully", "S")).response(processLotRepository.save(processLot)).build();
        }
    }

    @Override
    public ProcessLotResponse retrievePcuByProcessLot(String site, String processLot) throws Exception {
        ProcessLot existingProcessLot=processLotRepository.findByActiveAndSiteAndProcessLot(1,site,processLot);
        if(existingProcessLot==null){
            throw new ProcessLotException(3802,processLot);
        }
        return ProcessLotResponse.builder().processLotMembers(existingProcessLot.getProcessLotMember()).build();
    }
    @Override
    public String callExtension(Extension extension) throws Exception {
        String extensionResponse = webClientBuilder.build()
                .post()
                .uri(extensionUrl)
                .bodyValue(extension)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        if (extensionResponse == null) {
            throw new ProcessLotException(800);
        }
        return extensionResponse;
    }

    @Override
    public EmployeeMessageModel createHrmUser(EmployeeRequest employeeRequest) throws Exception {

        Employee existingEmployee=employeeRepository.findBySiteAndEmpIdAndUserName(
        employeeRequest.getSite(),employeeRequest.getEmpId(),employeeRequest.getUserName());

        validateRequiredFields(employeeRequest);

        if(existingEmployee!=null){
         throw new ProcessLotException(409,employeeRequest.getEmpId());
        }

        Employee newEmployee=createBuilder(employeeRequest);
        employeeRepository.save(newEmployee);

        return EmployeeMessageModel.builder()
                .message_details(new MessageDetails("Employee Details Are Created Successfully","S"))
                .response(newEmployee)
                .build();
    }

    @Override
    public EmployeeMessageModel updateHrmUser(EmployeeRequest employeeRequest) throws Exception {

        Employee existingEmployee=employeeRepository.findBySiteAndEmpIdAndUserName(
                employeeRequest.getSite(),employeeRequest.getEmpId(),employeeRequest.getUserName());

       if(existingEmployee!=null){
         Employee updatedEmployee=updateBuilder(existingEmployee,employeeRequest);
          employeeRepository.save(updatedEmployee);
           return EmployeeMessageModel.builder()
                   .message_details(new MessageDetails("Employee Details Are Updated Successfully","S"))
                   .response(updatedEmployee)
                   .build();
       }
       else{
         throw new ProcessLotException(421);
       }

    }

    @Override
    public EmployeeResponseList retrieveAll(String site) {
        List<EmployeeResponse> employeeResponseList=employeeRepository.findBySite(site);
        return new EmployeeResponseList(employeeResponseList);

    }

    private Employee createBuilder(EmployeeRequest employeeRequest){
    Employee employee=Employee.builder()
            .site(employeeRequest.getSite())
            .handle("EmployeeBO:"+employeeRequest.getSite()+","+employeeRequest.getEmpId()+","+employeeRequest.getUserName())
            .empId(employeeRequest.getEmpId())
            .userName(employeeRequest.getUserName())
            .firstName(employeeRequest.getFirstName())
            .lastName(employeeRequest.getLastName())
            .role(employeeRequest.getRole())
            .reportingManager(employeeRequest.getReportingManager())
            .dateOfJoin(employeeRequest.getDateOfJoin())
            .email(employeeRequest.getEmail())
            .phoneNumber(employeeRequest.getPhoneNumber())
            .dateOfBirth(employeeRequest.getDateOfBirth())
            .address(employeeRequest.getAddress())
            .gender(employeeRequest.getGender())
            .employmentStatus(employeeRequest.getEmploymentStatus())
            .department(employeeRequest.getDepartment())
            .salaryRange(employeeRequest.getSalaryRange())
            .probationPeriod(employeeRequest.getProbationPeriod())
            .profilePictureUrl(employeeRequest.getProfilePictureUrl())
            .status(employeeRequest.getStatus())
            .build();

            return employee;
    }
    private void validateRequiredFields(EmployeeRequest employeeRequest) {
        if (isNullOrEmpty(employeeRequest.getSite())) {
            throw new ProcessLotException(410);
        }
        if (isNullOrEmpty(employeeRequest.getEmpId())) {
            throw new ProcessLotException(411);
        }
        if (isNullOrEmpty(employeeRequest.getUserName())) {
            throw new ProcessLotException(412);
        }
        if (isNullOrEmpty(employeeRequest.getRole())) {
            throw new ProcessLotException(413);
        }
        if (isNullOrEmpty(employeeRequest.getReportingManager())) {
            throw new ProcessLotException(414);
        }
        if (employeeRequest.getDateOfJoin() == null) {
            throw new ProcessLotException(415);
        }
        if (isNullOrEmpty(employeeRequest.getEmail())) {
            throw new ProcessLotException(416);
        }
        if (isNullOrEmpty(employeeRequest.getDepartment())) {
            throw new ProcessLotException(417);
        }
        if (isNullOrEmpty(employeeRequest.getSalaryRange())) {
            throw new ProcessLotException(418);
        }
        if (isNullOrEmpty(employeeRequest.getProbationPeriod())) {
            throw new ProcessLotException(419);
        }
        if (isNullOrEmpty(employeeRequest.getEmploymentStatus())) {
            throw new ProcessLotException(420);
        }

    }
    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private Employee updateBuilder(Employee existingEmployee, EmployeeRequest employeeRequest){
      existingEmployee.setFirstName(employeeRequest.getFirstName());
      existingEmployee.setHandle("EmployeeBO:"+employeeRequest.getSite()+","+employeeRequest.getEmpId()+","+employeeRequest.getUserName());
      existingEmployee.setLastName(employeeRequest.getLastName());
      existingEmployee.setRole(employeeRequest.getRole());
      existingEmployee.setReportingManager(employeeRequest.getReportingManager());
      existingEmployee.setDateOfJoin(employeeRequest.getDateOfJoin());
      existingEmployee.setEmail(employeeRequest.getEmail());
      existingEmployee.setPhoneNumber(employeeRequest.getPhoneNumber());
      existingEmployee.setDateOfBirth(employeeRequest.getDateOfBirth());
      existingEmployee.setAddress(employeeRequest.getAddress());
      existingEmployee.setGender(employeeRequest.getGender());
      existingEmployee.setEmploymentStatus(employeeRequest.getEmploymentStatus());
      existingEmployee.setDepartment(employeeRequest.getDepartment());
      existingEmployee.setSalaryRange(employeeRequest.getSalaryRange());
      existingEmployee.setProbationPeriod(employeeRequest.getProbationPeriod());
      existingEmployee.setProfilePictureUrl(employeeRequest.getProfilePictureUrl());
      existingEmployee.setStatus(employeeRequest.getStatus());
      return existingEmployee;
    }
}
