package com.rits.templatebuilderservice.service;

import com.rits.templatebuilderservice.dto.TemplateRequest;
import com.rits.templatebuilderservice.dto.TemplateResponse;
import com.rits.templatebuilderservice.model.MessageModel;
import com.rits.templatebuilderservice.model.Template;

import java.util.List;

public interface TemplateService {

    MessageModel createTemplate(TemplateRequest templateRequest) throws Exception;

    MessageModel updateTemplate(TemplateRequest templateRequest) throws Exception;

    MessageModel deleteTemplate(TemplateRequest templateRequest) throws Exception;

    Template retrieveTemplate(TemplateRequest templateRequest) throws Exception;

    List<TemplateResponse> retrieveAllTemplates(TemplateRequest templateRequest) throws Exception;

    List<TemplateResponse> retrieveTop50Template(TemplateRequest templateRequest) throws Exception;

    void currentVersionUpdate(TemplateRequest templateRequest) throws Exception;
}
