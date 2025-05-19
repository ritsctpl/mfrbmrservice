package com.rits.componentbuilderservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rits.componentbuilderservice.dto.ComponentRequest;
import com.rits.componentbuilderservice.dto.ComponentResponse;
import com.rits.componentbuilderservice.exception.ComponentBuilderException;
import com.rits.componentbuilderservice.model.Component;
import com.rits.componentbuilderservice.model.MessageModel;
import com.rits.componentbuilderservice.service.ComponentService;
import com.rits.componentbuilderservice.service.ComponentServiceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/componentbuilder-service")
public class ComponentController {
  private final ComponentServiceImpl componentServiceImpl;


    @PostMapping("/createComponent")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createComponent(@RequestBody ComponentRequest componentRequest) throws JsonProcessingException {
        MessageModel createComponent;
        try {
            createComponent = componentServiceImpl.createComponent(componentRequest);
            return ResponseEntity.ok(createComponent);
        }
        catch (ComponentBuilderException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/updateComponent")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> updateComponent(@RequestBody ComponentRequest componentRequest) throws JsonProcessingException {
        MessageModel updateComponent;
        try {
            updateComponent = componentServiceImpl.updateComponent(componentRequest);
            return ResponseEntity.ok(updateComponent);
        }
        catch (ComponentBuilderException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/deleteComponent")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> deleteComponent(@RequestBody ComponentRequest componentRequest) throws JsonProcessingException {
        MessageModel deleteComponent;
        try {
            deleteComponent = componentServiceImpl.deleteComponent(componentRequest);
            return ResponseEntity.ok(deleteComponent);
        }
        catch (ComponentBuilderException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getComponentById")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> retrieveComponent(@RequestBody ComponentRequest componentRequest) throws JsonProcessingException {
        Component retrieveComponent;
        try {
            retrieveComponent = componentServiceImpl.retrieveComponent(componentRequest);
            return ResponseEntity.ok(retrieveComponent);
        }
        catch (ComponentBuilderException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getAllComponent")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> retrieveAllComponent(@RequestBody ComponentRequest componentRequest) throws JsonProcessingException {
        List<ComponentResponse> retrieveAllComponent;
        try {
            retrieveAllComponent = componentServiceImpl.retrieveAllComponent(componentRequest);
            return ResponseEntity.ok(retrieveAllComponent);
        }
        catch (ComponentBuilderException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getTop50Component")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> retrieveTop50Component(@RequestBody ComponentRequest componentRequest) throws JsonProcessingException {
        List<ComponentResponse> retrieveTop50Component;
        try {
            retrieveTop50Component = componentServiceImpl.retrieveTop50Component(componentRequest);
            return ResponseEntity.ok(retrieveTop50Component);
        }
        catch (ComponentBuilderException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
