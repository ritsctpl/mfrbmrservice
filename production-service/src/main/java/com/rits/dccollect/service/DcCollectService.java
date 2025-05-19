package com.rits.dccollect.service;

import com.rits.dccollect.dto.*;
import com.rits.dccollect.model.DcCollectMessageModel;
import com.rits.dccollect.model.ParametricMeasures;

import java.util.List;

public interface DcCollectService {
    public List<DcGroupList> getDataCollectionGroupList(DcCollectRequest dcCollectRequest) throws Exception;

    public List<Parameter> getParameterList(DcCollectRequest dcCollectRequest) throws Exception;

    public DataCollection retrievedDataCollection(DcCollectRequest dcCollectRequest)throws Exception;

    public DcCollectMessageModel logDc(List<DcCollectRequest> dcCollectRequest) throws Exception;

    List<DcSaveParametricMeasures> retrieveByDcGroupAndVersion(DcCollectRequest dcCollectRequest) throws Exception;

    List<DcSaveParametricMeasures> retrieve(String site, String pcu, String dataCollection, String version)throws Exception;

    public MessageModel saveDraft(List<DcCollectRequest> dcCollectRequestList) throws Exception;

    public Boolean parameterValidation(DcCollectRequest dcCollectRequest)throws Exception;

    public DcParametricPreSave retrievePreSaved (DcCollectRequest dcCollectRequest)throws Exception;

    Boolean isExists(DcCollectRequest dcCollectRequest)throws Exception;

    public DcGroupResponse findDcGroupNameByName(DcCollectRequest dcCollectRequest)throws Exception;

    List<ParametricMeasures> retrieveForDataCollection(String site, String pcu);

    public List<DcGroupResponse> findDcGroupNameByResource(DcCollectRequest dcCollectRequest) throws Exception;

    public List<DcGroupResponse> getCurrentDataCollection(DcCollectRequest dcCollectRequest) throws Exception;

    public List<DcGroupResponse> getAllDataCollection(DcCollectRequest dcCollectRequest) throws Exception;

    public DcGroupResponse findCurrentDcGroupByName(DcCollectRequest dcCollectRequest)throws Exception;

    public DcGroupResponse findDcGroupByNameAndVersion(DcCollectRequest dcCollectRequest) throws Exception;
}
