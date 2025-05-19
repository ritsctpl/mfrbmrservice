package com.rits.systemruleservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "R_SYSTEM_RULE_GROUP")
public class SystemRuleGroup {
    @Id
    private String handle;
    private String moduleId;
    private String serviceName;
    private String methodName;
    private SystemRuleGroupRequest request;

}
