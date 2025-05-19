package com.rits.pco.service.impl;


import com.rits.pco.dto.*;
import com.rits.pco.model.*;
import com.rits.pco.repository.*;
import com.rits.pco.service.KafkaListenerService;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpMethod;

//@Service
public class KafkaListenerServiceImpl{}
/*
public class KafkaListenerServiceImpl implements KafkaListenerService {
    private final HandshakeRepository handshakeRepository;
    private final PcoAgentRepository pcoAgentRepository;
    private final ApiTransactionRepository apiTransactionRepository;
    private final WebClient webClient;
    private final KafkaAdmin kafkaAdmin;

    private static final Logger log = LoggerFactory.getLogger(KafkaListenerServiceImpl.class);

    public KafkaListenerServiceImpl(HandshakeRepository handshakeRepository, PcoAgentRepository pcoAgentRepository, ApiTransactionRepository apiTransactionRepository, WebClient.Builder webClientBuilder, KafkaAdmin kafkaAdmin) {
        this.handshakeRepository = handshakeRepository;
        this.pcoAgentRepository = pcoAgentRepository;
        this.apiTransactionRepository = apiTransactionRepository;
        this.webClient = webClientBuilder.build();
        this.kafkaAdmin = kafkaAdmin;
    }

    @PostConstruct
    public void initializeTopicListeners() {
        List<PcoAgentRecord> agents = pcoAgentRepository.findAll();
        Set<String> existingTopics = new HashSet<>();

        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            ListTopicsResult topicsResult = adminClient.listTopics();
            existingTopics = topicsResult.names().get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.warn("⚠️ Kafka topics could not be retrieved. Kafka might not be running.");
            return;  // Allow application to continue running
        }

        List<String> topicsToSubscribe = new ArrayList<>();
        for (PcoAgentRecord agent : agents) {
            String topic = agent.getPcoId() + "-" + agent.getAgentId();
            if (existingTopics.contains(topic)) {  // Only subscribe if it exists
                topicsToSubscribe.add(topic);
            }
        }

        if (!topicsToSubscribe.isEmpty()) {
            log.info("📡 Subscribing to topics: {}", topicsToSubscribe);
            // Use a Kafka consumer to subscribe to these topics dynamically
        } else {
            log.warn("⚠️ No topics found to subscribe.");
        }
    }




    private void createTopic(String topicName) {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            short replicationFactor = 1;  // Use only 1 since it's a single broker setup
            NewTopic newTopic = new NewTopic(topicName, 1, replicationFactor);
            CreateTopicsResult result = adminClient.createTopics(Collections.singletonList(newTopic));
            result.all().get();  // Wait for completion
            log.info("✅ Created topic: {}", topicName);
        } catch (InterruptedException | ExecutionException e) {
            log.error("❌ Failed to create topic: {}", topicName, e);
        }
    }



    @KafkaListener(topics = "rits-check", groupId = "rits-group")
    public void processHandshake(HandshakeRequest request) {
        handshakeRepository.save(new HandshakeRecord(null, request.getCorrelationId(), request.getPcoId(), request.getUsername(),""));
    }

    @KafkaListener(topics = "rits-pco-agent", groupId = "rits-group")
    public void registerPcoAgent(PcoAgentRequest request) {
        String topicName = request.getPcoId() + "-" + request.getAgentId();

        log.info("🆕 Registering PCO agent and creating topic: {}", topicName);
        pcoAgentRepository.save(new PcoAgentRecord(null, request.getCorrelationId(), request.getPcoId(), request.getAgentId(), request.getUsername(),""));

        createTopic(topicName);
        subscribeToTopics(Collections.singleton(topicName));
    }

    private void subscribeToTopics(Set<String> topics) {
        if (topics.isEmpty()) {
            log.warn("⚠️ No topics found to subscribe.");
            return;
        }
        log.info("📡 Subscribing Kafka Consumer to topics: {}", topics);
        // Assuming your consumer is configurable, update the listener to consume new topics dynamically
    }

    */
/*@KafkaListener(topics = "#{'${rits-pcoid-agentid}'}", groupId = "rits-group")
    public void processApiRequest(ApiRequest request) {
        String response = webClient.post()
                .uri(request.getApiUrl())
                .bodyValue(request.getRequest())
                .retrieve()
                .bodyToMono(String.class)
                .block();
        apiTransactionRepository.save(new ApiTransaction(null, request.getCorrelationId(), request.getPcoId(), request.getAgentId(), request.getApiUrl(), request.getRequest(), request.getMethod(), response));
    }*//*



    @KafkaListener(topics = "#{'${rits-pcoid-agentid}'}", groupId = "rits-group")
    public void processApiRequest(String message) { // ✅ Receive as String
        ObjectMapper objectMapper = new ObjectMapper();

        ApiTransaction transaction;
        try {
            transaction = objectMapper.readValue(message, ApiTransaction.class); // ✅ Deserialize JSON String to ApiTransaction
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing Kafka message", e);
        }

        // ✅ Ensure `request` is stored as a String
        String requestBody = transaction.getRequest();

        String response = webClient
                .method(HttpMethod.valueOf(transaction.getMethod()))
                .uri(transaction.getApiUrl())
                .bodyValue(requestBody) // ✅ Now properly serialized
                .retrieve()
                .bodyToMono(String.class)
                .block();

        apiTransactionRepository.save(new ApiTransaction(
                null,
                transaction.getCorrelationId(),
                transaction.getPcoId(),
                transaction.getAgentId(),
                transaction.getApiUrl(),
                requestBody, // ✅ Ensure stored as String
                transaction.getMethod(),
                response
        ));
    }



}*/
