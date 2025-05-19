package com.rits.processlotservice.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.rits.processlotservice.dto.*;
import com.rits.processlotservice.model.EmployeeMessageModel;
import com.rits.processlotservice.model.MessageModel;

public interface ProcessLotService {
    public MessageModel createProcessLot(ProcessLotRequest processLotRequest) throws  Exception;
    public ProcessLotResponse retrievePcuByProcessLot(String site,String processLot) throws Exception;
    public String callExtension(Extension extension) throws Exception;
    public EmployeeMessageModel createHrmUser(EmployeeRequest employeeRequest) throws Exception;
    public EmployeeMessageModel updateHrmUser(EmployeeRequest employeeRequest) throws Exception;
    public EmployeeResponseList retrieveAll(String site);

}
