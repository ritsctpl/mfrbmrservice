package com.rits.listmaintenceservice.service;

import com.rits.listmaintenceservice.dto.Extension;
import com.rits.listmaintenceservice.dto.ListMaintenanceRequest;
import com.rits.listmaintenceservice.dto.ListMaintenanceResponseList;
import com.rits.listmaintenceservice.model.ListMaintenance;
import com.rits.listmaintenceservice.model.MessageModel;

public interface ListMaintenanceService {
    public MessageModel createListMaintenance(ListMaintenanceRequest listMaintenanceRequest) throws  Exception;
    public MessageModel updateListMaintenance(ListMaintenanceRequest listMaintenanceRequest)throws Exception;

    public MessageModel deleteListMaintenance(String site,String list,String category)throws  Exception;
    public ListMaintenance retrieveListMaintenance(String site, String list,String category)throws Exception;
    public ListMaintenanceResponseList getAllListMaintenance(String site,String list)throws Exception;
    public ListMaintenanceResponseList getAllListMaintenanceByCreatedDate(String site)throws Exception;

    public ListMaintenanceResponseList getAllListByCategory(String site,String category) throws Exception;
    public String callExtension(Extension extension);
}
