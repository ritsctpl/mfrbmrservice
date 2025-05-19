package com.rits.systemruleservice.repository;

import com.rits.systemruleservice.model.SystemRuleGroup;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SystemRuleGroupRepository extends MongoRepository<SystemRuleGroup,String> {
}
