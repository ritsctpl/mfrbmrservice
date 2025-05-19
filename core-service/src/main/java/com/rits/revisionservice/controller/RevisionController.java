package com.rits.revisionservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rits.revisionservice.service.RevisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/revision-service")
public class RevisionController {
    private final RevisionService revisionService;

    @PostMapping("addRevisionExtension")
    public String addRevisionExtension(@RequestBody String request){
        try {
            return revisionService.addRevisionExtension(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
