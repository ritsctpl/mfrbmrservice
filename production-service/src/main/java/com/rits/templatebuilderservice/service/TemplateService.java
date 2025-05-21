package com.rits.templatebuilderservice.service;

import com.rits.templatebuilderservice.dto.TemplateRequest;
import com.rits.templatebuilderservice.dto.TemplateResponse;
import com.rits.templatebuilderservice.model.MessageModel;
import com.rits.templatebuilderservice.model.Template;
import org.bson.Document;

import java.util.List;

public interface TemplateService {

  MessageModel createTemplate(TemplateRequest templateRequest) throws Exception;

    MessageModel UpdateTemplate(TemplateRequest templateRequest) throws Exception;

    MessageModel deleteTemplate(TemplateRequest templateRequest) throws Exception;

    Template retrieveTemplate(TemplateRequest templateRequest) throws Exception;

    List<TemplateResponse> retrieveTop50Template(TemplateRequest templateRequest) throws Exception;

    List<TemplateResponse> retrieveAllTemplate(TemplateRequest templateRequest) throws Exception;

    List preview(TemplateRequest templateRequest)throws Exception;
}
