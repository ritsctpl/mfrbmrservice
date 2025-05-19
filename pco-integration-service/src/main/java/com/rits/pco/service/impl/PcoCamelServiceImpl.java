package com.rits.pco.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rits.pco.dto.*;
import com.rits.pco.model.ApiTransaction;
import com.rits.pco.model.HandshakeRecord;
import com.rits.pco.model.PcoAgentRecord;
import com.rits.pco.repository.ApiTransactionRepository;
import com.rits.pco.repository.HandshakeRepository;
import com.rits.pco.repository.PcoAgentRepository;
import com.rits.pco.service.PcoCamelService;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Service
public class PcoCamelServiceImpl implements PcoCamelService {
    private final CamelContext camelContext;
    private final ProducerTemplate producerTemplate;
    private final HandshakeRepository handshakeRepository;
    private final PcoAgentRepository pcoAgentRepository;
    private final ApiTransactionRepository apiTransactionRepository;
    private final WebClient webClient;  // ✅ WebClient injected properly
    private final ObjectMapper objectMapper = new ObjectMapper();
    public PcoCamelServiceImpl(CamelContext camelContext, ProducerTemplate producerTemplate,
                               HandshakeRepository handshakeRepository,
                               PcoAgentRepository pcoAgentRepository,
                               ApiTransactionRepository apiTransactionRepository,
                               WebClient.Builder webClientBuilder) {
        this.camelContext = camelContext;  // ✅ Inject WebClient.Builder
        this.producerTemplate = producerTemplate;
        this.handshakeRepository = handshakeRepository;
        this.pcoAgentRepository = pcoAgentRepository;
        this.apiTransactionRepository = apiTransactionRepository;
        this.webClient = webClientBuilder.build();  // ✅ Build WebClient instance
    }

    //@PostConstruct
    public void initializeTopicListeners() {
        try {
            if (!camelContext.isStarted()) {
                System.out.println("⚡ Starting CamelContext inside PcoCamelServiceImpl...");
                camelContext.start();
            }

            System.out.println("✅ PcoCamelServiceImpl: Sending test message to Kafka topic...");
     //       producerTemplate.sendBody("direct:testKafkaRoute", "Test Message");

        } catch (Exception e) {
            System.err.println("❌ Error initializing Kafka Route: " + e.getMessage());
        }
    }

    public void processHandshake(HandshakeRequest request) {
        HandshakeRecord handshake = new HandshakeRecord(null, request.getCorrelationId(), request.getPcoId(), request.getUsername(), "");
        handshakeRepository.save(handshake);
        HandshakeResponse response = new HandshakeResponse(request.getCorrelationId(), request.getPcoId(), "registered");
        producerTemplate.sendBody("kafka:fenta-response", response);
    }

    public void registerPcoAgent(PcoAgentRequest request) {
        PcoAgentRecord pcoAgent = new PcoAgentRecord(null, request.getCorrelationId(), request.getPcoId(), request.getAgentId(), request.getUsername(), request.getStatus(),request.isResponseEnabled());
        pcoAgent = pcoAgentRepository.save(pcoAgent);

        String newTopic = request.getPcoId() + "-" + request.getAgentId();
        String responseTopic = newTopic + "-response";

        producerTemplate.sendBody("direct:startListening", newTopic);
        producerTemplate.sendBody("direct:startListening", responseTopic);

        PcoAgentResponse response = new PcoAgentResponse(pcoAgent.getId(), request.getCorrelationId(), request.getPcoId(), request.getAgentId(), "registered");
        producerTemplate.sendBody("kafka:fenta-pco-agent-response", response);
    }

