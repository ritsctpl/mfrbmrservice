package com.rits.site.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.rits.auditlogservice.dto.AuditLogRequest;
import com.rits.site.dto.*;
import com.rits.site.model.MessageModel;
import com.rits.site.model.Site;

public interface SiteService {
    public MessageModel createSite(SiteRequest siteRequest) throws Exception;

    public Boolean isSiteExists(SiteRequest siteRequest) throws Exception;

    public MessageModel updateSite(SiteRequest siteRequest) throws Exception;

    public TimeZoneResponse retrieveTimeZoneList() throws Exception;

    public RetrieveTop50Response retrieveTop50() throws Exception;

    public Site retrieveBySite(SiteRequest siteRequest) throws Exception;

    public String callExtension(Extension extension);

    AuditLogRequest createAuditLog(SiteRequest siteRequest);

    AuditLogRequest updateAuditLog(SiteRequest siteRequest);

}
