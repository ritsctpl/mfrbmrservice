package com.rits.site.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.auditlogservice.dto.AuditLogRequest;
import com.rits.auditlogservice.service.AuditLogService;
import com.rits.auditlogservice.service.ProducerEvent;
import com.rits.auditlogservice.service.ProducerEventListener;
import com.rits.site.dto.*;
import com.rits.site.exception.SiteException;
import com.rits.site.model.MessageModel;
import com.rits.site.model.Site;
import com.rits.site.service.SiteServiceImpl;

import com.rits.usergroupservice.exception.UserGroupException;
import com.rits.userservice.exception.UserException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/site-service")
public class SiteController {

    @Lazy
    private final SiteServiceImpl siteServiceImpl;
    private final ApplicationEventPublisher eventPublisher;


//    "required": ["site"]

//    {
//
//        "site":"RITS322",
//
//            "description":"rits2",
//
//            "type":"type3",
//
//            "timeZone":[],
//            "activityHookLists":[
//            {
//              "sequence":"",
//              "hookPoint":"",
//              "activity":"",
//              "enabled":true,
//              "userArgument":""
//            }],
//
//        "local":true
//
//    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createSite(@RequestBody SiteRequest siteRequest) throws Exception {
        MessageModel createSite;
        if (siteRequest.getSite() != null && !siteRequest.getSite().isEmpty()) {
            try {
                createSite = siteServiceImpl.createSite(siteRequest);
                AuditLogRequest auditlog = siteServiceImpl.createAuditLog(siteRequest);
                eventPublisher.publishEvent(new ProducerEvent(auditlog));
                return ResponseEntity.ok(MessageModel.builder().message_details(createSite.getMessage_details()).response(createSite.getResponse()).build());
            } catch (SiteException e) {
                throw e;
            } catch (UserException e) {
                throw e;
            } catch (UserGroupException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new SiteException(2103);
    }

//    {
//        "site":"RITS"
//    }

    @PostMapping("/isExist")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> isSiteExists(@RequestBody SiteRequest siteRequest) {
        if (siteRequest.getSite() != null && !siteRequest.getSite().isEmpty()) {
            try {
                Boolean isSiteExist = siteServiceImpl.isSiteExists(siteRequest);
                return ResponseEntity.ok(isSiteExist);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new SiteException(2103, siteRequest.getSite());
    }

//    {
//
//        "site":"RITS2",
//
//            "description":"rits2",
//
//            "type":"type3",
//
//            "timeZone":["time1","time2","time3","time4","time5"],
//           "activityHookLists":[
//            {
//              "sequence":"",
//              "hookPoint":"",
//              "activity":"",
//              "enabled":true,
//              "userArgument":""
//            }]
//
//        "local":false
//
//    }

    @PostMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<MessageModel> updateSite(@RequestBody SiteRequest siteRequest) throws JsonProcessingException {
        MessageModel updateSite;
        if (siteRequest.getSite() != null && !siteRequest.getSite().isEmpty()) {
            try {
                updateSite = siteServiceImpl.updateSite(siteRequest);
                AuditLogRequest auditlog = siteServiceImpl.updateAuditLog(siteRequest);
                eventPublisher.publishEvent(new ProducerEvent(auditlog));
                return ResponseEntity.ok(MessageModel.builder().message_details(updateSite.getMessage_details()).response(updateSite.getResponse()).build());
            } catch (SiteException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new SiteException(2103);
    }

//    {
//        "site":"RITS2"
//    }

    @PostMapping("/retrieveTimeZoneList")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TimeZoneResponse> retrieveTimeZoneList() {
        try {
            TimeZoneResponse timeZoneList = siteServiceImpl.retrieveTimeZoneList();
            return ResponseEntity.ok(timeZoneList);
        } catch (SiteException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    No need to pass any Json

    @PostMapping("/retrieveTop50")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<RetrieveTop50Response> retrieveTop50() {
        try {
            RetrieveTop50Response retrieveTop50 = siteServiceImpl.retrieveTop50();
            return ResponseEntity.ok(retrieveTop50);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    {
//        "site":"RITS1"
//    }

    @PostMapping("/retrieveBySite")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Site> retrieveBySite(@RequestBody SiteRequest siteRequest) {
        if (siteRequest.getSite() != null && !siteRequest.getSite().isEmpty()) {
            try {
                Site retrieveBySite = siteServiceImpl.retrieveBySite(siteRequest);
                return ResponseEntity.ok(retrieveBySite);
            } catch (SiteException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new SiteException(2103, siteRequest.getSite());
    }
}
