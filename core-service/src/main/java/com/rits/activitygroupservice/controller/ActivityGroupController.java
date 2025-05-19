package com.rits.activitygroupservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.activitygroupservice.dto.*;

import com.rits.activitygroupservice.exception.ActivityGroupException;
import com.rits.activitygroupservice.model.ActivityGroup;
import com.rits.activitygroupservice.model.ActivityGroupMessageModel;
import com.rits.activitygroupservice.service.ActivityGroupService;
import com.rits.auditlogservice.dto.AuditLogRequest;
import com.rits.auditlogservice.service.ProducerEvent;
import com.rits.usergroupservice.dto.UserGroupRequest;
import com.rits.usergroupservice.exception.UserGroupException;
import com.rits.usergroupservice.model.Activity;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/activitygroup-service")
public class ActivityGroupController {
    private final ActivityGroupService activityGroupService;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createActivityGroup(@RequestBody ActivityGroupRequest activityGroupRequest) throws JsonProcessingException {
        ActivityGroupMessageModel createResponse;
//        if(activityGroupRequest.getSite()!=null && !activityGroupRequest.getSite().isEmpty()){
            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(activityGroupRequest.getCurrentSite()).hookPoint("PRE").activity("activitygroup-service").hookableMethod("create").request(objectMapper.writeValueAsString(activityGroupRequest)).build();
            String preExtensionResponse = activityGroupService.callExtension(preExtension);
            ActivityGroupRequest preExtensionActivityGroup = objectMapper.readValue(preExtensionResponse, ActivityGroupRequest.class);
            try {
                createResponse = activityGroupService.createActivityGroup(preExtensionActivityGroup);
                AuditLogRequest auditlog = AuditLogRequest.builder()
                        .site(activityGroupRequest.getCurrentSite())
                        .action_code("ACTIVITY-GROUP-CREATED "+activityGroupRequest.getActivityGroupDescription())
                        .action_detail("Activity Group Created "+activityGroupRequest.getActivityGroupDescription())
                        .action_detail_handle("ActionDetailBO:"+activityGroupRequest.getCurrentSite()+","+"ACTIVITY-GROUP-CREATED"+","+activityGroupRequest.getUserId()+":"+"com.rits.activitygroupservice.controller")
                        .activity("From Service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(activityGroupRequest.getUserId())
                        .txnId("ACTIVITY-GROUP-CREATED"+String.valueOf(LocalDateTime.now())+activityGroupRequest.getUserId())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("Create")
                        .topic("audit-log")
                        .build();
                eventPublisher.publishEvent(new ProducerEvent(auditlog));
                Extension postExtension = Extension.builder().site(activityGroupRequest.getCurrentSite()).hookPoint("POST").activity("activitygroup-service").hookableMethod("create").request(objectMapper.writeValueAsString(createResponse.getResponse())).build();
                String postExtensionResponse = activityGroupService.callExtension(postExtension);
                ActivityGroup postExtensionDataType = objectMapper.readValue(postExtensionResponse, ActivityGroup.class);
                return ResponseEntity.ok(ActivityGroupMessageModel.builder().message_details(createResponse.getMessage_details()).response(postExtensionDataType).build());
            }catch(ActivityGroupException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
//        }
//        throw new ActivityGroupException(1402, activityGroupRequest.getSite());
    }

    @PostMapping("/retrieveTop50")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ActivityGroupResponseList> getActivityGroupListByCreationDate(@RequestBody ActivityGroupRequest activityGroupRequest) {
        ActivityGroupResponseList top50Response;
//        if(activityGroupRequest.getSite()!=null && !activityGroupRequest.getSite().isEmpty()) {
            try {
                top50Response= activityGroupService.getActivityGroupListByCreationDate(activityGroupRequest);
                return ResponseEntity.ok(top50Response);
            }catch(ActivityGroupException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
//        }
//        throw new ActivityGroupException(1402, activityGroupRequest.getSite());
    }

    @PostMapping("/retrieveAll")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ActivityGroupResponseList> getActivityGroupList(@RequestBody ActivityGroupRequest activityGroupRequest) {
        ActivityGroupResponseList activityGroupListResponse;
//        if(activityGroupRequest.getSite()!=null && !activityGroupRequest.getSite().isEmpty()) {
            try {
                activityGroupListResponse= activityGroupService.getActivityGroupList(activityGroupRequest);
                return ResponseEntity.ok(activityGroupListResponse);
            }catch(ActivityGroupException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
//        }
//        throw new ActivityGroupException(1402, activityGroupRequest.getSite());
    }

    @PostMapping("/retrieve")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ActivityGroup> retrieveActivityGroup(@RequestBody ActivityGroupRequest activityGroupRequest) throws JsonProcessingException {
        ActivityGroup retrieveActivityGroupResponse;
//        if(activityGroupRequest.getSite()!=null && !activityGroupRequest.getSite().isEmpty()) {
            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(activityGroupRequest.getCurrentSite()).hookPoint("PRE").activity("activitygroup-service").hookableMethod("retrieve").request(objectMapper.writeValueAsString(activityGroupRequest)).build();
            String preExtensionResponse = activityGroupService.callExtension(preExtension);
            ActivityGroupRequest preExtensionActivityGroupRequest = objectMapper.readValue(preExtensionResponse, ActivityGroupRequest.class);

            try {
                retrieveActivityGroupResponse = activityGroupService.retrieveActivityGroup(preExtensionActivityGroupRequest);
                Extension postExtension = Extension.builder().site(activityGroupRequest.getCurrentSite()).hookPoint("POST").activity("activitygroup-service").hookableMethod("retrieve").request(objectMapper.writeValueAsString(retrieveActivityGroupResponse)).build();
                String postExtensionResponse = activityGroupService.callExtension(postExtension);
                ActivityGroup postExtensionActivityGroup = objectMapper.readValue(postExtensionResponse, ActivityGroup.class);
                return ResponseEntity.ok(postExtensionActivityGroup);
            }catch(ActivityGroupException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
//        }
//        throw new ActivityGroupException(1402, activityGroupRequest.getSite());
    }

    @PostMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> updateActivityGroup(@RequestBody ActivityGroupRequest activityGroupRequest) throws JsonProcessingException {
        ActivityGroupMessageModel updateResponse;
//        if(activityGroupRequest.getSite()!=null && !activityGroupRequest.getSite().isEmpty()) {
            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(activityGroupRequest.getCurrentSite()).hookPoint("PRE").activity("activitygroup-service").hookableMethod("update").request(objectMapper.writeValueAsString(activityGroupRequest)).build();
            String preExtensionResponse = activityGroupService.callExtension(preExtension);
            ActivityGroupRequest preExtensionActivityGroup = objectMapper.readValue(preExtensionResponse, ActivityGroupRequest.class);

            try {
                updateResponse = activityGroupService.updateActivityGroup(preExtensionActivityGroup);

                AuditLogRequest auditlog = AuditLogRequest.builder()
                        .site(activityGroupRequest.getCurrentSite())
                        .action_code("ACTVITY-GROUP-UPDATED "+activityGroupRequest.getActivityGroupDescription())
                        .action_detail("Activity Group Updated")
                        .action_detail_handle("ActionDetailBO:"+activityGroupRequest.getCurrentSite()+","+"ACTVITY-GROUP-UPDATED"+","+activityGroupRequest.getUserId()+":"+"com.rits.activitygroupservice.controller")
                        .activity("From Service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(activityGroupRequest.getUserId())
                        .txnId("ACTVITY-GROUP-UPDATED"+String.valueOf(LocalDateTime.now())+activityGroupRequest.getUserId())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("Update")
                        .topic("audit-log")
                        .build();
                eventPublisher.publishEvent(new ProducerEvent(auditlog));

                Extension postExtension = Extension.builder().site(activityGroupRequest.getCurrentSite()).hookPoint("POST").activity("activitygroup-service").hookableMethod("update").request(objectMapper.writeValueAsString(updateResponse.getResponse())).build();
                String postExtensionResponse = activityGroupService.callExtension(postExtension);
                ActivityGroup postExtensionDataType = objectMapper.readValue(postExtensionResponse, ActivityGroup.class);
                return ResponseEntity.ok(ActivityGroupMessageModel.builder().message_details(updateResponse.getMessage_details()).response(postExtensionDataType).build());
            }catch(ActivityGroupException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
//        }
//        throw new ActivityGroupException(1402, activityGroupRequest.getSite());

    }

    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> deleteActivityGroup(@RequestBody ActivityGroupRequest activityGroupRequest) throws JsonProcessingException {
        ActivityGroupMessageModel deleteResponse;
//        if(activityGroupRequest.getSite()!=null && !activityGroupRequest.getSite().isEmpty()) {
            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(activityGroupRequest.getCurrentSite()).hookPoint("PRE").activity("activitygroup-service").hookableMethod("delete").request(objectMapper.writeValueAsString(activityGroupRequest)).build();
            String preExtensionResponse = activityGroupService.callExtension(preExtension);
            ActivityGroupRequest preExtensionActivityGroupRequest = objectMapper.readValue(preExtensionResponse, ActivityGroupRequest.class);
            try {
                deleteResponse = activityGroupService.deleteActivityGroup(preExtensionActivityGroupRequest);
                AuditLogRequest auditlog = AuditLogRequest.builder()
                        .site(activityGroupRequest.getCurrentSite())
                        .action_code("ACTIVITY-GROUP-DELETED")
                        .action_detail("Activity Group Deleted "+activityGroupRequest.getActivityGroupDescription())
                        .action_detail_handle("ActionDetailBO:"+activityGroupRequest.getCurrentSite()+","+"ACTIVITY-GROUP-DELETED"+","+activityGroupRequest.getUserId()+":"+"com.rits.activitygroupservice.controller")
                        .activity("From Service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(activityGroupRequest.getUserId())
                        .txnId("ACTIVITY-GROUP-DELETED"+String.valueOf(LocalDateTime.now())+activityGroupRequest.getUserId())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("Delete")
                        .topic("audit-log")
                        .build();
                eventPublisher.publishEvent(new ProducerEvent(auditlog));

                return ResponseEntity.ok(deleteResponse);
            }catch(ActivityGroupException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
//        }
//        throw new ActivityGroupException(1402, activityGroupRequest.getSite());
    }

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> associateActivityMemberToActivityGroupMemberList(@RequestBody AddRemoveActivityRequest activityGroupMemberListRequest){
        Boolean associateResponse;
            try {
                associateResponse= activityGroupService.associateActivityMemberToActivityGroupMemberList(activityGroupMemberListRequest);
                return ResponseEntity.ok(associateResponse);
            }catch(ActivityGroupException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

    @PostMapping("/remove")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> removeActivityMemberFromActivityGroupMemberList(@RequestBody AddRemoveActivityRequest activityGroupMemberListRequest){
        Boolean removeResponse;
            try {
                removeResponse =activityGroupService.removeActivityMemberFromActivityGroupMemberList(activityGroupMemberListRequest);
                return ResponseEntity.ok(removeResponse);
            } catch(ActivityGroupException e){
                throw e;
            }catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

    @PostMapping("/retrieveActivityGroupMemberList")
    public ResponseEntity<ActivityGroupMemberResponseList> getActivityGroupMemberList(@RequestBody ActivityGroupMemberListRequest activityGroupMemberListRequest){
        ActivityGroupMemberResponseList activityGroupMemberListResponse;
        if(activityGroupMemberListRequest.getSite()!=null && !activityGroupMemberListRequest.getSite().isEmpty()) {
            try {
                activityGroupMemberListResponse= activityGroupService.getActivityGroupMemberList(activityGroupMemberListRequest);
                return ResponseEntity.ok(activityGroupMemberListResponse);
            }catch(ActivityGroupException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ActivityGroupException(1402, activityGroupMemberListRequest.getSite());
    }

    @PostMapping("/retrieveAvailableActivities")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<AvailableActivityList> getAvailableActivities(@RequestBody ActivityGroupRequest activityGroupRequest) {
        AvailableActivityList availableActivitiesResponse;
//        if(activityGroupRequest.getSite()!=null && !activityGroupRequest.getSite().isEmpty()) {
            try {
                availableActivitiesResponse= activityGroupService.getAvailableActivities(activityGroupRequest);
                return ResponseEntity.ok(availableActivitiesResponse);
            }catch(ActivityGroupException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
//        }
//        throw new ActivityGroupException(1402, activityGroupRequest.getSite());
    }

    @PostMapping("/isExist")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> isActivityGroupExist(@RequestBody ActivityGroupRequest activityGroupRequest){
        Boolean isExistResponse;
//        if(activityGroupRequest.getSite()!=null && !activityGroupRequest.getSite().isEmpty()) {
            try {
                isExistResponse= activityGroupService.isActivityGroupExist(activityGroupRequest);
                return ResponseEntity.ok(isExistResponse);
            }catch(ActivityGroupException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
//        }
//        throw new ActivityGroupException(1402, activityGroupRequest.getSite());
    }

    @PostMapping("/retrieveBySite")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<?>> retrieveAllBySite(@RequestBody ActivityGroupRequest activityGroupRequest){
        List<ActivityGroup> retrieveAllBySiteResponse;
//        if(activityGroupRequest.getSite()!=null && !activityGroupRequest.getSite().isEmpty()) {
            try {
                retrieveAllBySiteResponse= activityGroupService.retrieveAllBySite();
                return ResponseEntity.ok(retrieveAllBySiteResponse);
            }catch(ActivityGroupException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
//        }
//        throw new ActivityGroupException(1402, activityGroupRequest.getSite());
    }

    @PostMapping("getActivities")
    public ResponseEntity<List<ActivityGroupResponse>> getActivities(@RequestBody UserGroupRequest userGroupRequest) {

        if (StringUtils.isEmpty(userGroupRequest.getSite()))
            throw new UserGroupException(1);

        try {
            return ResponseEntity.ok(activityGroupService.getActivities(userGroupRequest));
        } catch (UserGroupException userGroupException) {
            throw userGroupException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
