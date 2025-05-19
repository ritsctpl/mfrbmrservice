package com.rits.signoffservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SignOffRequest implements Cloneable {
    private String site;
    private String handle;
    private LocalDateTime dateTime;
    private String pcuBO;
    private String itemBO;
    private String routerBO;
    private String resourceBO;
    private String operationBO;
    private String stepID;
    private String userBO;
    private String quantity;
    private String qtyToComplete;
    private String workCenter;
    private String qtyInQueue;
    private String shopOrderBO;
    private String childRouterBO;
    private String parentStepID;
    private String status;
    private LocalDateTime createdDateTime;
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
