package com.rits.signoffservice.service;

import com.rits.signoffservice.dto.SignOffRequestList;
import com.rits.signoffservice.dto.SignOffRequestListDetails;
import com.rits.signoffservice.model.MessageModel;


public interface SignOffService {

    public MessageModel signOff(SignOffRequestList signOffRequestList) throws Exception;
    SignOffRequestList convertToSignOffRequestList(SignOffRequestListDetails signOffRequestListNoBO);
}

