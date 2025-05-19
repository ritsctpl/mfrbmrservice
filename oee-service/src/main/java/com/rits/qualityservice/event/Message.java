package com.rits.qualityservice.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Message<T> {
    private String eventType;
    private T object;

    @JsonCreator
    public Message(@JsonProperty("eventtype") String eventType, @JsonProperty("object") T object) {
        this.eventType = eventType;
        this.object = object;
    }

    public String getEventType() {
        return eventType;
    }

    public T getObject() {
        return object;
    }
}
