package com.rits.event;

import org.springframework.context.ApplicationEvent;

public class WebClientCallEvent extends ApplicationEvent {

    private final String url; // API endpoint
    private final Object requestPayload; // Request body payload

    public WebClientCallEvent(Object source, String url, Object requestPayload) {
        super(source);
        this.url = url;
        this.requestPayload = requestPayload;
    }

    public String getUrl() {
        return url;
    }

    public Object getRequestPayload() {
        return requestPayload;
    }
}
