package com.rits.systemruleservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "R_SYSTEM_RULE")
public class SystemRule {
    @Id
    private String handle;
    private String moduleId;
    private String serviceName;
    private String methodName;
    private Request request;
}
