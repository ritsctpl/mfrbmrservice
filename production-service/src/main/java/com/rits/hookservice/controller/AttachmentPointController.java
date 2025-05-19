package com.rits.hookservice.controller;

import com.rits.hookservice.model.AttachmentPoint;
import com.rits.hookservice.repository.AttachmentPointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/app/v1/hook-points")
public class AttachmentPointController {
   /* {
        "_id": {
        "$oid": "67b0ef72a462a627465e7e09"
    },
        "targetClass": "com.rits.processorderstateservice.service.ProcessOrderStateServiceImpl",
            "targetMethod": "testHookableProcessStart",
            "hookType": "HOOK",
            "hookPoint": "BEFORE",
            "hookClass": "customHook",
            "hookMethod": "beforeStartProcess",
            "executionMode": "SYNC",
            "active": true,
            "_class": "com.rits.hookservice.model.AttachmentPoint"
    }
    {
        "_id": {
        "$oid": "67b0f45d2030eb478d24210a"
    },
        "targetClass": "com.rits.processorderstateservice.service.ProcessOrderStateServiceImpl",
            "targetMethod": "testHookableProcessStart",
            "hookType": "EXTENSION",
            "hookPoint": "BEFORE",
            "hookClass": "customServiceExtension",
            "hookMethod": "beforeStartProcess",
            "executionMode": "SYNC",
            "active": true,
            "_class": "com.rits.hookservice.model.AttachmentPoint"
    }
    {
        "_id": {
        "$oid": "67b0f4722030eb478d24210b"
    },
        "targetClass": "com.rits.processorderstateservice.service.ProcessOrderStateServiceImpl",
            "targetMethod": "testHookableProcessStart",
            "hookType": "EXTENSION",
            "hookPoint": "AFTER",
            "hookClass": "customServiceExtension",
            "hookMethod": "afterStartProcess",
            "executionMode": "SYNC",
            "active": true,
            "_class": "com.rits.hookservice.model.AttachmentPoint"
    }*/


    @Autowired
    private AttachmentPointRepository attachmentPointRepository;

    // GET all attachment points
    @GetMapping
    public List<AttachmentPoint> getAllAttachmentPoints() {
        return attachmentPointRepository.findAll();
    }

    // GET a single attachment point by ID
    @GetMapping("/{id}")
    public ResponseEntity<AttachmentPoint> getAttachmentPointById(@PathVariable String id) {
        return attachmentPointRepository.findById(id)
                .map(attachmentPoint -> ResponseEntity.ok().body(attachmentPoint))
                .orElse(ResponseEntity.notFound().build());
    }

    // POST a new attachment point
    @PostMapping
    public AttachmentPoint createAttachmentPoint(@RequestBody AttachmentPoint attachmentPoint) {
        return attachmentPointRepository.save(attachmentPoint);
    }

    // PUT (update) an existing attachment point
    @PutMapping("/{id}")
    public ResponseEntity<AttachmentPoint> updateAttachmentPoint(@PathVariable String id,
                                                                 @RequestBody AttachmentPoint attachmentPointDetails) {
        return attachmentPointRepository.findById(id)
                .map(attachmentPoint -> {
                    attachmentPoint.setTargetClass(attachmentPointDetails.getTargetClass());
                    attachmentPoint.setTargetMethod(attachmentPointDetails.getTargetMethod());
                    attachmentPoint.setHookType(attachmentPointDetails.getHookType());
                    attachmentPoint.setHookPoint(attachmentPointDetails.getHookPoint());
                    attachmentPoint.setHookClass(attachmentPointDetails.getHookClass());
                    attachmentPoint.setHookMethod(attachmentPointDetails.getHookMethod());
                    attachmentPoint.setExecutionMode(attachmentPointDetails.getExecutionMode());
                    attachmentPoint.setActive(attachmentPointDetails.isActive());
                    AttachmentPoint updatedAttachmentPoint = attachmentPointRepository.save(attachmentPoint);
                    return ResponseEntity.ok().body(updatedAttachmentPoint);
                }).orElse(ResponseEntity.notFound().build());
    }

    // DELETE an attachment point by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAttachmentPoint(@PathVariable String id) {
        return attachmentPointRepository.findById(id)
                .map(attachmentPoint -> {
                    attachmentPointRepository.delete(attachmentPoint);
                    return ResponseEntity.ok().build();
                }).orElse(ResponseEntity.notFound().build());
    }
}
