package com.rits.scrapservice.dto;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuInWork {
    private String site;
    private String handle;
    private String pcuBO;
    private String itemBO;
    private String routerBO;
    private String operationBO;
    private String resourceBO;
    private String stepID;
    private String userBO;
    private String workCenter;
    private String qtyInWork;
    private String qtyToComplete;
    private String shopOrderBO;
    private String childRouterBO;
    private String parentStepID;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}

