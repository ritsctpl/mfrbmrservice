package com.rits.controller;

import com.rits.kafkapojo.ProcessedMessage;
import com.rits.kafkaservice.ProcessedMessageService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/app/v1/integration-service/processed-messages")
public class ProcessedMessageController {

    private final ProcessedMessageService service;

    public ProcessedMessageController(ProcessedMessageService service) {
        this.service = service;
    }

    @GetMapping("/retrieveAllForFilters")
    public List<ProcessedMessage> getAllProcessedMessagesForFilters(
            @RequestParam(required = false) String topicName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        return service.getAllProcessedMessages(topicName, status, startDate, endDate);
    }

    @GetMapping("/retrieveAll")
    public List<ProcessedMessage> getAllProcessedMessages() {
        return service.getAllProcessedMessages();
    }

    @GetMapping("/retrieveTop50")
    public List<ProcessedMessage> getTop50ProcessedMessages() {
        return service.getTop50ProcessedMessages();
    }

    @PostMapping("/delete")
    public boolean deleteProcessedMessages(@RequestBody ProcessedMessage request) {
        return service.deleteProcessedMessages(request.getStatus(), request.getHours(), request.getMinutes(), request.getSeconds());
    }

    @GetMapping("/{messageId}/exists")
    public boolean isMessageProcessed(@PathVariable String messageId) {
        return service.isMessageProcessed(messageId);
    }

    @PostMapping
    public ProcessedMessage createProcessedMessage(@RequestBody ProcessedMessage message) {
        return service.createProcessedMessage(message);
    }

    @DeleteMapping("/{messageId}")
    public void deleteProcessedMessage(@PathVariable String messageId) {
        service.deleteProcessedMessage(messageId);
    }
}
