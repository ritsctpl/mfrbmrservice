package com.rits.systemruleservice.controller;

import com.rits.systemruleservice.dto.SystemRuleRequest;
import com.rits.systemruleservice.exception.SystemRuleException;
import com.rits.systemruleservice.model.MessageModel;
import com.rits.systemruleservice.model.SystemRule;
import com.rits.systemruleservice.model.SystemRuleGroup;
import com.rits.systemruleservice.service.SystemRuleService;
import com.rits.systemruleservice.service.SystemRuleServiceImpl;
import com.rits.userservice.exception.UserException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/app/v1/systemrule-service")
public class SystemRuleServiceController {
    private final SystemRuleService systemRuleService;
    private final SystemRuleServiceImpl systemRuleServiceImpl;

    @PostMapping("uploadSystemRuleRecords")
    public MessageModel uploadSystemRuleRecords(@RequestBody List<SystemRule> systemRuleRequestList)
    {
        try {
            MessageModel uploadFile = systemRuleService.uploadSystemRuleRecords(systemRuleRequestList);
            return uploadFile;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("uploadSystemRuleGroupRecords")
    public MessageModel uploadSystemRuleGroupRecords(@RequestBody List<SystemRuleGroup> systemRuleGroupRequestList)
    {
        try {
            MessageModel uploadFile = systemRuleService.uploadSystemRuleGroupRecords(systemRuleGroupRequestList);
            return uploadFile;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveAll")
    public List<SystemRule> retrieveBySite(@RequestBody SystemRuleRequest systemRuleRequest)
    {
        List<SystemRule> systemRuleList = null;
        try {
            systemRuleList = systemRuleService.retrieveSystemRule(systemRuleRequest.getSite());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return systemRuleList;
    }

}