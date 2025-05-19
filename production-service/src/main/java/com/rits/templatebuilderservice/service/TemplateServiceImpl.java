package com.rits.templatebuilderservice.service;

import com.rits.templatebuilderservice.dto.TemplateRequest;
import com.rits.templatebuilderservice.dto.TemplateResponse;
import com.rits.templatebuilderservice.model.MessageDetails;
import com.rits.templatebuilderservice.model.MessageModel;
import com.rits.templatebuilderservice.model.Template;
import com.rits.templatebuilderservice.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService{

@Autowired
private TemplateRepository templateRepository;

    public Template createUpdateBuilder(Template template, TemplateRequest templateRequest, Boolean templateExists) {
        if(templateExists){
            template.setHandle(template.getHandle());
            template.setSite(template.getSite());
            template.setTemplateLabel(template.getTemplateLabel());
            template.setTemplateVersion(template.getTemplateVersion());
            template.setTemplateType(templateRequest.getTemplateType());
            template.setGroupIds(templateRequest.getGroupIds());
            template.setCurrentVersion(templateRequest.getCurrentVersion());
            template.setCreatedDateTime(template.getCreatedDateTime());
            template.setUserId(templateRequest.getUserId());
            template.setUpdatedDateTime(LocalDateTime.now());
            template.setActive(template.getActive());
        }
        else{
            template = Template.builder()
                    .handle("TemplateBO:"+templateRequest.getSite()+","+templateRequest.getTemplateLabel()+","+templateRequest.getTemplateVersion())
                    .templateLabel(templateRequest.getTemplateLabel())
                    .templateType(templateRequest.getTemplateType())
                    .templateVersion(templateRequest.getTemplateVersion())
                    .groupIds(templateRequest.getGroupIds())
                    .site(templateRequest.getSite())
                    .userId(templateRequest.getUserId())
                    .currentVersion(templateRequest.getCurrentVersion())
                    .active(1)
                    .createdDateTime(LocalDateTime.now())
                    .build();
        }
        return template;
    }


    @Override
    public MessageModel createTemplate(TemplateRequest templateRequest) throws Exception {
        Boolean templateExists = templateRepository.existsByHandleAndSiteAndActiveEquals("TemplateBO:"+templateRequest.getSite()+","+templateRequest.getTemplateLabel()+","+templateRequest.getTemplateVersion(), templateRequest.getSite(), 1);
        if(templateExists){
            throw new Exception("Template with this label already exists");
        }
        currentVersionUpdate(templateRequest);
        Template template = createUpdateBuilder(new Template(), templateRequest, false);
        return MessageModel.builder()
                .response(templateRepository.save(template))
                .message_details(MessageDetails.builder()
                        .msg("Template created successfully")
                        .msg_type("success")
                        .build())
                .build();
    }

    @Override
    public MessageModel updateTemplate(TemplateRequest templateRequest) throws Exception {
        Boolean templateExists = templateRepository.existsByHandleAndSiteAndActiveEquals("TemplateBO:"+templateRequest.getSite()+","+templateRequest.getTemplateLabel()+","+templateRequest.getTemplateVersion(), templateRequest.getSite(), 1);
        if(!templateExists){
            throw new Exception("Template with this label does not exist");
        }
        currentVersionUpdate(templateRequest);
        Template template = createUpdateBuilder(templateRepository.findByHandleAndSiteAndActiveEquals("TemplateBO:"+templateRequest.getSite()+","+templateRequest.getTemplateLabel()+","+templateRequest.getTemplateVersion(), templateRequest.getSite(), 1), templateRequest, true);
        return MessageModel.builder()
                .response(templateRepository.save(template))
                .message_details(MessageDetails.builder()
                        .msg("Template updated successfully")
                        .msg_type("success")
                        .build())
                .build();
    }

    @Override
    public MessageModel deleteTemplate(TemplateRequest templateRequest) throws Exception {
        Boolean templateExists = templateRepository.existsByHandleAndSiteAndActiveEquals("TemplateBO:" + templateRequest.getSite() + "," + templateRequest.getTemplateLabel() + "," + templateRequest.getTemplateVersion(), templateRequest.getSite(), 1);
        if (!templateExists) {
            throw new Exception("Template with this label does not exist");
        }

        Template template = templateRepository.findByHandleAndSiteAndActiveEquals("TemplateBO:" + templateRequest.getSite() + "," + templateRequest.getTemplateLabel() + "," + templateRequest.getTemplateVersion(), templateRequest.getSite(), 1);
        template.setActive(0);
        return MessageModel.builder()
                .response(templateRepository.save(template))
                .message_details(MessageDetails.builder()
                        .msg("Template deleted successfully")
                        .msg_type("success")
                        .build())
                .build();
    }

    @Override
    public Template retrieveTemplate(TemplateRequest templateRequest) throws Exception {
        Template template = templateRepository.findByHandleAndSiteAndActiveEquals("TemplateBO:" + templateRequest.getSite() + "," + templateRequest.getTemplateLabel() + "," + templateRequest.getTemplateVersion(), templateRequest.getSite(), 1);
        if (template != null && template.getHandle() != null) {
            return template;
        } else {
            throw new Exception("Template with this label does not exist");
        }
    }

    @Override
    public List<TemplateResponse> retrieveAllTemplates(TemplateRequest templateRequest) throws Exception {
        List<TemplateResponse> templates = templateRepository.findBySiteAndTemplateLabelContainingIgnoreCaseAndActiveEquals(templateRequest.getSite(), templateRequest.getTemplateLabel(), 1);
        if (templates != null && !templates.isEmpty()) {
            return templates;
        } else {
            throw new Exception("No templates found");
        }
    }

    @Override
    public List<TemplateResponse> retrieveTop50Template(TemplateRequest templateRequest) throws Exception {
        List<TemplateResponse> templates = templateRepository.findTop50BySiteAndActiveOrderByCreatedDateTimeDesc(templateRequest.getSite(), 1);
        if (templates != null && !templates.isEmpty()) {
            return templates;
        } else {
            throw new Exception("No templates found");
        }
    }

    @Override
    public void currentVersionUpdate(TemplateRequest templateRequest) throws Exception {
        Template templates = templateRepository.findBySiteAndTemplateLabelAndCurrentVersionAndActiveEquals(templateRequest.getSite(), templateRequest.getTemplateLabel(), true, 1);
        if (templates != null) {
            templates.setCurrentVersion(false);
            templateRepository.save(templates);
        }
    }

}

