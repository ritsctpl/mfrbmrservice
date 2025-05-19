package com.rits.kafkaevent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
@Slf4j
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductionLogEvent extends BaseEvent{
    @JsonProperty("eventId")
    private String eventId;

    @JsonProperty("eventType")
    private String eventType;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("pcuBO")
    private String pcuBO;

    @JsonProperty("shopOrderBO")
    private String shopOrderBO;

    @JsonProperty("operation_bo")
    private String operation_bo;

    @JsonProperty("workCenterBO")
    private String workCenterBO;

    @JsonProperty("resourceBO")
    private String resourceBO;

    @JsonProperty("eventData")
    private String eventData;

    @JsonProperty("itemBO")
    private String itemBO;

    @JsonProperty("dc_grp")
    private String dc_grp;

    @JsonProperty("data_field")
    private String data_field;

    @JsonProperty("data_value")
    private String data_value;

    @JsonProperty("component")
    private String component;

    @JsonProperty("nc")
    private String nc;

    @JsonProperty("meta_data")
    private String meta_data;

    @JsonProperty("active")
    private int active;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
}
