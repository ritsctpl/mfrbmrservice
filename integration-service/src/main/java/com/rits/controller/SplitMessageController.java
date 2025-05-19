package com.rits.controller;

import com.rits.integration.model.SplitMessageEntity;
import com.rits.integration.repository.SplitMessageRepository;
import com.rits.kafkapojo.ProcessedMessage;
import com.rits.service.SplitMessageProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/split-message")
public class SplitMessageController {

    @Autowired
    private SplitMessageRepository splitMessageRepository;

    @Autowired
    private SplitMessageProcessingService splitMessageProcessingService;

    // Get a split message by its ID
    @GetMapping("/{id}")
    public ResponseEntity<SplitMessageEntity> getSplitMessageById(@PathVariable String id) {
        SplitMessageEntity splitMessage = splitMessageRepository.findById(id).orElse(null);
        if (splitMessage == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(splitMessage);
    }

    // Get all unprocessed split messages for a specific parentIdentifier
    @GetMapping("/parent/{parentIdentifier}")
    public ResponseEntity<List<SplitMessageEntity>> getSplitMessagesByParent(@PathVariable String parentIdentifier) {
        List<SplitMessageEntity> messages = splitMessageRepository.findByParentIdentifierAndProcessed(parentIdentifier, false);
        return ResponseEntity.ok(messages);
    }

    // Update split message processed status
    @PutMapping("/{id}/processed")
    public ResponseEntity<SplitMessageEntity> markMessageAsProcessed(@PathVariable String id) {
        SplitMessageEntity splitMessage = splitMessageRepository.findById(id).orElse(null);
        if (splitMessage == null) {
            return ResponseEntity.notFound().build();
        }

        splitMessage.setProcessed(true);
        splitMessage.setCreatedDateTime(LocalDateTime.now());
        splitMessageRepository.save(splitMessage);
        return ResponseEntity.ok(splitMessage);
    }

    @PostMapping("/delete")
    public boolean deleteSplitMessages(@RequestBody SplitMessageEntity request) {
        return splitMessageProcessingService.deleteSplitMessages(request.getHours(), request.getMinutes(), request.getSeconds());
    }
}
