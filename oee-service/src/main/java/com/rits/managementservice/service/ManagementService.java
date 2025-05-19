package com.rits.managementservice.service;

import com.rits.managementservice.dto.ManageRequest;
import com.rits.managementservice.dto.MessageModel;

public interface ManagementService {
    MessageModel create(ManageRequest manageRequest) throws Exception;
    MessageModel update(ManageRequest manageRequest) throws Exception;
    MessageModel delete(ManageRequest manageRequest) throws Exception;
    MessageModel retrieve(ManageRequest manageRequest) throws Exception;
    MessageModel retrieveAll(ManageRequest manageRequest) throws Exception;
    MessageModel retrieveForColorScheme(ManageRequest manageRequest) throws Exception;
    MessageModel retrieveForFilter(ManageRequest manageRequest) throws Exception;
}
