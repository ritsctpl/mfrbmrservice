package com.rits.pco.service;

public interface KafkaMessageService {
    void sendMessage(String topic, String key, String message);

    void sendMessageApi(String topic, String key, Object message);
}
