package com.rits.mfrservice.service;
import java.util.List;


import com.fasterxml.jackson.databind.JsonNode;
import com.rits.activitygroupservice.dto.Extension;
import com.rits.customdataservice.dto.CustomDataRequest;
import com.rits.mfrservice.dto.MFRResponseList;
import com.rits.mfrservice.model.MessageModel;
import com.rits.mfrservice.dto.MfrRequest;
import com.rits.mfrservice.dto.Response;
import com.rits.mfrservice.model.Mfr;
import com.rits.mfrservice.model.MfrDataList;

public interface MfrService {
    public MessageModel createMfr(MfrRequest mfrRequest) throws Exception;

    Boolean isMfrExist(MfrRequest mfrRequest);




    public Mfr retrieveMfr(MfrRequest mfrRequest) throws Exception;


    MFRResponseList getMfrListByCreationDate( String site);
}