    public void processApiRequest(ApiRequest request) {
        // ✅ Extract request body as Map
        Map<String, Object> requestBody = request.getRequest();

        // ✅ Build WebClient dynamically based on method type
        WebClient.RequestHeadersSpec<?> requestSpec;
        HttpMethod httpMethod = HttpMethod.resolve(request.getMethod().toUpperCase());

        if (httpMethod == HttpMethod.GET || httpMethod == HttpMethod.DELETE) {
            // GET & DELETE don't require a body
            requestSpec = webClient.method(httpMethod)
                    .uri(request.getApiUrl());
        } else {
            // POST, PUT, PATCH require a body
            requestSpec = webClient.method(httpMethod)
                    .uri(request.getApiUrl())
                    .bodyValue(requestBody);
        }

        // ✅ Send request & capture response
        String response = requestSpec.retrieve()
                .bodyToMono(String.class)
                .block();

        // ✅ Store request as `Map<String, Object>`
        ApiTransaction transaction = new ApiTransaction(
                null,
                request.getCorrelationId(),
                request.getPcoId(),
                request.getAgentId(),
                request.getApiUrl(),
                requestBody,  // ✅ Store as a Map, NOT a String
                request.getMethod(),
                response
        );

        // ✅ Save in MongoDB
        apiTransactionRepository.save(transaction);

        // ✅ Ensure Kafka sends as JSON object
        try {
            producerTemplate.sendBody(
                    "kafka:" + request.getPcoId() + "-" + request.getAgentId(),
                    objectMapper.writeValueAsString(transaction)
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing transaction for Kafka", e);
        }
    }



   /* public void processApiRequest(ApiRequest request) {
        String response = webClient
                .method(HttpMethod.valueOf(request.getMethod()))
                .uri(request.getApiUrl())
                .bodyValue(request.getRequest())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        ApiTransaction transaction = new ApiTransaction(null, request.getCorrelationId(), request.getPcoId(), request.getAgentId(), request.getApiUrl(), request.getRequest(), request.getMethod(), response);
        apiTransactionRepository.save(transaction);

        String responseTopic = request.getPcoId() + "-" + request.getAgentId() + "-response";
        producerTemplate.sendBody("kafka:" + responseTopic, transaction);
    }*/
}


/*
package com.rits.pco.service.impl;

import com.rits.pco.dto.*;
import com.rits.pco.model.ApiTransaction;
import com.rits.pco.model.HandshakeRecord;
import com.rits.pco.model.PcoAgentRecord;
import com.rits.pco.repository.ApiTransactionRepository;
import com.rits.pco.repository.HandshakeRepository;
import com.rits.pco.repository.PcoAgentRepository;
import com.rits.pco.service.PcoCamelService;
import org.apache.camel.ProducerTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class PcoCamelServiceImpl implements PcoCamelService {

    private final ProducerTemplate producerTemplate;
    private final HandshakeRepository handshakeRepository;
    private final PcoAgentRepository pcoAgentRepository;
    private final ApiTransactionRepository apiTransactionRepository;
    private final WebClient webClient;

    public PcoCamelServiceImpl(ProducerTemplate producerTemplate,
                               HandshakeRepository handshakeRepository,
                               PcoAgentRepository pcoAgentRepository,
                               ApiTransactionRepository apiTransactionRepository) {
        this.producerTemplate = producerTemplate;
        this.handshakeRepository = handshakeRepository;
        this.pcoAgentRepository = pcoAgentRepository;
        this.apiTransactionRepository = apiTransactionRepository;
        this.webClient = WebClient.create();
    }

    @PostConstruct
    public void initializeTopicListeners() {
        List<PcoAgentRecord> agents = pcoAgentRepository.findAll();
        agents.forEach(agent -> {
            String topic = agent.getPcoId() + "-" + agent.getAgentId();
            producerTemplate.sendBody("direct:startListening", topic);
        });
    }

    public void processHandshake(HandshakeRequest request) {
        HandshakeRecord handshake = new HandshakeRecord(null,request.getCorrelationId(), request.getPcoId(), request.getUsername(),"");
        handshakeRepository.save(handshake);
        HandshakeResponse response = new HandshakeResponse(request.getCorrelationId(), request.getPcoId(), "registered");
        producerTemplate.sendBody("kafka:fenta-response", response);
    }

    public void registerPcoAgent(PcoAgentRequest request) {
        PcoAgentRecord pcoAgent = new PcoAgentRecord(null, request.getCorrelationId(), request.getPcoId(), request.getAgentId(), request.getUsername(),"");
        pcoAgent = pcoAgentRepository.save(pcoAgent);

        String newTopic = request.getPcoId() + "-" + request.getAgentId();
        String responseTopic = newTopic + "-response";

        producerTemplate.sendBody("direct:startListening", newTopic);
        producerTemplate.sendBody("direct:startListening", responseTopic);

        PcoAgentResponse response = new PcoAgentResponse(pcoAgent.getId(), request.getCorrelationId(), request.getPcoId(), request.getAgentId(), "registered");
        producerTemplate.sendBody("kafka:fenta-pco-agent-response", response);
    }

    public void processApiRequest(ApiRequest request) {
        String response = webClient
                .method(HttpMethod.valueOf(request.getMethod()))
                .uri(request.getApiUrl())
                .bodyValue(request.getRequest())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        ApiTransaction transaction = new ApiTransaction(null, request.getCorrelationId(), request.getPcoId(), request.getAgentId(), request.getApiUrl(), request.getRequest(), request.getMethod(), response);
        apiTransactionRepository.save(transaction);

        String responseTopic = request.getPcoId() + "-" + request.getAgentId() + "-response";
        producerTemplate.sendBody("kafka:" + responseTopic, transaction);
    }
}
*/
