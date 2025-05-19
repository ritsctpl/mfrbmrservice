package com.rits.toolnumberservice.controller;

import com.rits.kafkaservice.ProducerEvent;
import com.rits.operationservice.dto.AuditLogRequest;
import com.rits.toolnumberservice.dto.MeasurementPointsResponseList;
import com.rits.toolnumberservice.dto.Response;
import com.rits.toolnumberservice.dto.ToolNumberRequest;
import com.rits.toolnumberservice.dto.ToolNumberResponseList;
import com.rits.toolnumberservice.exception.ToolNumberException;
import com.rits.toolnumberservice.model.ToolNumber;
import com.rits.toolnumberservice.model.ToolNumberMessageModel;
import com.rits.toolnumberservice.service.ToolNumberService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/toolnumber-service")
public class ToolNumberController {
    private final ToolNumberService toolNumberService;
    private final ApplicationEventPublisher eventPublisher;



                //    {
                //        "site":"RITS",
                //            "toolNumber":"tool4",
                //            "toolBo":"",
                //            "description":"a",
                //            "status":"",
                //            "toolGroup":"group1",
                //            "qtyAvailable":"",
                //            "erpEquipmentNumber":"",
                //            "erpPlanMaintenanceOrder":"",
                //            "toolQty":"",
                //            "location":"",
                //            "calibrationType":"",
                //            "startCalibrationDate":"",
                //            "calibrationPeriod":"",
                //            "calibrationCount":"",
                //            "maximumCalibrationCount":"",
                //            "expirationDate":"",
                //            "toolGroupSetting":"",
                //            "measurementPointsList":[
                //        {
                //            "measurementPoint":"m1"
                //        },
                //        {
                //            "measurementPoint":"m1"
                //        }
                //	],
                //        "customDataList" : [
                //        {
                //            "customData": "",
                //                "value": ""
                //        },
                //        {
                //            "customData": "",
                //                "value": ""
                //        }
                //    ]
                //
                //    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createToolNumber(@RequestBody ToolNumberRequest toolNumberRequest){
        ToolNumberMessageModel createResponse=null;
        if(toolNumberRequest.getSite()!=null && !toolNumberRequest.getSite().isEmpty()){
            try {
                createResponse= toolNumberService.createToolNumber(toolNumberRequest);

                AuditLogRequest activityLog = AuditLogRequest.builder()
                        .site(toolNumberRequest.getSite())
                        .action_code("TOOLNUMBER-CREATE")
                        .action_detail("ToolNumber Created "+toolNumberRequest.getToolNumber())
                        .action_detail_handle("ActionDetailBO:"+toolNumberRequest.getSite()+","+"TOOLNUMBER-CREATE"+","+toolNumberRequest.getUserId()+":"+"com.rits.toolnumberservice.service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(toolNumberRequest.getUserId())
                        .txnId("TOOLNUMBER-CREATE"+String.valueOf(LocalDateTime.now())+toolNumberRequest.getUserId())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("Create")
                        .topic("audit-log")
                        .build();
                eventPublisher.publishEvent(new ProducerEvent(activityLog));

                return ResponseEntity.ok(createResponse);
            }catch(ToolNumberException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ToolNumberException(1102, toolNumberRequest.getSite());

    }



//                    {
//                        "site":"RITS",
//                            "toolNumber":"tool4",
//                            "toolBo":"",
//                            "description":"a",
//                            "status":"",
//                            "toolGroup":"group1",
//                            "qtyAvailable":"",
//                            "erpEquipmentNumber":"",
//                            "erpPlanMaintenanceOrder":"",
//                            "toolQty":"",
//                            "location":"",
//                            "calibrationType":"",
//                            "startCalibrationDate":"",
//                            "calibrationPeriod":"",
//                            "calibrationCount":"",
//                            "maximumCalibrationCount":"",
//                            "expirationDate":"",
//                            "toolGroupSetting":"",
//                            "measurementPointsList":[
//                        {
//                            "measurementPoint":"m1"
//                        },
//                        {
//                            "measurementPoint":"m1"
//                        }
//                    ],
//                        "customDataList" : [
//                        {
//                            "customData": "",
//                                "value": ""
//                        },
//                        {
//                            "customData": "",
//                                "value": ""
//                        }
//                    ]
//
//                    }

    @PostMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ToolNumberMessageModel> updateToolNumber(@RequestBody ToolNumberRequest toolNumberRequest) {
        ToolNumberMessageModel updateResponse=null;
        if(toolNumberRequest.getSite()!=null && !toolNumberRequest.getSite().isEmpty()){
            try {
                updateResponse= toolNumberService.updateToolNumber(toolNumberRequest);
                AuditLogRequest activityLog = AuditLogRequest.builder()
                        .site(toolNumberRequest.getSite())
                        .action_code("TOOLNUMBER-UPDATE")
                        .action_detail("ToolNumber Updated "+toolNumberRequest.getToolNumber())
                        .action_detail_handle("ActionDetailBO:"+toolNumberRequest.getSite()+","+"TOOLNUMBER-UPDATE"+","+toolNumberRequest.getUserId()+":"+"com.rits.toolnumberservice.service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(toolNumberRequest.getUserId())
                        .txnId("TOOLNUMBER-UPDATE"+String.valueOf(LocalDateTime.now())+toolNumberRequest.getUserId())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("Update")
                        .topic("audit-log")
                        .build();
                eventPublisher.publishEvent(new ProducerEvent(activityLog));
                return ResponseEntity.ok(updateResponse);
            }catch(ToolNumberException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ToolNumberException(1102, toolNumberRequest.getSite());
    }



//    {
//        "site":"RITS",
//            "toolNumber":"tool1"
//    }

    @PostMapping("/retrieve")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ToolNumber> retrieveToolNumber(@RequestBody ToolNumberRequest toolNumberRequest) {
        ToolNumber toolNumberResponse=new ToolNumber();
        if(toolNumberRequest.getSite()!=null && !toolNumberRequest.getSite().isEmpty()){
            try {
                toolNumberResponse= toolNumberService.retrieveToolNumber(toolNumberRequest);
                return ResponseEntity.ok(toolNumberResponse);
            }catch(ToolNumberException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ToolNumberException(1102, toolNumberRequest.getSite());
    }

    @PostMapping("/updateCurrentCount")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> updateCurrentCount(@RequestBody ToolNumberRequest toolNumberRequest) {
        Boolean toolNumberResponse=null;
            try {
                toolNumberResponse= toolNumberService.updateCurrentCount(toolNumberRequest);
                return ResponseEntity.ok(toolNumberResponse);
            }catch(ToolNumberException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }



//    {
//        "site":"RITS"
//    }
    @PostMapping("/retrieveTop50")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ToolNumberResponseList> getToolNumberListByCreationDate(@RequestBody ToolNumberRequest toolNumberRequest) {
        ToolNumberResponseList top50Response=new ToolNumberResponseList();
        if(toolNumberRequest.getSite()!=null && !toolNumberRequest.getSite().isEmpty()) {
            try {
                top50Response= toolNumberService.getToolNumberListByCreationDate(toolNumberRequest);
                return ResponseEntity.ok(top50Response);
            }catch(ToolNumberException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ToolNumberException(1102, toolNumberRequest.getSite());
    }

    @PostMapping("/retrieveEnabledToolNumber")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ToolNumberResponseList> getEnabledToolNumberList(@RequestBody ToolNumberRequest toolNumberRequest) {
        ToolNumberResponseList top50Response=new ToolNumberResponseList();
        if(toolNumberRequest.getSite()!=null && !toolNumberRequest.getSite().isEmpty()) {
            try {
                top50Response= toolNumberService.getEnabledToolNumber(toolNumberRequest);
                return ResponseEntity.ok(top50Response);
            }catch(ToolNumberException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ToolNumberException(1102, toolNumberRequest.getSite());
    }



    //    {
//        "site":"RITS",
//            "toolNumber":"t"    //parial valuw it retrievs all. if toolNumber is empty it gives Top50
//    }
    @PostMapping("/retrieveAll")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ToolNumberResponseList> getToolNumberList(@RequestBody ToolNumberRequest toolNumberRequest) {
        ToolNumberResponseList toolNumberListResponse=new ToolNumberResponseList();
        if(toolNumberRequest.getSite()!=null && !toolNumberRequest.getSite().isEmpty()) {
            try {
                toolNumberListResponse= toolNumberService.getToolNumberList(toolNumberRequest);
                return ResponseEntity.ok(toolNumberListResponse);
            }catch(ToolNumberException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ToolNumberException(1102, toolNumberRequest.getSite());
    }


//        {
//        "site":"RITS",
//            "toolNumber":"tool1"
//    }
    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ToolNumberMessageModel> deleteToolNumber(@RequestBody ToolNumberRequest toolNumberRequest){
        ToolNumberMessageModel deleteResponse=null;
        if(toolNumberRequest.getSite()!=null && !toolNumberRequest.getSite().isEmpty()) {
            try {
                deleteResponse= toolNumberService.deleteToolNumber(toolNumberRequest);

                AuditLogRequest activityLog = AuditLogRequest.builder()
                        .site(toolNumberRequest.getSite())
                        .action_code("TOOLNUMBER-DELETE")
                        .action_detail("ToolNumber Deleted "+toolNumberRequest.getToolNumber())
                        .action_detail_handle("ActionDetailBO:"+toolNumberRequest.getSite()+","+"TOOLNUMBER-DELETE"+","+toolNumberRequest.getUserId()+":"+"com.rits.toolnumberservice.service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(toolNumberRequest.getUserId())
                        .txnId("TOOLNUMBER-DELETE"+String.valueOf(LocalDateTime.now())+toolNumberRequest.getUserId())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("Delete")
                        .topic("audit-log")
                        .build();
                eventPublisher.publishEvent(new ProducerEvent(activityLog));

                return  ResponseEntity.ok(deleteResponse);
            }catch(ToolNumberException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ToolNumberException(1102, toolNumberRequest.getSite());
    }



    //    {
//        "site":"RITS",
//            "toolNumber":"tool1"
//    }
    @PostMapping("/isExist")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> isToolNumberExist(@RequestBody ToolNumberRequest toolNumberRequest){
        Boolean isExistResponse;
        if(toolNumberRequest.getSite()!=null && !toolNumberRequest.getSite().isEmpty()) {
            try {
                isExistResponse= toolNumberService.isToolNumberExist(toolNumberRequest);
                return  ResponseEntity.ok(isExistResponse);
            }  catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ToolNumberException(1102, toolNumberRequest.getSite());
    }



                //    {
            //        "site":"RITS",
            //            "toolNumber":"tool1"
            //    }

    @PostMapping("/retrieveMeasurementPointsList")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<MeasurementPointsResponseList> getMeasurementPointsList(@RequestBody  ToolNumberRequest toolNumberRequest){
        MeasurementPointsResponseList measurementPointsListResponse=new MeasurementPointsResponseList();
        if(toolNumberRequest.getSite()!=null && !toolNumberRequest.getSite().isEmpty()) {
            try {
                measurementPointsListResponse= toolNumberService.getMeasurementPointsList(toolNumberRequest);
                return ResponseEntity.ok(measurementPointsListResponse);
            }catch (ToolNumberException e) {
                throw e;
            }catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ToolNumberException(1102, toolNumberRequest.getSite());
    }

    @PostMapping("/retrieveToolNumberByToolGroup")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> retrieveByToolGroup(@RequestBody ToolNumberRequest toolNumberRequest) {
        List<ToolNumber> toolNumberResponse= new ArrayList<>();
            try {
                toolNumberResponse= toolNumberService.retrieveAllByToolGroup(toolNumberRequest.getSite(),toolNumberRequest.getToolGroup());
                return ResponseEntity.ok(toolNumberResponse);
            }catch(ToolNumberException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }


}
