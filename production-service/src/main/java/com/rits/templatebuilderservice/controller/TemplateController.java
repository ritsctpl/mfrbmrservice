package com.rits.templatebuilderservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rits.templatebuilderservice.dto.TemplateRequest;
import com.rits.templatebuilderservice.dto.TemplateResponse;
import com.rits.templatebuilderservice.exception.TemplateException;
import com.rits.templatebuilderservice.model.MessageModel;
import com.rits.templatebuilderservice.model.Template;
import com.rits.templatebuilderservice.service.TemplateServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/templatebuilder-service")
public class TemplateController {

    private final TemplateServiceImpl templateServiceImpl;

    @PostMapping("/createTemplate")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createTemplate(@RequestBody TemplateRequest templateRequest) throws JsonProcessingException {

        MessageModel createTemplate;
        try {
            createTemplate = templateServiceImpl.createTemplate(templateRequest);
            return ResponseEntity.ok(createTemplate);
        } catch (TemplateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

     @PostMapping("/updateTemplate")
     @ResponseStatus(HttpStatus.OK)
     public ResponseEntity<?> updateTemplate(@RequestBody TemplateRequest templateRequest) throws JsonProcessingException {
         MessageModel updateTemplate;
         try {
             updateTemplate = templateServiceImpl.updateTemplate(templateRequest);
             return ResponseEntity.ok(updateTemplate);
         } catch (TemplateException e) {
             throw e;
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }

        @PostMapping("/deleteTemplate")
        @ResponseStatus(HttpStatus.OK)
        public ResponseEntity<?> deleteTemplate(@RequestBody TemplateRequest templateRequest) throws JsonProcessingException {
            MessageModel deleteTemplate;
            try {
                deleteTemplate = templateServiceImpl.deleteTemplate(templateRequest);
                return ResponseEntity.ok(deleteTemplate);
            } catch (TemplateException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    @PostMapping("/getTemplate")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> retrieveTemplate(@RequestBody TemplateRequest templateRequest) throws JsonProcessingException {
        Template retrieveTemplate;
        try {
            retrieveTemplate = templateServiceImpl.retrieveTemplate(templateRequest);
            return ResponseEntity.ok(retrieveTemplate);
        } catch (TemplateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getAllTemplate")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> retrieveTemplates(@RequestBody TemplateRequest templateRequest) throws JsonProcessingException {
        List<TemplateResponse> retrieveTemplates;
        try {
            retrieveTemplates = templateServiceImpl.retrieveAllTemplates(templateRequest);
            return ResponseEntity.ok(retrieveTemplates);
        } catch (TemplateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getTop50Template")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> retrieveTop50Template(@RequestBody TemplateRequest templateRequest) throws JsonProcessingException {
        List<TemplateResponse> retrieveTop50Template;
        try {
            retrieveTop50Template = templateServiceImpl.retrieveTop50Template(templateRequest);
            return ResponseEntity.ok(retrieveTop50Template);
        } catch (TemplateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }




}
