package com.rits.storagelocationservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.containermaintenanceservice.dto.AuditLogRequest;
import com.rits.kafkaservice.ProducerEvent;
import com.rits.storagelocationservice.dto.*;
import com.rits.storagelocationservice.exception.StorageLocationException;
import com.rits.storagelocationservice.model.MessageModel;
import com.rits.storagelocationservice.model.StorageLocation;
import com.rits.storagelocationservice.service.StorageLocationService;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.core.Application;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/storagelocation-service")
public class StorageLocationController {

    private final StorageLocationService storageLocationService;
    private final ApplicationEventPublisher eventPublisher;
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createStorageLocation(@RequestBody StorageLocationRequest storageLocationRequest){
//        MessageModel validationResponse = storageLocationService.validation( payload);
//        if (validationResponse.getMessage_details().getMsg_type().equals("E")) {
//            return ResponseEntity.badRequest().body(validationResponse.getMessage_details().getMsg());
//        }
//        StorageLocationRequest storageLocationRequest = new ObjectMapper().convertValue(payload, StorageLocationRequest.class);
        StorageLocation createResponse=new StorageLocation();
        if(storageLocationRequest.getSite()!=null && !storageLocationRequest.getSite().isEmpty()){
            try {
                createResponse= storageLocationService.createStorageLocation(storageLocationRequest);

                AuditLogRequest activityLog = AuditLogRequest.builder()
                        .site(storageLocationRequest.getSite())
                        .change_stamp("Create")
                        .action_code("STORAGELOCATION-CREATE")
                        .action_detail("StorageLocation Created "+storageLocationRequest.getStorageLocation())
                        .action_detail_handle("ActionDetailBO:"+storageLocationRequest.getSite()+","+"STORAGELOCATION-CREATE"+","+storageLocationRequest.getUserId()+":"+"com.rits.storagelocationservice.service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(storageLocationRequest.getUserId())
                        .txnId("STORAGELOCATION-CREATE"+String.valueOf(LocalDateTime.now())+storageLocationRequest.getUserId())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("Create")
                        .build();
                eventPublisher.publishEvent(new ProducerEvent(activityLog));

                return ResponseEntity.ok(createResponse);
            }catch(StorageLocationException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new StorageLocationException(1902, storageLocationRequest.getSite());
    }

    @PostMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<StorageLocation> updateStorageLocation(@RequestBody StorageLocationRequest storageLocationRequest) {
        StorageLocation updateResponse=new StorageLocation();
        if(storageLocationRequest.getSite()!=null && !storageLocationRequest.getSite().isEmpty()){
            try {
                updateResponse= storageLocationService.updateStorageLocation(storageLocationRequest);

                AuditLogRequest activityLog = AuditLogRequest.builder()
                        .site(storageLocationRequest.getSite())
                        .action_code("STORAGELOCATION-UPDATE")
                        .action_detail("StorageLocation Updated "+storageLocationRequest.getStorageLocation())
                        .action_detail_handle("ActionDetailBO:"+storageLocationRequest.getSite()+","+"STORAGELOCATION-UPDATE"+","+storageLocationRequest.getUserId()+":"+"com.rits.storagelocationservice.service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(storageLocationRequest.getUserId())
                        .txnId("STORAGELOCATION-UPDATE"+String.valueOf(LocalDateTime.now())+storageLocationRequest.getUserId())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("Update")
                        .build();
                eventPublisher.publishEvent(new ProducerEvent(activityLog));

                return ResponseEntity.ok(updateResponse);
            }catch(StorageLocationException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new StorageLocationException(1902, storageLocationRequest.getSite());
    }



    //    {
//        "site":"RITS",
//            "storageLocation":"sl1"
//    }

    @PostMapping("/retrieve")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<StorageLocation> retrieveStorageLocation(@RequestBody StorageLocationRequest storageLocationRequest) {
        StorageLocation storageLocationResponse=new StorageLocation();
        if(storageLocationRequest.getSite()!=null && !storageLocationRequest.getSite().isEmpty()){
            try {
                storageLocationResponse= storageLocationService.retrieveStorageLocation(storageLocationRequest);
                return ResponseEntity.ok(storageLocationResponse);
            }catch(StorageLocationException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new StorageLocationException(1902, storageLocationRequest.getSite());
    }



    //    {
//        "site":"RITS"
//    }
    @PostMapping("/retrieveTop50")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<StorageLocationResponseList> getStorageLocationListByCreationDate(@RequestBody StorageLocationRequest storageLocationRequest) {
        StorageLocationResponseList top50Response=new StorageLocationResponseList();
        if(storageLocationRequest.getSite()!=null && !storageLocationRequest.getSite().isEmpty()) {
            try {
                top50Response= storageLocationService.getStorageLocationListByCreationDate(storageLocationRequest);
                return ResponseEntity.ok(top50Response);
            }catch(StorageLocationException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new StorageLocationException(1902, storageLocationRequest.getSite());
    }



    //    {
//        "site":"RITS",
//            "storageLocation":"s"  //any letter containing
//    }
    @PostMapping("/retrieveAll")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<StorageLocationResponseList> getStorageLocationList(@RequestBody StorageLocationRequest storageLocationRequest) {
        StorageLocationResponseList storageLocationListResponse=new StorageLocationResponseList();
        if(storageLocationRequest.getSite()!=null && !storageLocationRequest.getSite().isEmpty()) {
            try {
                storageLocationListResponse= storageLocationService.getStorageLocationList(storageLocationRequest);
                return ResponseEntity.ok(storageLocationListResponse);
            }catch(StorageLocationException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new StorageLocationException(1902, storageLocationRequest.getSite());
    }


    //    {
//        "site":"RITS",
//            "storageLocation":"sl1"
//    }
    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Response> deleteStorageLocation(@RequestBody StorageLocationRequest storageLocationRequest){
        Response deleteResponse=new Response();
        if(storageLocationRequest.getSite()!=null && !storageLocationRequest.getSite().isEmpty()) {
            try {
                deleteResponse= storageLocationService.deleteStorageLocation(storageLocationRequest);
                AuditLogRequest activityLog = AuditLogRequest.builder()
                        .site(storageLocationRequest.getSite())
                        .action_code("STORAGELOCATION-DELETE")
                        .action_detail("StorageLocation Deleted "+storageLocationRequest.getStorageLocation())
                        .action_detail_handle("ActionDetailBO:"+storageLocationRequest.getSite()+","+"STORAGELOCATION-DELETE"+","+storageLocationRequest.getUserId()+":"+"com.rits.storagelocationservice.service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(storageLocationRequest.getUserId())
                        .txnId("STORAGELOCATION-DELETE"+String.valueOf(LocalDateTime.now())+storageLocationRequest.getUserId())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("Delete")
                        .build();
                eventPublisher.publishEvent(new ProducerEvent(activityLog));

                return  ResponseEntity.ok(deleteResponse);
            }catch(StorageLocationException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new StorageLocationException(1902, storageLocationRequest.getSite());
    }



    //    {
//        "site":"RITS",
//            "storageLocation":"sl1"
//    }
    @PostMapping("/isExist")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> isStorageLocationExist(@RequestBody StorageLocationRequest storageLocationRequest){
        Boolean isExistResponse;
        if(storageLocationRequest.getSite()!=null && !storageLocationRequest.getSite().isEmpty()) {
            try {
                isExistResponse= storageLocationService.isStorageLocationExist(storageLocationRequest);
                return  ResponseEntity.ok(isExistResponse);
            }  catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new StorageLocationException(1902, storageLocationRequest.getSite());
    }
    //    {
//        "site":"RITS",
//            "storageLocation":"sl1",
//            "workCenter":["workcenter1","workcenter2"]
//    }


    @PostMapping("/add")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<StorageLocation> associateWorkCenterToWorkCenterList(@RequestBody WorkCenterListRequest workCenterListRequest){
        StorageLocation associateResponse=new StorageLocation();
        if(workCenterListRequest.getSite()!=null && !workCenterListRequest.getSite().isEmpty()) {
            try {
                associateResponse= storageLocationService.associateWorkCenterToWorkCenterList(workCenterListRequest);
                return ResponseEntity.ok(associateResponse);
            }catch(StorageLocationException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new StorageLocationException(1902, workCenterListRequest.getSite());
    }

    //    {
//        "site":"RITS",
//            "storageLocation":"sl1",
//            "workCenter":["workcenter1","workcenter2"]
//    }

    @PostMapping("/remove")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<StorageLocation> removeWorkCenterToWorkCenterList(@RequestBody WorkCenterListRequest workCenterListRequest){
        StorageLocation removeResponse=new StorageLocation();
        if(workCenterListRequest.getSite()!=null && !workCenterListRequest.getSite().isEmpty()) {
            try {
                removeResponse =storageLocationService.removeWorkCenterToWorkCenterList(workCenterListRequest);
                return ResponseEntity.ok(removeResponse);
            } catch(StorageLocationException e){
                throw e;
            }catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new StorageLocationException(1902, workCenterListRequest.getSite());
    }

    @PostMapping("/availableWorkCenters")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<AvailableWorkCenters> availableWorkCenters(@RequestBody StorageLocationRequest storageLocationRequest){
        AvailableWorkCenters availableResponse=new AvailableWorkCenters();
        if(storageLocationRequest.getSite()!=null && !storageLocationRequest.getSite().isEmpty()) {
            try {
                availableResponse =storageLocationService.getAvailableWorkCenters(storageLocationRequest);
                return ResponseEntity.ok(availableResponse);
            } catch(StorageLocationException e){
                throw e;
            }catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new StorageLocationException(1902, storageLocationRequest.getSite());
    }

}
