package com.rits.kafkaevent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BaseEvent {
    @JsonProperty("productionLogType")
    private String eventType;
}
