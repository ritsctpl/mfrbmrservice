package com.rits.toolgroupservice.controller;

import com.rits.kafkaservice.ProducerEvent;
import com.rits.operationservice.dto.AuditLogRequest;
import com.rits.toolgroupservice.dto.AttachmentListResponseList;
import com.rits.toolgroupservice.dto.Response;
import com.rits.toolgroupservice.dto.ToolGroupListResponseList;
import com.rits.toolgroupservice.dto.ToolGroupRequest;
import com.rits.toolgroupservice.exception.ToolGroupException;
import com.rits.toolgroupservice.model.ToolGroup;
import com.rits.toolgroupservice.model.ToolGroupMessageModel;
import com.rits.toolgroupservice.service.ToolGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


//import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/toolgroup-service")
public class ToolGroupController {

    private  final  ToolGroupService toolGroupService;
    private final ApplicationEventPublisher eventPublisher;




    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createToolGroup(@RequestBody ToolGroupRequest toolGroupRequest){
        ToolGroupMessageModel createResponse=null;
        if(toolGroupRequest.getSite()!=null && !toolGroupRequest.getSite().isEmpty()){
            try {
                createResponse= toolGroupService.createToolGroup(toolGroupRequest);
                AuditLogRequest activityLog = AuditLogRequest.builder()
                        .site(toolGroupRequest.getSite())
                        .action_code("TOOLGROUP-CREATE")
                        .action_detail("ToolGroup Created "+toolGroupRequest.getToolGroup())
                        .action_detail_handle("ActionDetailBO:"+toolGroupRequest.getSite()+","+"TOOLGROUP-CREATE"+","+toolGroupRequest.getUserId()+":"+"com.rits.toolgroupservice.service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(toolGroupRequest.getUserId())
                        .txnId("TOOLGROUP-CREATE"+String.valueOf(LocalDateTime.now())+toolGroupRequest.getUserId())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("Create")
                        .topic("audit-log")
                        .build();
                eventPublisher.publishEvent(new ProducerEvent(activityLog));

                return ResponseEntity.ok(createResponse);
            } catch(ToolGroupException e) {
                throw e;
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ToolGroupException(1202, toolGroupRequest.getSite());
    }



//
//                        {
//                            "site":"RITS",
//                                "toolGroup":"tgroup1",
//                                "description":"1",
//                                "status":"",
//                                "trackingControl":"t1",
//                                "location":"1",
//                                "toolQty":"1",
//                                "timeBased":"1",
//                                "erpGroup":"1",
//                                "attachmentList":[
//                            {
//                                "sequence":"1",
//                                    "quantityRequired":"",
//                                    "item":"",
//                                    "itemVersion":"",
//                                    "routing":"",
//                                    "routingVersion":"",
//                                    "stepId":"",
//                                    "operation":"",
//                                    "workCenter":"",
//                                    "resource":"",
//                                    "resourceType":"",
//                                    "shopOrder":""
//                            }
//                    ],
//                            "calibrationType":"",
//                                "startCalibrationDate":"",
//                                "calibrationPeriod":"",
//                                "calibrationCount":"",
//                                "expirationDate":"",
//                                "maximumCalibrationCount":"",
//                                "customDataList" : [
//                            {
//                                "customData": "",
//                                    "value": ""
//                            },
//                            {
//                                "customData": "",
//                                    "value": ""
//                            }
//                    ]
//                        }
//    @PostMapping("/create")
//    @ResponseStatus(HttpStatus.CREATED)
//    public ResponseEntity<ToolGroup> createToolGroup(@RequestBody ToolGroupRequest toolGroupRequest){
//        ToolGroup createResponse=new ToolGroup();
//        if(toolGroupRequest.getSite()!=null && !toolGroupRequest.getSite().isEmpty()){
//            try {
//                createResponse= toolGroupService.createToolGroup(toolGroupRequest);
//                return ResponseEntity.ok(createResponse);
//            }catch(ToolGroupException e){
//                throw e;
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }
//        throw new ToolGroupException(1202, toolGroupRequest.getSite());
//    }

//                    @PostMapping("/create")
//                    @ResponseStatus(HttpStatus.CREATED)
//                    public ResponseEntity<ToolGroup> createToolGroup( @RequestBody ToolGroupRequest toolGroupRequest) {
//                        try {
//                            ToolGroup toolGroup = toolGroupService.createToolGroup(toolGroupRequest);
//                            return ResponseEntity.ok(toolGroup);
//                        } catch (ToolGroupException e) {
//                            throw e;
//                        } catch (Exception e) {
//                            throw new ToolGroupException(500, e.getMessage());
//                        }
//                    }



                    //    {
                    //        "site":"RITS",
                    //            "toolGroup":"tgroup1",
                    //            "description":"1",
                    //            "status":"",
                    //            "trackingControl":"t1",
                    //            "location":"1",
                    //            "toolQty":"1",
                    //            "timeBased":"1",
                    //            "erpGroup":"1",
                    //            "attachmentList":[
                    //        {
                    //            "sequence":"1",
                    //                "quantityRequired":"",
                    //                "item":"",
                    //                "itemVersion":"",
                    //                "routing":"",
                    //                "routingVersion":"",
                    //                "stepId":"",
                    //                "operation":"",
                    //                "workCenter":"",
                    //                "resource":"",
                    //                "resourceType":"",
                    //                "shopOrder":""
                    //        }
                    //],
                    //        "calibrationType":"",
                    //            "startCalibrationDate":"",
                    //            "calibrationPeriod":"",
                    //            "calibrationCount":"",
                    //            "expirationDate":"",
                    //            "maximumCalibrationCount":"",
                    //            "customDataList" : [
                    //        {
                    //            "customData": "",
                    //                "value": ""
                    //        },
                    //        {
                    //            "customData": "",
                    //                "value": ""
                    //        }
                    //]
                    //    }
    @PostMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ToolGroupMessageModel> updateToolGroup(@RequestBody ToolGroupRequest toolGroupRequest) {
        ToolGroupMessageModel updateResponse=null;
        if(toolGroupRequest.getSite()!=null && !toolGroupRequest.getSite().isEmpty()){
            try {
                updateResponse= toolGroupService.updateToolGroup(toolGroupRequest);
                AuditLogRequest activityLog = AuditLogRequest.builder()
                        .site(toolGroupRequest.getSite())
                        .action_code("TOOLGROUP-UPDATE")
                        .action_detail("ToolGroup Updated "+toolGroupRequest.getToolGroup())
                        .action_detail_handle("ActionDetailBO:"+toolGroupRequest.getSite()+","+"TOOLGROUP-UPDATE"+","+toolGroupRequest.getUserId()+":"+"com.rits.toolgroupservice.service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(toolGroupRequest.getUserId())
                        .txnId("TOOLGROUP-UPDATE"+String.valueOf(LocalDateTime.now())+toolGroupRequest.getUserId())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("Update")
                        .topic("audit-log")
                        .build();
                eventPublisher.publishEvent(new ProducerEvent(activityLog));
                return ResponseEntity.ok(updateResponse);
            }catch(ToolGroupException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ToolGroupException(1202, toolGroupRequest.getSite());
    }



    //   {
//        "site":"RITS",
//            "toolGroup":"tgroup1"
//    }
    @PostMapping("/retrieve")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ToolGroup> retrieveToolGroup(@RequestBody ToolGroupRequest toolGroupRequest) {
        ToolGroup toolGroupResponse=new ToolGroup();
        if(toolGroupRequest.getSite()!=null && !toolGroupRequest.getSite().isEmpty()){
            try {
                toolGroupResponse= toolGroupService.retrieveToolGroup(toolGroupRequest);
                return ResponseEntity.ok(toolGroupResponse);
            } catch(ToolGroupException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ToolGroupException(1202, toolGroupRequest.getSite());
    }


    //   {
//        "site":"RITS",
//    }

    @PostMapping("/retrieveTop50")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ToolGroupListResponseList> getToolGroupListByCreationDate(@RequestBody ToolGroupRequest toolGroupRequest) {
        ToolGroupListResponseList top50Response=new ToolGroupListResponseList();
        if(toolGroupRequest.getSite()!=null && !toolGroupRequest.getSite().isEmpty()){
            try {
                top50Response= toolGroupService.getToolGroupListByCreationDate(toolGroupRequest);
                return ResponseEntity.ok(top50Response);
            }catch(ToolGroupException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ToolGroupException(1202, toolGroupRequest.getSite());
    }

    @PostMapping("/retrieveByAttachment")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> retrieveByAttachment(@RequestBody ToolGroupRequest toolGroupRequest) {
        List<ToolGroup> retrievedByAttachmentToolGroup =null;
            try {
                retrievedByAttachmentToolGroup= toolGroupService.retrieveByAttachment(toolGroupRequest.getAttachmentList());
                return ResponseEntity.ok(retrievedByAttachmentToolGroup);
            }catch(ToolGroupException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }



    //   {
//        "site":"RITS",
//            "toolGroup":"tg"    //partial value ,it reteives all . if- the toolgroup is empty value then it retrieves top50
//    }
    @PostMapping("/retrieveAll")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ToolGroupListResponseList> getToolGroupList(@RequestBody ToolGroupRequest toolGroupRequest) {
        ToolGroupListResponseList toolGroupListResponse=new ToolGroupListResponseList();
        if(toolGroupRequest.getSite()!=null && !toolGroupRequest.getSite().isEmpty()) {
            try {
                toolGroupListResponse= toolGroupService.getToolGroupList(toolGroupRequest);
                return ResponseEntity.ok(toolGroupListResponse);
            }catch(ToolGroupException e){
                throw e;
            }  catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ToolGroupException(1202, toolGroupRequest.getSite());
    }



    //   {
//        "site":"RITS",
//            "toolGroup":"tgroup1"
//    }
    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ToolGroupMessageModel> deleteToolGroup(@RequestBody ToolGroupRequest toolGroupRequest){
        ToolGroupMessageModel deleteResponse=null;
        if(toolGroupRequest.getSite()!=null && !toolGroupRequest.getSite().isEmpty()) {
            try {
                deleteResponse= toolGroupService.deleteToolGroup(toolGroupRequest);
                AuditLogRequest activityLog = AuditLogRequest.builder()
                        .site(toolGroupRequest.getSite())
                        .action_code("TOOLGROUP-DELETE")
                        .action_detail("ToolGroup Deleted "+toolGroupRequest.getToolGroup())
                        .action_detail_handle("ActionDetailBO:"+toolGroupRequest.getSite()+","+"TOOLGROUP-DELETE"+","+toolGroupRequest.getUserId()+":"+"com.rits.toolgroupservice.service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(toolGroupRequest.getUserId())
                        .txnId("TOOLGROUP-DELETE"+String.valueOf(LocalDateTime.now())+toolGroupRequest.getUserId())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("Delete")
                        .topic("audit-log")
                        .build();
                eventPublisher.publishEvent(new ProducerEvent(activityLog));
                return ResponseEntity.ok(deleteResponse);
            }catch(ToolGroupException e){
                throw e;
            }  catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ToolGroupException(1202, toolGroupRequest.getSite());
    }



 //   {
//        "site":"RITS",
//            "toolGroup":"tgroup1"
//    }
    @PostMapping("/isExist")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> isToolGroupExist(@RequestBody ToolGroupRequest toolGroupRequest){
        Boolean isExistResponse;
        if(toolGroupRequest.getSite()!=null && !toolGroupRequest.getSite().isEmpty()) {
            try {
                isExistResponse= toolGroupService.isToolGroupExist(toolGroupRequest);
                return ResponseEntity.ok(isExistResponse);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ToolGroupException(1202, toolGroupRequest.getSite());
    }



//    {
//        "site":"RITS",
//            "toolGroup":"tgroup1"
//    }
    @PostMapping("/retrieveAttachmentList")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<AttachmentListResponseList> getMeasurementPointsList(@RequestBody  ToolGroupRequest toolGroupRequest){
        AttachmentListResponseList attachmentListResponse=new AttachmentListResponseList();
        if(toolGroupRequest.getSite()!=null && !toolGroupRequest.getSite().isEmpty()) {
            try {
                attachmentListResponse= toolGroupService.getAttachmentList(toolGroupRequest);
                return ResponseEntity.ok(attachmentListResponse);
            }catch (ToolGroupException e) {
                throw e;
            }catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ToolGroupException(1202, toolGroupRequest.getSite());
    }


}
