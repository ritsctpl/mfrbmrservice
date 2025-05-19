package com.rits.activityservice.controller;

import com.rits.activityservice.dto.*;
import com.rits.activityservice.exception.ActivityException;
import com.rits.activityservice.model.Activity;
import com.rits.activityservice.model.ActivityHook;
import com.rits.activityservice.model.ActivityMessageModel;
import com.rits.activityservice.service.ActivityService;
import com.rits.auditlogservice.dto.AuditLogRequest;
import com.rits.auditlogservice.service.ProducerEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/activity-service")
public class ActivityController {
    private final ActivityService activityService;
    private final ApplicationEventPublisher eventPublisher;
    private final ApplicationContext context;
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createActivity(@RequestBody ActivityRequest activityRequest){
        ActivityMessageModel createResponse;
        //if(activityRequest.getSite()!=null && !activityRequest.getSite().isEmpty()){
            try {
                createResponse= activityService.createActivity(activityRequest);

                AuditLogRequest auditlog = AuditLogRequest.builder()
                        .site(activityRequest.getCurrentSite())
                        .action_code("ACTIVITY-CREATED "+activityRequest.getDescription())
                        .action_detail("Activity Group Created "+activityRequest.getDescription())
                        .action_detail_handle("ActionDetailBO:"+activityRequest.getCurrentSite()+","+"ACTIVITY-CREATED"+","+activityRequest.getUserId()+":"+"com.rits.activityservice.controller")
                        .activity("From Service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(activityRequest.getUserId())
                        .txnId("ACTIVITY-CREATED"+String.valueOf(LocalDateTime.now())+activityRequest.getUserId())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("Create")
                        .topic("audit-log")
                        .build();
                eventPublisher.publishEvent(new ProducerEvent(auditlog));

                return ResponseEntity.ok(createResponse);
            }catch (ActivityException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        //}
        //throw new ActivityException(1302,activityRequest.getSite());
    }

    @PostMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ActivityMessageModel> updateActivity(@RequestBody ActivityRequest activityRequest) {
        ActivityMessageModel updateResponse;
        //if(activityRequest.getSite()!=null && !activityRequest.getSite().isEmpty()){
            try {
                updateResponse= activityService.updateActivity(activityRequest);

                AuditLogRequest auditlog = AuditLogRequest.builder()
                        .site(activityRequest.getCurrentSite())
                        .action_code("ACTVITY-UPDATED "+activityRequest.getDescription())
                        .action_detail("Activity Updated")
                        .action_detail_handle("ActionDetailBO:"+activityRequest.getCurrentSite()+","+"ACTVITY-UPDATED"+","+activityRequest.getUserId()+":"+"com.rits.activityservice.controller")
                        .activity("From Service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(activityRequest.getUserId())
                        .txnId("ACTVITY-UPDATED"+String.valueOf(LocalDateTime.now())+activityRequest.getUserId())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("Update")
                        .topic("audit-log")
                        .build();
                eventPublisher.publishEvent(new ProducerEvent(auditlog));

                return ResponseEntity.ok(updateResponse);
            }catch (ActivityException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        //}
        //throw new ActivityException(1302,activityRequest.getSite());
    }


    @PostMapping("/retrieveTop50")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ActivityListResponseList> getActivityListByCreationDate(@RequestBody ActivityRequest activityRequest) {
        ActivityListResponseList top50Response;
        //if(activityRequest.getSite()!=null && !activityRequest.getSite().isEmpty()){
            try {
                top50Response= activityService.getActivityListByCreationDate(activityRequest);
                return ResponseEntity.ok(top50Response);
            }catch (ActivityException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        //}
        //throw new ActivityException(1302,activityRequest.getSite());
    }

    @PostMapping("/retrieveByActivityId")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ActivityListResponseList> getActivityList(@RequestBody ActivityRequest activityRequest) {
        ActivityListResponseList activityListResponse;
        //if(activityRequest.getSite()!=null && !activityRequest.getSite().isEmpty()){
            try {
                activityListResponse= activityService.getActivityList(activityRequest);
                return ResponseEntity.ok(activityListResponse);
            }catch (ActivityException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        //}
        //throw new ActivityException(1302,activityRequest.getSite());
    }

    @PostMapping("retrieveActivityHookList")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ActivityHookResponseList> getActivityHookList(@RequestBody ActivityHookListRequest activityHookListRequest){
        ActivityHookResponseList activityHookListResponse;
        //if(activityHookListRequest.getSite()!=null && !activityHookListRequest.getSite().isEmpty()){
            try {
                activityHookListResponse= activityService.getActivityHookList(activityHookListRequest);
                return ResponseEntity.ok(activityHookListResponse);
            }catch (ActivityException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        //}
        //throw new ActivityException(1302,activityHookListRequest.getSite());
    }

    @PostMapping("/retrieve")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Activity> retrieveActivity(@RequestBody ActivityRequest activityRequest) {
        Activity retrieveActivityResponse;
        //if(activityRequest.getSite()!=null && !activityRequest.getSite().isEmpty()){
            try {
                retrieveActivityResponse= activityService.retrieveActivity(activityRequest);
                return ResponseEntity.ok(retrieveActivityResponse);
            }catch (ActivityException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        //}
        //throw new ActivityException(1302,activityRequest.getSite());
    }

    @PostMapping("/addActivityGroupName")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> addActivityGroupName(@RequestBody ActivityRequest activityRequest) {
        Boolean addGroupNameResponse;
        //if (activityRequest.getSite() != null && !activityRequest.getSite().isEmpty()) {
            try {
                addGroupNameResponse = activityService.addActivityGroupName(activityRequest);
                return ResponseEntity.ok(addGroupNameResponse);
            } catch (ActivityException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        //}
        //throw new ActivityException(1302, activityRequest.getSite());
    }

    @PostMapping("/retrieveAll")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ActivityResponse>> getAllActivities(@RequestBody ListOfActivityRequest listOfActivityRequest) {
        List<ActivityResponse> allActivitiesResponse;
        //if (listOfActivityRequest.getSite() != null && !listOfActivityRequest.getSite().isEmpty()) {
            try {
                allActivitiesResponse= activityService.getAllActivities(listOfActivityRequest);
                return  ResponseEntity.ok(allActivitiesResponse);
            }catch (ActivityException e){
                throw e;
            }  catch (Exception e) {
                throw new RuntimeException(e);
            }
        //}
        //throw new ActivityException(1302,listOfActivityRequest.getSite());
    }

    @PostMapping("/retrieveServiceActivity")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ActivityResponse>> getServiceActivities(@RequestBody ListOfActivityRequest listOfActivityRequest) {
        List<ActivityResponse> allActivitiesResponse;
        //if (listOfActivityRequest.getSite() != null && !listOfActivityRequest.getSite().isEmpty()) {
            try {
                allActivitiesResponse= activityService.getServiceActivities(listOfActivityRequest);
                return  ResponseEntity.ok(allActivitiesResponse);
            }catch (ActivityException e){
                throw e;
            }  catch (Exception e) {
                throw new RuntimeException(e);
            }
        //}
        //throw new ActivityException(1302,listOfActivityRequest.getSite());
    }
    @PostMapping("/retrieveHookActivity")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ActivityHookListResponse>> retrieveHookActivity(@RequestBody ListOfActivityRequest listOfActivityRequest) {
        List<ActivityHookListResponse> allActivitiesResponse;
        //if (listOfActivityRequest.getSite() != null && !listOfActivityRequest.getSite().isEmpty()) {
            try {
                allActivitiesResponse= activityService.getHookActivities(listOfActivityRequest);
                return  ResponseEntity.ok(allActivitiesResponse);
            }catch (ActivityException e){
                throw e;
            }  catch (Exception e) {
                throw new RuntimeException(e);
            }
        //}
        //throw new ActivityException(1302,listOfActivityRequest.getSite());
    }
    @PostMapping("/retrieveByActivityGroup")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ActivityResponse>> getListOfActivityByActivityGroup(@RequestBody ListOfActivityRequest listOfActivityRequest) {
        List<ActivityResponse> activitiesByActivityGroupResponse;
        //if (listOfActivityRequest.getSite() != null && !listOfActivityRequest.getSite().isEmpty()) {
            List<String> activityGroup = listOfActivityRequest.getActivityGroup();
            try {
                activitiesByActivityGroupResponse= activityService.getListOfActivityByActivityGroup(activityGroup);
                return ResponseEntity.ok(activitiesByActivityGroupResponse);
            } catch (ActivityException e){
               throw e;
            }  catch (Exception e) {
                throw new RuntimeException(e);
            }
        //}
        //throw new ActivityException(1302,listOfActivityRequest.getSite());
    }

    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Response> deleteActivity(@RequestBody ActivityRequest activityRequest){
        Response deleteResponse;
        //if(activityRequest.getSite()!=null && !activityRequest.getSite().isEmpty()){
                try {
                    deleteResponse= activityService.deleteActivity(activityRequest);
                    AuditLogRequest auditlog = AuditLogRequest.builder()

                            .site(activityRequest.getCurrentSite())
                            .action_code("ACTIVITY-DELETED")
                            .action_detail("Activity Deleted "+activityRequest.getDescription())
                            .action_detail_handle("ActionDetailBO:"+activityRequest.getCurrentSite()+","+"ACTIVITY-DELETED"+","+activityRequest.getUserId()+":"+"com.rits.activityservice.controller")
                            .activity("From Service")
                            .date_time(String.valueOf(LocalDateTime.now()))
                            .userId(activityRequest.getUserId())
                            .txnId("ACTIVITY-DELETED"+String.valueOf(LocalDateTime.now())+activityRequest.getUserId())
                            .created_date_time(String.valueOf(LocalDateTime.now()))
                            .category("Delete")
                            .topic("audit-log")
                            .build();
                    eventPublisher.publishEvent(new ProducerEvent(auditlog));

                    return ResponseEntity.ok(deleteResponse);
                } catch (ActivityException e){
                    throw e;
                }  catch (Exception e) {
                    throw new RuntimeException(e);
                }
        //}
        //throw new ActivityException(1302,activityRequest.getSite());
    }

    @PostMapping("/removeActivityGroupMember")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> removeActivityGroupMember(@RequestBody ListOfActivityRequest request) {
        Boolean removeActivityGroupResponse;
        //if (request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                removeActivityGroupResponse = activityService.removeActivityGroupMemberFromActivity(
                        request.getActivityGroup().get(0),
                        request.getActivityId()
                );

                return ResponseEntity.ok(removeActivityGroupResponse);
            } catch (ActivityException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        //}
        //throw new ActivityException(1302, request.getSite());
    }

    @PostMapping("/isExist")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> isActivityExist(@RequestBody ActivityRequest activityRequest){
        Boolean isExistResponse;
        //if(activityRequest.getSite()!=null && !activityRequest.getSite().isEmpty()) {
            try {
                isExistResponse= activityService.isActivityExist(activityRequest);
                return ResponseEntity.ok(isExistResponse);
            }catch (ActivityException e){
                throw e;
            }  catch (Exception e) {
                throw new RuntimeException(e);
            }
        //}
        //throw new ActivityException(1302,activityRequest.getSite());
    }
    @PostMapping("/getActivityUrl")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> getActivityUrl(@RequestBody ActivityRequest activityRequest){
        String getActivityUrl;
            try {
                getActivityUrl= activityService.getActivityUrl(activityRequest.getActivityId());
                return ResponseEntity.ok(getActivityUrl);
            }catch (ActivityException e){
                throw e;
            }  catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    @PostMapping("/shutdown")
    public void shutdown() {

            System.out.println("Shutting down...");
            SpringApplication.exit(context, () -> 1);

    }

    @PostMapping("/getByActivityId")
    @ResponseStatus(HttpStatus.OK)
    public Activity getByActivityId(@RequestBody ActivityRequest activityRequest){
        Activity activity;
        try {
            if(activityRequest.getActivityId() == null || activityRequest.getActivityId().isEmpty()){
                throw new ActivityException(2407);
            }
            activity= activityService.getByActivityId(activityRequest.getSite(), activityRequest.getActivityId());
            return activity;
        }catch (ActivityException e){
            throw e;
        }  catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
