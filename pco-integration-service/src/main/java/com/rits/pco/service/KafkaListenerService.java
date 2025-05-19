package com.rits.pco.service;

import com.rits.pco.dto.*;

public interface KafkaListenerService {
    void processHandshake(HandshakeRequest request);
    void registerPcoAgent(PcoAgentRequest request);
    //void processApiRequest(ApiRequest request);

    void processApiRequest(String message);
    void initializeTopicListeners();
}
