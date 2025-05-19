package com.rits.pco.service;

import com.rits.pco.dto.*;

public interface PcoCamelService {
    void processHandshake(HandshakeRequest request);
    void registerPcoAgent(PcoAgentRequest request);
    void processApiRequest(ApiRequest request);
    void initializeTopicListeners();
}
