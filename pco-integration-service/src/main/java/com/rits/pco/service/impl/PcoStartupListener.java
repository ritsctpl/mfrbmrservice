package com.rits.pco.service.impl;

import com.rits.pco.model.PcoAgentRecord;
import org.apache.camel.CamelContext;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.apache.kafka.clients.admin.NewTopic;

import java.util.List;

@Component
public class PcoStartupListener {

    private final MongoTemplate mongoTemplate;
    private final CamelContext camelContext;
    private final WebClient webClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaAdmin kafkaAdmin;

    public PcoStartupListener(
            MongoTemplate mongoTemplate,
            CamelContext camelContext,
            WebClient.Builder webClientBuilder,
            KafkaTemplate<String, String> kafkaTemplate,
            KafkaAdmin kafkaAdmin) {
        this.mongoTemplate = mongoTemplate;
        this.camelContext = camelContext;
        this.webClient = webClientBuilder.build();
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaAdmin = kafkaAdmin;
    }

    /**
     * 🚀 Ensures Kafka topics & initializes consumers AFTER Spring Context is fully started.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        System.out.println("✅ Application Started! Initializing Kafka Consumers...");

        // Ensure CamelContext is started before adding routes
        if (!camelContext.isStarted()) {
            try {
                System.out.println("⚡ Starting CamelContext...");
                camelContext.start();
            } catch (Exception e) {
                System.err.println("❌ Error starting CamelContext: " + e.getMessage());
                return;
            }
        }

        // Fetch all registered PCo agents
        List<PcoAgentRecord> pcoAgents = mongoTemplate.findAll(PcoAgentRecord.class);

        // If no records exist, do nothing
        if (pcoAgents.isEmpty()) {
            System.out.println("⚠ No PCo agents found in MongoDB. Skipping Kafka topic subscription.");
            return;
        }

        for (PcoAgentRecord agent : pcoAgents) {
            try {
                String topic = agent.getPcoId() + "-" + agent.getAgentId();
                String responseTopic = topic + "-response";
                boolean responseEnabled = agent.isResponseEnabled();

                System.out.println("✅ Ensuring Kafka topics exist: " + topic + " & " + responseTopic);
                createKafkaTopic(topic);
             //   createKafkaTopic(responseTopic);

                if (responseEnabled) {
                    String responseTopicEnabled = topic + "-response";
                    createKafkaTopic(responseTopicEnabled);
                }
                System.out.println("✅ Subscribing to dynamic topics: " + topic + " & " + responseTopic);
                camelContext.addRoutes(new DynamicKafkaRoute(topic, mongoTemplate, webClient, kafkaTemplate, kafkaAdmin));

            } catch (Exception e) {
                System.err.println("❌ Error subscribing to topic: " + e.getMessage());
            }
        }
    }

    /**
     * ✅ Kafka Topic Creation (Only If It Doesn't Exist)
     */
    private void createKafkaTopic(String topicName) {
        kafkaAdmin.createOrModifyTopics(new NewTopic(topicName, 1, (short) 1));
    }
}


/*
package com.rits.pco.service.impl;

import com.rits.pco.model.PcoAgentRecord;
import org.apache.camel.CamelContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.apache.kafka.clients.admin.NewTopic;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class PcoStartupListener {

    private final MongoTemplate mongoTemplate;
    private final CamelContext camelContext;
    private final WebClient webClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaAdmin kafkaAdmin;

    public PcoStartupListener(MongoTemplate mongoTemplate, CamelContext camelContext, WebClient.Builder webClientBuilder, KafkaTemplate<String, String> kafkaTemplate, KafkaAdmin kafkaAdmin) {
        this.mongoTemplate = mongoTemplate;
        this.camelContext = camelContext;
        this.webClient = webClientBuilder.build();
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaAdmin = kafkaAdmin;
    }

    @PostConstruct
    public void initialize() {
        // Fetch all registered PCo agents
        List<PcoAgentRecord> pcoAgents = mongoTemplate.findAll(PcoAgentRecord.class);

        // If no records exist, do nothing
        if (pcoAgents.isEmpty()) {
            System.out.println("⚠ No PCo agents found in MongoDB. Skipping Kafka topic subscription.");
            return;
        }

        for (PcoAgentRecord agent : pcoAgents) {
            try {
                String topic = agent.getPcoId() + "-" + agent.getAgentId();
                String responseTopic = topic + "-response";

                System.out.println("✅ Ensuring Kafka topics exist: " + topic + " & " + responseTopic);
                createKafkaTopic(topic);
                createKafkaTopic(responseTopic);

                System.out.println("✅ Subscribing to dynamic topics: " + topic + " & " + responseTopic);
                camelContext.addRoutes(new DynamicKafkaRoute(topic, mongoTemplate, webClient, kafkaTemplate, kafkaAdmin));

            } catch (Exception e) {
                System.err.println("❌ Error subscribing to topic: " + e.getMessage());
            }
        }
    }

    */
/**
     * ✅ Kafka Topic Creation (Only If It Doesn't Exist)
     *//*

    private void createKafkaTopic(String topicName) {
        kafkaAdmin.createOrModifyTopics(new NewTopic(topicName, 1, (short) 1));
    }
}

*/
