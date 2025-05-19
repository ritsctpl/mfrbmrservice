package com.rits.startservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "START_SERVICE")
public class Start {
    private String site;
    @Id
    private String handle;
    private LocalDateTime dateTime;
    private String pcuBO;
    private String itemBO;
    private String routerBO;
    private String operationBO;
    private String resourceBO;
    private String workCenter;
    private String quantity;
    private String stepID;
    private String userBO;
    private String shopOrderBO;
    private String childRouterBO;
    private String parentStepID;
    private int active;
}
