package com.rits.workcenterservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.rits.operationservice.dto.AuditLogRequest;
import com.rits.workcenterservice.dto.*;
import com.rits.workcenterservice.exception.WorkCenterException;
import com.rits.workcenterservice.model.Association;
import com.rits.workcenterservice.model.MessageModel;
import com.rits.workcenterservice.model.WorkCenter;

import java.util.List;

public interface WorkCenterService {
    public MessageModel createWorkCenter(WorkCenterRequest workCenterRequest) throws Exception;

    public MessageModel updateWorkCenter(WorkCenterRequest workCenterRequest) throws Exception;
    public WorkCenter retrieveWorkCenter(String workCenter, String  site) throws Exception;

    public MessageModel deleteWorkCenter(String workCenter, String site) throws Exception;

    public Boolean isWorkCenterExist(String workCenter, String site) throws Exception;

    public WorkCenterResponseList getAllWorkCenterList(String workCenter, String site) throws Exception;

    WorkCenterResponseList retrieveTop50(String site) throws Exception;

    public WorkCenterResponseList getErpWorkCenterList(String site) throws Exception;
    public WorkCenterResponseList getListOfWorkCenter(String workCenterCategory, String site) throws Exception;

    List<Association> associateObjectToWorkCenter(String workCenter, String site, List<Association> associationList) throws Exception;

    public List<Association> removeObjectFromWorkCenter(String workCenter, String site, List<String> sequence) throws Exception;

    public Association findDefaultResourceForWorkCenter(String workCenter, String site) throws Exception;

    public Response getParentWorkCenter(String workCenter, String site) throws Exception;
   // public String callExtension(Extension extension) throws Exception;
    public AvailableWorkCenterList getAllAvailableWorkCenter(String site,String workCenter) throws Exception;
    String getWorkCenterByResource(String site,String resource);


    AuditLogRequest createAuditLog(WorkCenterRequest workCenterRequest);

    AuditLogRequest updateAuditLog(WorkCenterRequest workCenterRequest);

    AuditLogRequest deleteAuditLog(RetrieveRequest workCenterRequest);

    List<WorkCenter> getTrackOeeWorkCenters(String site) throws Exception;

    /**
     * Given a lower-level workcenter id (for example, a line id) and the site,
     * returns the parent's cell workCenter field.
     */
    String getCellForWorkcenter(String childWorkCenterId, String site) throws WorkCenterException;

    /**
     * Given a cell id and site,
     * returns the parent's cell group workCenter field.
     */
    String getCellGroupForCell(String cellId, String site) throws WorkCenterException;

}
