package com.rits.kafkapojo;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MessageWrapper<T> {
    private String messageId;
    private T payload;
/*
    public MessageWrapper(String messageId, T payload) {
        this.messageId = messageId;
        this.payload = payload;
    }

    // Getters and setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }*/
}
