package com.rits.systemruleservice.dto;

import com.rits.systemruleservice.model.Request;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SystemRuleRequest {
    private String handle;
    private String moduleId;
    private String serviceName;
    private String methodName;
    private Request request;
    private String site;
}
