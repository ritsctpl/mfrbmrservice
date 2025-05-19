package com.rits.worklistservice.service;

import com.rits.worklistservice.dto.*;
import com.rits.worklistservice.model.WorkList;

import java.util.List;

public interface WorkListService {

    public List<WorkListResponse> getWorkList(WorkListRequest workListRequest) throws Exception;
    public List<WorkListResponse> setValues(List<PcuInQueue> list) throws Exception;
    public List<WorkListResponse> setValuesOfPcuInWork(List<PcuInQueue> list) throws Exception;
    public List<WorkListResponse> setValuesOfPcuDone(List<PcuDone> list) throws Exception;
    public List<ColumnList> setShopOrder(PcuInQueue pcuInQueue) throws Exception;
    public List<WorkList> getFieldNameByCategory(String category) throws Exception ;
    boolean dummyWebCLient(String site);
    WorkListRequest convertToWorkListRequest(WorkListRequestNoBO workListRequestNoBO);

}
