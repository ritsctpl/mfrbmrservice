package com.rits.toollogservice.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "R_TOOL_LOG")
public class ToolLog {
    private String handle;
    private String site;
    private String toolGroupBO;
    private String toolNumberBO;
    private int loggedQty;
    private String pcuBO;
    private String itemBO;
    private String routerBO;
    private String operationBO;
    private String resourceBO;
    private String shopOrderBO;
    private String workCenterBO;
    private String attachment;
    private String comments;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String userId;
}
