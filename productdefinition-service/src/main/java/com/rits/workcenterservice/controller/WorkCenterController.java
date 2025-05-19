package com.rits.workcenterservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.kafkaservice.ProducerEvent;
import com.rits.operationservice.dto.AuditLogRequest;
import com.rits.reasoncodeservice.dto.ReasonCodeRequest;
import com.rits.reasoncodeservice.dto.ResponseList;
import com.rits.reasoncodeservice.exception.ReasonCodeException;
import com.rits.routingservice.dto.RoutingRequest;
import com.rits.routingservice.dto.RoutingResponseList;
import com.rits.routingservice.exception.RoutingException;
import com.rits.workcenterservice.dto.*;
import com.rits.workcenterservice.exception.WorkCenterException;
import com.rits.workcenterservice.model.Association;
import com.rits.workcenterservice.model.MessageModel;
import com.rits.workcenterservice.model.WorkCenter;
import com.rits.workcenterservice.service.WorkCenterServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/workcenter-service")
public class WorkCenterController {

    private final WorkCenterServiceImpl workCenterService;
    private final ApplicationEventPublisher eventPublisher;

//    {
//      "site": "rits",
//      "workCenter": "workCenter1",
//     "workCenterCategory": "cell",
//      "status": "releasable"
//    }

//    {
//      "site": "rits",
//      "workCenter": "workCenterB",
//      "description": "A",
//      "status": "releasable",
//      "routing": "routing1",
//      "routingVersion": "A",
//      "workCenterCategory": "cell",
//      "defaultParentWorkCenter": "workCenterA",
//      "erpWorkCenter": "erp",
//      "associationList": [
//        {
//          "sequence": 1,
//          "type": "resource",
//          "associateId": "Res",
//          "status": "Test",
//          "defaultResource": true
//        },
//        {
//          "sequence": 2,
//          "type": "workCenter",
//          "associateId": "workCenterA",
//          "status": "Test",
//          "defaultResource": true
//        }
//      ],
//      "customDataList": [
//        {
//          "customData": "data",
//          "value": "value"
//        },
//        {
//          "customData": "data",
//          "value": "value"
//        }
//      ]
//    }
    @PostMapping("create")
    public ResponseEntity<?> createWorkCenter(@RequestBody WorkCenterRequest workCenterRequest) throws Exception {
        MessageModel createWorkCenter;
        if (workCenterRequest.getSite() != null && !workCenterRequest.getSite().isEmpty()) {
            try {
                createWorkCenter = workCenterService.createWorkCenter(workCenterRequest);
                AuditLogRequest activityLog = workCenterService.createAuditLog(workCenterRequest);
                eventPublisher.publishEvent(new ProducerEvent(activityLog));
                return ResponseEntity.ok(MessageModel.builder().message_details(createWorkCenter.getMessage_details()).response(createWorkCenter.getResponse()).build());
            } catch (WorkCenterException workCenterException) {
                throw workCenterException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkCenterException(1);
    }
    //{
    //  "site": "rits",
    //  "workCenter": "workCenter1",
    // "workCenterCategory": "cell",
    //  "status": "releasable"
    //}

    //{
    //  "site": "rits",
    //  "workCenter": "workCenterB",
    //  "description": "A",
    //  "status": "releasable",
    //  "routing": "routing1",
    //  "routingVersion": "A",
    //  "workCenterCategory": "cell",
    //  "defaultParentWorkCenter": "workCenterA",
    //  "erpWorkCenter": "erp",
    //  "associationList": [
    //    {
    //      "sequence": 1,
    //      "type": "resource",
    //      "associateId": "Res",
    //      "status": "Test",
    //      "defaultResource": true
    //    },
    //    {
    //      "sequence": 2,
    //      "type": "workCenter",
    //      "associateId": "workCenterA",
    //      "status": "Test",
    //      "defaultResource": true
    //    }
    //  ],
    //  "customDataList": [
    //    {
    //      "customData": "data",
    //      "value": "value"
    //    },
    //    {
    //      "customData": "data",
    //      "value": "value"
    //    }
    //  ]
    //}
    @PostMapping("update")
    public ResponseEntity<MessageModel> updateWorkCenter(@RequestBody WorkCenterRequest workCenterRequest) throws Exception {
        MessageModel updateWorkCenter;
            if (workCenterRequest.getSite() != null && !workCenterRequest.getSite().isEmpty()) {
            try {
                updateWorkCenter = workCenterService.updateWorkCenter(workCenterRequest);

                AuditLogRequest activityLog = workCenterService.updateAuditLog(workCenterRequest);
                eventPublisher.publishEvent(new ProducerEvent(activityLog));

                return ResponseEntity.ok( MessageModel.builder().message_details(updateWorkCenter.getMessage_details()).response(updateWorkCenter.getResponse()).build());
            } catch (WorkCenterException workCenterException) {
                throw workCenterException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkCenterException(1);
    }

    //{
    //  "site": "rits",
    //  "workCenter": "workCenterB"
    //}
    @PostMapping("delete")
    public ResponseEntity<MessageModel> deleteWorkCenter(@RequestBody RetrieveRequest workCenterRequest)//whole json
    {
        MessageModel deleteWorkCenter;
        if (workCenterRequest.getSite() != null && !workCenterRequest.getSite().isEmpty()) {
            try {
                deleteWorkCenter = workCenterService.deleteWorkCenter(workCenterRequest.getWorkCenter(), workCenterRequest.getSite());

                AuditLogRequest activityLog = workCenterService.deleteAuditLog(workCenterRequest);
                eventPublisher.publishEvent(new ProducerEvent(activityLog));

                return ResponseEntity.ok(deleteWorkCenter);
            } catch (WorkCenterException workCenterException) {
                throw workCenterException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkCenterException(1);
    }

    //{
    //    "site": "rits",
    //    "workCenter": "workCenter1"
    //}
    @PostMapping("retrieve")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<WorkCenter> retrieveWorkCenter(@RequestBody RetrieveRequest workCenterRequest) throws Exception {
        WorkCenter retrieveWorkCenter;
        if (workCenterRequest.getSite() != null && !workCenterRequest.getSite().isEmpty()) {
            try {
                retrieveWorkCenter = workCenterService.retrieveWorkCenter(workCenterRequest.getWorkCenter(), workCenterRequest.getSite());
                return ResponseEntity.ok(retrieveWorkCenter);
            } catch (WorkCenterException workCenterException) {
                throw workCenterException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkCenterException(1);
    }

    //{
    //  "site": "rits",
    //  "workCenter": "workCenterB"
    //}
    @PostMapping("isExist")
    public ResponseEntity<Boolean> isWorkCenterExist(@RequestBody RetrieveRequest workCenterRequest)//whole json
    {
        Boolean isWorkCenterExist;
        if (workCenterRequest.getSite() != null && !workCenterRequest.getSite().isEmpty()) {
            try {
                isWorkCenterExist = workCenterService.isWorkCenterExist(workCenterRequest.getWorkCenter(), workCenterRequest.getSite());
                return ResponseEntity.ok(isWorkCenterExist);
            } catch (WorkCenterException workCenterException) {
                throw workCenterException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkCenterException(1);
    }

    //{
    //    "site": "rits",
    //    "workCenter": "wo"
    //}
    @PostMapping("retrieveAll")
    public ResponseEntity<WorkCenterResponseList> getAllWorkCenterList(@RequestBody RetrieveRequest workCenterRequest) {
        WorkCenterResponseList retrieveAllWorkCenter;
        if (workCenterRequest.getSite() != null && !workCenterRequest.getSite().isEmpty()) {
            try {
                retrieveAllWorkCenter = workCenterService.getAllWorkCenterList(workCenterRequest.getWorkCenter(), workCenterRequest.getSite());
                return ResponseEntity.ok(retrieveAllWorkCenter);
            } catch (WorkCenterException workCenterException) {
                throw workCenterException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkCenterException(1);
    }

    //{
    //    "site": "rits",
    //    "workCenterCategory":"cell"
    //}



    @PostMapping("/retrieveTop50")
    public ResponseEntity<WorkCenterResponseList> retrieveTop50(@RequestBody RetrieveRequest workCenterRequest)
    {
        WorkCenterResponseList top50WorkCenters = null;
        if (workCenterRequest.getSite() != null && !workCenterRequest.getSite().isEmpty()) {
    try {
        top50WorkCenters = workCenterService.retrieveTop50(workCenterRequest.getSite());
        return ResponseEntity.ok(top50WorkCenters);
    } catch (WorkCenterException e) {
        throw e;
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
    }
        throw new WorkCenterException(1);
    }


    @PostMapping("retrieveByWorkCenterCategory")
    public ResponseEntity<WorkCenterResponseList> getListOfWorkCenter(@RequestBody RetrieveRequest workCenterRequest) {
        WorkCenterResponseList retrieveByWorkCenterCategory;
        if (workCenterRequest.getSite() != null && !workCenterRequest.getSite().isEmpty()) {
            try {
                retrieveByWorkCenterCategory = workCenterService.getListOfWorkCenter(workCenterRequest.getWorkCenterCategory(), workCenterRequest.getSite());
                return ResponseEntity.ok(retrieveByWorkCenterCategory);
            } catch (WorkCenterException workCenterException) {
                throw workCenterException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkCenterException(1);
    }


    //{
    //    "site":"rits",
    //    "workCenter":"workCenterA",
    //    "associationList":[
    //	{
    //		"sequence":"001",
    //		"type":"resource",
    //		"associateId":"resourceBo",
    //		"status":"Test",
    //		"defaultResource":false
    //	},
    //		{
    //		"sequence":"002",
    //		"type":"workCenter",
    //		"associateId":"workcenter1",
    //		"status":"Test",
    //		"defaultResource":true
    //	}
    //	]
    //}

    @PostMapping("add")
    public ResponseEntity<List<Association>> associationObjectToWorkCenter(@RequestBody WorkCenterRequest workCenterRequest) {
        List<Association> associationObjectToWorkCenter;
        if (workCenterRequest.getSite() != null && !workCenterRequest.getSite().isEmpty()) {
            try {
                associationObjectToWorkCenter = workCenterService.associateObjectToWorkCenter(workCenterRequest.getWorkCenter(), workCenterRequest.getSite(), workCenterRequest.getAssociationList());
                return ResponseEntity.ok(associationObjectToWorkCenter);
            } catch (WorkCenterException workCenterException) {
                throw workCenterException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkCenterException(1);
    }

    //{
    //    "site":"rits",
    //    "workCenter":"workCenter1",
    //    "sequence":[4,5]
    //}
    @PostMapping("remove")
    public ResponseEntity<List<Association>> removeObjectFromWorkCenter(@RequestBody RetrieveRequest workCenterRequest) {

        List<Association> removeObjectFromWorkCenter;
        if (workCenterRequest.getSite() != null && !workCenterRequest.getSite().isEmpty()) {

            try {
                removeObjectFromWorkCenter = workCenterService.removeObjectFromWorkCenter(workCenterRequest.getWorkCenter(), workCenterRequest.getSite(), workCenterRequest.getSequence());
                return ResponseEntity.ok(removeObjectFromWorkCenter);
            } catch (WorkCenterException workCenterException) {
                throw workCenterException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkCenterException(1);
    }

    //{
    //    "site":"rits",
    //    "workCenter":"workCenter22"
    //}
    @PostMapping("findDefaultResource")
    public ResponseEntity<Association> findDefaultResourceForWorkCenter(@RequestBody RetrieveRequest workCenterRequest) {
        Association findDefaultResource;
        if (workCenterRequest.getSite() != null && !workCenterRequest.getSite().isEmpty()) {
            try {
                findDefaultResource = workCenterService.findDefaultResourceForWorkCenter(workCenterRequest.getWorkCenter(), workCenterRequest.getSite());
                return ResponseEntity.ok(findDefaultResource);
            } catch (WorkCenterException workCenterException) {
                throw workCenterException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkCenterException(1);
    }

    //{
    //    "site":"rits",
    //    "workCenter":"workCenter22"
    //}
    @PostMapping("getParentWorkCenter")
    public ResponseEntity<Response> getParentWorkCenter(@RequestBody RetrieveRequest workCenterRequest) {

        Response getParentWorkCenterResponse;
        if (workCenterRequest != null && workCenterRequest.getSite() != null && !workCenterRequest.getSite().isEmpty()) {
            try {
                getParentWorkCenterResponse = workCenterService.getParentWorkCenter(workCenterRequest.getWorkCenter(), workCenterRequest.getSite());
                return ResponseEntity.ok(getParentWorkCenterResponse);
            } catch (WorkCenterException workCenterException) {
                throw workCenterException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkCenterException(1);
    }
    //{
    //    "site":"rits",
    //    "workCenter":"workCenter22"
    //}

    //{
    //    "site":"rits"
    //}
    @PostMapping("retrieveBySite")
    public ResponseEntity<AvailableWorkCenterList> getAllAvailableWorkCenter(@RequestBody RetrieveRequest workCenterRequest) {

        AvailableWorkCenterList getAllAvailableWorkCenter;
        if (workCenterRequest != null && workCenterRequest.getSite() != null && !workCenterRequest.getSite().isEmpty()) {
            try {
                getAllAvailableWorkCenter = workCenterService.getAllAvailableWorkCenter(workCenterRequest.getSite(), workCenterRequest.getWorkCenter());
                return ResponseEntity.ok(getAllAvailableWorkCenter);
            } catch (WorkCenterException workCenterException) {
                throw workCenterException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkCenterException(1);
    }


    @PostMapping("getErpWorkCenterList")
    public ResponseEntity<WorkCenterResponseList> getErpWorkCenterList(@RequestBody RetrieveRequest workCenterRequest) {
        WorkCenterResponseList getErpWorkCenterList;
        if (workCenterRequest.getSite() != null && !workCenterRequest.getSite().isEmpty()) {

            try {
                getErpWorkCenterList = workCenterService.getErpWorkCenterList(workCenterRequest.getSite());
                return ResponseEntity.ok(getErpWorkCenterList);
            } catch (WorkCenterException workCenterException) {
                throw workCenterException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkCenterException(1);
    }
    @PostMapping("getWorkCenterByResource")
    public ResponseEntity<String> getErpWorkCenterByResourceWorkCenterList(@RequestBody RetrieveRequest workCenterRequest) {
        String getWorkCenter;
        if (workCenterRequest.getSite() != null && !workCenterRequest.getSite().isEmpty()) {

            try {
                getWorkCenter = workCenterService.getWorkCenterByResource(workCenterRequest.getSite(),workCenterRequest.getResource());
                return ResponseEntity.ok(getWorkCenter);
            } catch (WorkCenterException workCenterException) {
                throw workCenterException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkCenterException(1);
    }

    @PostMapping("retrieveTrackOeeWorkcenters")
    public ResponseEntity<List<WorkCenter>> getTrackOeeWorkCenters(@RequestBody RetrieveRequest workCenterRequest) {
        if (workCenterRequest != null && workCenterRequest.getSite() != null && !workCenterRequest.getSite().isEmpty()) {
            try {
                List<WorkCenter> trackOeeWorkCenters = workCenterService.getTrackOeeWorkCenters(workCenterRequest.getSite());
                return ResponseEntity.ok(trackOeeWorkCenters);
            } catch (WorkCenterException workCenterException) {
                throw workCenterException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkCenterException(1);
    }

    /**
     * POST endpoint to get the cell (parent) for a given lower-level workcenter.
     * The JSON payload is mapped to RetrieveRequest, for example:
     * {
     *     "site": "1004",
     *     "workCenter": "TABLETLINE1_WC"
     * }
     * The service returns the cell workCenter value (e.g. "TABLET").
     */
    @PostMapping("/getCell")
    public ResponseEntity<String> getCellForWorkcenter(@RequestBody RetrieveRequest request) {
        try {
            // For a lower-level workcenter request, we expect the workCenter field to contain the child workcenter id.
            String childWorkCenterId = request.getWorkCenter();
            String site = request.getSite();
            String cell = workCenterService.getCellForWorkcenter(childWorkCenterId, site);
            return ResponseEntity.ok(cell);
        } catch (WorkCenterException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * POST endpoint to get the cell group (parent) for a given cell.
     * The JSON payload is mapped to RetrieveRequest, for example:
     * {
     *     "site": "1004",
     *     "workCenter": "TABLET"
     * }
     * In this case, the workCenter field is treated as the cell id, and the service returns the cell group (e.g. "MAKALI").
     */
    @PostMapping("/getCellGroup")
    public ResponseEntity<String> getCellGroupForCell(@RequestBody RetrieveRequest request) {
        try {
            // For a cell group lookup, assume the workCenter field holds the cell id.
            String cellId = request.getWorkCenter();
            String site = request.getSite();
            String cellGroup = workCenterService.getCellGroupForCell(cellId, site);
            return ResponseEntity.ok(cellGroup);
        } catch (WorkCenterException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
