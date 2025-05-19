
package com.rits.systemruleservice.service;

import com.rits.systemruleservice.dto.*;
import com.rits.systemruleservice.model.MessageModel;
import com.rits.systemruleservice.model.SystemRule;
import com.rits.systemruleservice.model.SystemRuleGroup;

import java.util.List;

public interface SystemRuleService {

    MessageModel uploadSystemRuleRecords(List<SystemRule> systemRuleRequests) throws Exception;

    MessageModel uploadSystemRuleGroupRecords(List<SystemRuleGroup> systemRuleGroupRequests) throws Exception;

    List<SystemRule> retrieveSystemRule(String site)throws Exception;
}

