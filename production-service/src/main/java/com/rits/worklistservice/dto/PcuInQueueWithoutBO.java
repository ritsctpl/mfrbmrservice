package com.rits.worklistservice.dto;

import lombok.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class PcuInQueueWithoutBO {
    private String site;
    private String handle;
    private LocalDateTime dateTime;
    private String pcu;
    private String item;
    private String itemVersion;
    private String router;
    private String routerVersion;
    private String operation;
    private String operationVersion;
    private String resource;
    private String stepID;
    private String user;
    private String shopOrder;
    private String childRouter;
    private String childRouterVersion;
    private String parentStepID;
    private String type;
    private int recordLimit;

    private String qtyInWork;// for in work
    private String qtyInQueue; // for inqueue
    private String qtyDone; //for done

    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;

}
