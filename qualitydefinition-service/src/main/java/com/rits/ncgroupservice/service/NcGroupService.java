package com.rits.ncgroupservice.service;

import com.rits.ncgroupservice.dto.*;
import com.rits.ncgroupservice.model.NcGroupMessageModel;
import com.rits.ncgroupservice.model.NcCodeDPMOCategory;
import com.rits.ncgroupservice.model.NcGroup;
import com.rits.ncgroupservice.model.Operation;

import java.util.List;

public interface NcGroupService {
    public NcGroupMessageModel createNcGroup(NcGroupRequest ncGroupRequest) throws Exception;

    public NcGroupMessageModel updateNcGroup(NcGroupRequest ncGroupRequest) throws Exception;

    public NcGroup retrieveNcGroup(String site, String ncGroup) throws Exception;

    public NcGroupResponseList getAllNCGroup(String site, String ncGroup) throws Exception;

    public NcGroupResponseList getAllNCGroupByCreatedDate(String site) throws Exception;

    public NcGroupMessageModel deleteNcGroup(String site, String ncGroup, String userId) throws Exception;

    public List<NcCodeDPMOCategory> getAllNcCode(String site, String ncGroup) throws Exception;

    public Boolean assignNcCode(String site, String ncGroup, List<NcCodeDPMOCategory> ncCodeList) throws Exception;

    public Boolean removeNcCode(String site, String ncGroup, List<NcCodeDPMOCategory> ncCodeList) throws Exception;

    public List<Operation> getAllOperation(String site, String ncGroup) throws Exception;

    public List<Operation> assignOperation(String site, String ncGroup, List<Operation> operationList) throws Exception;

    public List<Operation> removeOperation(String site, String ncGroup, List<Operation> operationList) throws Exception;

    public String callExtension(Extension extension) throws Exception;

    public List<NcGroupResponse> getAvailableNcGroup(String site) throws Exception;
    public List<NcGroupResponse> getNcGroupByOperation(String site,String operation) throws Exception ;

    }
