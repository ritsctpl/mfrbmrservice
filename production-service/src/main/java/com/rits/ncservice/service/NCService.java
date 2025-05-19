package com.rits.ncservice.service;

import com.rits.ncservice.dto.DispositionRoutingResponse;
import com.rits.ncservice.dto.DispositionRoutings;
import com.rits.ncservice.dto.NCCodeRequest;

import java.util.List;

public interface NCService {

    public List<DispositionRoutings> getDispositionRoutings(String nCCode, String site)throws Exception;
    public List<String> getListOfNcCode(String NcGroup, String site) throws Exception;

}
