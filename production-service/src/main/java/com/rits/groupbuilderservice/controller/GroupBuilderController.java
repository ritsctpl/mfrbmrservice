
package com.rits.groupbuilderservice.controller;


import com.rits.groupbuilderservice.dto.GroupBuilderRequest;
import com.rits.groupbuilderservice.dto.PreviewGroupRequest;
import com.rits.groupbuilderservice.exception.GroupBuilderException;
import com.rits.groupbuilderservice.model.GroupBuilder;
import com.rits.groupbuilderservice.model.MessageModel;
import com.rits.groupbuilderservice.service.GroupBuilderService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.bson.Document;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/groupbuilder-service")
public class GroupBuilderController {
    private final GroupBuilderService groupBuilderService;
    private final ApplicationEventPublisher eventPublisher;
    @PostMapping("createGroup")
    public MessageModel create(@RequestBody GroupBuilderRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                MessageModel response =groupBuilderService.create(request);
//                AuditLogRequest auditlog = AuditLogRequest.builder()
//                        .site(request.getSite())
//                        .action_code("GROUP-BUILDER-CREATED"+request.getGroupLabel())
//                        .action_detail("Group Builder Created "+request.getGroupLabel())
//                        .action_detail_handle("ActionDetailBO:"+request.getSite()+","+"GROUP-BUILDER-CREATED"+","+request.getUserId()+":"+"com.rits.groupbuilderservice.controller")
//                        .activity("From Service")
//                        .date_time(String.valueOf(LocalDateTime.now()))
//                        .userId(request.getUserId())
//                        .txnId("GROUP-BUILDER-CREATED"+String.valueOf(LocalDateTime.now())+request.getUserId())
//                        .created_date_time(String.valueOf(LocalDateTime.now()))
//                        .category("Create")
//                        .topic("audit-log")
//                        .build();
//                eventPublisher.publishEvent(new ProducerEvent(auditlog));
                return response;
            } catch (GroupBuilderException groupBuilderException) {
                throw groupBuilderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new GroupBuilderException(2001);
    }

    @PostMapping("updateGroup")
    public MessageModel update(@RequestBody GroupBuilderRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                MessageModel response = groupBuilderService.update(request);
//                AuditLogRequest auditlog = AuditLogRequest.builder()
//                        .site(request.getSite())
//                        .action_code("GROUP-BUILDER-UPDATED"+request.getGroupLabel())
//                        .action_detail("Group Builder Updated")
//                        .action_detail_handle("ActionDetailBO:"+request.getSite()+","+"GROUP-BUILDER-UPDATED"+","+request.getUserId()+":"+"com.rits.groupbuilderservice.controller")
//                        .activity("From Service")
//                        .date_time(String.valueOf(LocalDateTime.now()))
//                        .userId(request.getUserId())
//                        .txnId("GROUP-BUILDER-UPDATED"+String.valueOf(LocalDateTime.now())+request.getUserId())
//                        .created_date_time(String.valueOf(LocalDateTime.now()))
//                        .category("Update")
//                        .topic("audit-log")
//                        .build();
//                eventPublisher.publishEvent(new ProducerEvent(auditlog));
                return response;
            } catch (GroupBuilderException groupBuilderException) {
                throw groupBuilderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new GroupBuilderException(2001);
    }

    @PostMapping("deleteGroup")
    public MessageModel delete(@RequestBody GroupBuilderRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                MessageModel response = groupBuilderService.delete(request);
//                AuditLogRequest auditlog = AuditLogRequest.builder()
//
//                        .site(request.getSite())
//                        .action_code("GROUP-BUILDER-DELETED")
//                        .action_detail("Group Builder Deleted "+request.getGroupLabel())
//                        .action_detail_handle("ActionDetailBO:"+request.getGroupLabel()+","+"GROUP-BUILDER-DELETED"+","+request.getUserId()+":"+"com.rits.groupbuilderservice.controller")
//                        .activity("From Service")
//                        .date_time(String.valueOf(LocalDateTime.now()))
//                        .userId(request.getUserId())
//                        .txnId("GROUP-BUILDER-DELETED"+String.valueOf(LocalDateTime.now())+request.getUserId())
//                        .created_date_time(String.valueOf(LocalDateTime.now()))
//                        .category("Delete")
//                        .topic("audit-log")
//                        .build();
//                eventPublisher.publishEvent(new ProducerEvent(auditlog));
                return response;
            } catch (GroupBuilderException groupBuilderException) {
                throw groupBuilderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new GroupBuilderException(2001);
    }
    @PostMapping("getGroupById")
    public GroupBuilder retrieve(@RequestBody GroupBuilderRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return groupBuilderService.retrieve(request);
            } catch (GroupBuilderException groupBuilderException) {
                throw groupBuilderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new GroupBuilderException(2001);
    }
    @PostMapping("getAllGroup")
    public List<GroupBuilder> retrieveAll(@RequestBody GroupBuilderRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return groupBuilderService.retrieveAll(request.getSite());
            } catch (GroupBuilderException groupBuilderException) {
                throw groupBuilderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new GroupBuilderException(2001);
    }

    @PostMapping("getTop50")
    public List<GroupBuilder> retrieveTop50(@RequestBody GroupBuilderRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return groupBuilderService.retrieveTop50(request.getSite());
            } catch (GroupBuilderException groupBuilderException) {
                throw groupBuilderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new GroupBuilderException(2001);
    }

    @PostMapping("/isExist")
    public boolean isGroupBuilderExist(@RequestBody GroupBuilderRequest request) throws Exception {

        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try{
                return groupBuilderService.isGroupBuilderExist(request.getSite(), request.getGroupLabel());
            }catch (GroupBuilderException groupBuilderException) {
                throw groupBuilderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new GroupBuilderException(2001);
    }

    @PostMapping("/previewGroup")

    public List<Document> previewGroupBuilder(@RequestBody PreviewGroupRequest request) throws Exception {

        try{

            return groupBuilderService.previewGroups(request);

        }catch (GroupBuilderException groupBuilderException) {

            throw groupBuilderException;

        } catch (Exception e) {

            throw new RuntimeException(e);

        }

    }

}
