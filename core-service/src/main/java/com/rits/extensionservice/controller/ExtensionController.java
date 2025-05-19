package com.rits.extensionservice.controller;

import com.rits.extensionservice.dto.ExtensionRequest;
import com.rits.extensionservice.service.ExtensionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/extension-service")
public class ExtensionController {
    private final ExtensionService extensionService;

    @PostMapping("addExtension")
    public ResponseEntity<String> addExtension(@RequestBody ExtensionRequest extensionRequest){
        try{
            return ResponseEntity.ok(extensionService.createExtension(extensionRequest));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
