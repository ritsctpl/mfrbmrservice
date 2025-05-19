package com.rits.pco.controller;

import com.rits.pco.dto.*;
import com.rits.pco.service.PcoCamelService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app/v1/pco-integration-service")
public class PcoController {
    private final PcoCamelService pcoCamelService;

    public PcoController(PcoCamelService pcoCamelService) {
        this.pcoCamelService = pcoCamelService;
    }

    @PostMapping("/handshake")
    public void handshake(@RequestBody HandshakeRequest request) {
        pcoCamelService.processHandshake(request);
    }

    @PostMapping("/register")
    public void registerPcoAgent(@RequestBody PcoAgentRequest request) {
        pcoCamelService.registerPcoAgent(request);
    }

    @PostMapping("/api-request")
    public void processApiRequest(@RequestBody ApiRequest request) {
        pcoCamelService.processApiRequest(request);
    }
}

/*
package com.rits.pco.controller;

import com.rits.pco.dto.*;
import com.rits.pco.service.KafkaListenerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app/v1/pco-integration-service")
public class PcoController {
    private final KafkaListenerService kafkaListenerService;

    public PcoController(KafkaListenerService kafkaListenerService) {
        this.kafkaListenerService = kafkaListenerService;
    }

    @PostMapping("/handshake")
    public void handshake(@RequestBody HandshakeRequest request) {
        kafkaListenerService.processHandshake(request);
    }

    @PostMapping("/register")
    public void registerPcoAgent(@RequestBody PcoAgentRequest request) {
        kafkaListenerService.registerPcoAgent(request);
    }
}*/
