package com.rits.reasoncodeservice.service;

import com.rits.reasoncodeservice.dto.ReasonCodeRequest;
import com.rits.reasoncodeservice.dto.ResponseList;
import com.rits.reasoncodeservice.model.ReasonCode;
import com.rits.reasoncodeservice.model.ReasonCodeMessageModel;
import com.rits.resourceservice.dto.Extension;

public interface ReasonCodeService {
    ReasonCodeMessageModel create(ReasonCodeRequest reasonCodeRequest)throws Exception;

    ReasonCodeMessageModel update(ReasonCodeRequest reasonCodeRequest)throws Exception;

    ReasonCodeMessageModel delete(String site, String reasonCode,String userId)throws Exception;

    ReasonCode retrieve(String site, String reasonCode)throws Exception;

    ResponseList retrieveAll(String site) throws Exception;

    ResponseList retrieveTop50(String site) throws Exception;

    String callExtension(Extension extension);
}
