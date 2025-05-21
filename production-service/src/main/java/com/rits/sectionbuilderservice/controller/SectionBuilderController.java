package com.rits.sectionbuilderservice.controller;


import com.rits.sectionbuilderservice.dto.PreviewResponse;
import com.rits.sectionbuilderservice.dto.SectionBuilderRequest;
import com.rits.sectionbuilderservice.exception.SectionBuilderException;
import com.rits.sectionbuilderservice.model.MessageModel;
import com.rits.sectionbuilderservice.model.SectionBuilder;
import com.rits.sectionbuilderservice.service.SectionBuilderService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/sectionbuilder-service")
public class SectionBuilderController {
    private final SectionBuilderService sectionBuilderService;
    private final ApplicationEventPublisher eventPublisher;
    @PostMapping("createSection")
    public MessageModel create(@RequestBody SectionBuilderRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                MessageModel response =sectionBuilderService.create(request);
//                AuditLogRequest auditlog = AuditLogRequest.builder()
//                        .site(request.getSite())
//                        .action_code("SECTION-BUILDER-CREATED "+request.getSectionLabel())
//                        .action_detail("Section Builder Created "+request.getSectionLabel())
//                        .action_detail_handle("ActionDetailBO:"+request.getSite()+","+"SECTION-BUILDER-CREATED"+","+request.getUserId()+":"+"com.rits.sectionbuilderservice.controller")
//                        .activity("From Service")
//                        .date_time(String.valueOf(LocalDateTime.now()))
//                        .userId(request.getUserId())
//                        .txnId("SECTION-BUILDER-CREATED"+String.valueOf(LocalDateTime.now())+request.getUserId())
//                        .created_date_time(String.valueOf(LocalDateTime.now()))
//                        .category("Create")
//                        .topic("audit-log")
//                        .build();
//                eventPublisher.publishEvent(new ProducerEvent(auditlog));
                return response;
            } catch (SectionBuilderException sectionBuilderException) {
                throw sectionBuilderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new SectionBuilderException(2001);
    }

    @PostMapping("updateSection")
    public MessageModel update(@RequestBody SectionBuilderRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                MessageModel response = sectionBuilderService.update(request);
//                AuditLogRequest auditlog = AuditLogRequest.builder()
//                        .site(request.getSite())
//                        .action_code("SECTION-BUILDER-UPDATED "+request.getSectionLabel())
//                        .action_detail("Section Builder Updated")
//                        .action_detail_handle("ActionDetailBO:"+request.getSite()+","+"SECTION-BUILDER-UPDATED"+","+request.getUserId()+":"+"com.rits.sectionbuilderservice.controller")
//                        .activity("From Service")
//                        .date_time(String.valueOf(LocalDateTime.now()))
//                        .userId(request.getUserId())
//                        .txnId("SECTION-BUILDER-UPDATED"+String.valueOf(LocalDateTime.now())+request.getUserId())
//                        .created_date_time(String.valueOf(LocalDateTime.now()))
//                        .category("Update")
//                        .topic("audit-log")
//                        .build();
//                eventPublisher.publishEvent(new ProducerEvent(auditlog));
                return response;
            } catch (SectionBuilderException sectionBuilderException) {
                throw sectionBuilderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new SectionBuilderException(2001);
    }
    //
    @PostMapping("deleteSection")
    public MessageModel delete(@RequestBody SectionBuilderRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                MessageModel response = sectionBuilderService.delete(request);
//                AuditLogRequest auditlog = AuditLogRequest.builder()
//
//                        .site(request.getSite())
//                        .action_code("SECTION-BUILDER-DELETED")
//                        .action_detail("Section Builder Deleted "+request.getSectionLabel())
//                        .action_detail_handle("ActionDetailBO:"+request.getSectionLabel()+","+"SECTION-BUILDER-DELETED"+","+request.getUserId()+":"+"com.rits.sectionbuilderservice.controller")
//                        .activity("From Service")
//                        .date_time(String.valueOf(LocalDateTime.now()))
//                        .userId(request.getUserId())
//                        .txnId("SECTION-BUILDER-DELETED"+String.valueOf(LocalDateTime.now())+request.getUserId())
//                        .created_date_time(String.valueOf(LocalDateTime.now()))
//                        .category("Delete")
//                        .topic("audit-log")
//                        .build();
//                eventPublisher.publishEvent(new ProducerEvent(auditlog));
                return response;
            } catch (SectionBuilderException sectionBuilderException) {
                throw sectionBuilderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new SectionBuilderException(2001);
    }
    //
    @PostMapping("getSectionById")
    public SectionBuilder retrieve(@RequestBody SectionBuilderRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return sectionBuilderService.retrieve(request);
            } catch (SectionBuilderException sectionBuilderException) {
                throw sectionBuilderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new SectionBuilderException(2001);
    }
    //
    @PostMapping("getAllSection")
    public List<SectionBuilder> retrieveAll(@RequestBody SectionBuilderRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return sectionBuilderService.retrieveAll(request.getSite(), request.getSectionLabel());
            } catch (SectionBuilderException sectionBuilderException) {
                throw sectionBuilderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new SectionBuilderException(2001);
    }

    @PostMapping("getTop50")
    public List<SectionBuilder> retrieveTop50(@RequestBody SectionBuilderRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return sectionBuilderService.retrieveTop50(request.getSite());
            } catch (SectionBuilderException sectionBuilderException) {
                throw sectionBuilderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new SectionBuilderException(2001);
    }

    @PostMapping("/isExist")
    public boolean isLineClearanceExist(@RequestBody SectionBuilderRequest request) throws Exception {

        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try{
                return sectionBuilderService.isSectionBuilderExist(request.getSite(), request.getSectionLabel());
            }catch (SectionBuilderException sectionBuilderException) {
                throw sectionBuilderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new SectionBuilderException(2001);
    }

    @PostMapping("/preview")
    public PreviewResponse preview(@RequestBody SectionBuilderRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return sectionBuilderService.preview(request);
            } catch (SectionBuilderException sectionBuilderException) {
                throw sectionBuilderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new SectionBuilderException(2001);
    }
}
