
package com.rits.systemruleservice.repository;

import com.rits.systemruleservice.model.SystemRule;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SystemRuleRepository extends MongoRepository<SystemRule, Long> {

    List<SystemRule> findByRequestSystemRuleSettingSite(String site);
}
