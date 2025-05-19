package com.rits.pco.service.impl;

import com.rits.pco.model.HandshakeRecord;
import com.rits.pco.model.PcoAgentRecord;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.AdminClient;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * ✅ Handles PCo Agent Registration & Deregistration
 */

@Component
public class PcoCamelRoutes extends RouteBuilder {

    private final MongoTemplate mongoTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaAdmin kafkaAdmin;
    private final CamelContext camelContext;
    private final WebClient webClient;  // ✅ Add missing WebClient dependency

    public PcoCamelRoutes(MongoTemplate mongoTemplate, KafkaTemplate<String, String> kafkaTemplate, KafkaAdmin kafkaAdmin, CamelContext camelContext, WebClient.Builder webClientBuilder) {
        this.mongoTemplate = mongoTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaAdmin = kafkaAdmin;
        this.camelContext = camelContext;
        this.webClient = webClientBuilder.build();  // ✅ Build WebClient instance
       // System.out.println("✅ Tested");
        // 🔹 Log all active routes
        camelContext.getRoutes().forEach(route ->
                System.out.println("✅ Active Camel Route: " + route.getId()));
    }

    @Override
    public void configure() {
        System.out.println("🚀 PcoCamelRoutes: Starting Camel route configuration!");

        // ✅ Ensure required topics exist
        createKafkaTopic("fenta-check");
        createKafkaTopic("fenta-response");
        createKafkaTopic("fenta-pco-agent");
        createKafkaTopic("fenta-pco-agent-response");

        // ✅ Handshake Confirmation
        /*from("kafka:fenta-check?groupId=rits-group&autoOffsetReset=earliest")
                .process(exchange -> System.out.println("🔥 Received message in fenta-check route"))
                .unmarshal().json(HandshakeRecord.class)
                .process(exchange -> {
                    HandshakeRecord request = exchange.getIn().getBody(HandshakeRecord.class);
                    request.setStatus("registered");
                    mongoTemplate.save(request);
                    exchange.getMessage().setBody(request);
                })
                .marshal().json()
                .to("kafka:fenta-response");*/

        from("kafka:fenta-check?groupId=rits-group&autoOffsetReset=earliest")
                .log("🔥 Received message in fenta-check route: ${body}") // ✅ Log incoming Kafka message
                .unmarshal().json(Map.class) // ✅ Unmarshal as a generic Map (safer handling)
                .process(exchange -> {
                    Map<String, Object> requestMap = exchange.getIn().getBody(Map.class);

                    // ✅ Map available fields to HandshakeRecord
                    HandshakeRecord request = new HandshakeRecord();
                    request.setCorrelationId((String) requestMap.getOrDefault("correlationId", ""));
                    request.setPcoId((String) requestMap.getOrDefault("pcoId", ""));
                    request.setUsername((String) requestMap.getOrDefault("username", ""));
                    request.setStatus("registered"); // ✅ Setting status to "registered"

                    mongoTemplate.save(request, "handshake_records"); // ✅ Save to MongoDB

                    // ✅ Send back the response
                    exchange.getMessage().setBody(request);
                })
                .marshal().json() // ✅ Convert back to JSON
                .log("✅ Sending processed message to fenta-response: ${body}")
                .to("kafka:fenta-response");

        // ✅ PCo Agent Registration & Deletion Handling
        from("kafka:fenta-pco-agent?groupId=rits-group")
                .process(exchange -> System.out.println("🔥 Received message in fenta-pco-agent route"))
                .unmarshal().json(PcoAgentRecord.class)
                .process(exchange -> {
                    PcoAgentRecord request = exchange.getIn().getBody(PcoAgentRecord.class);
                    String topicName = request.getPcoId() + "-" + request.getAgentId();
                    String responseTopic = topicName + "-response";

                    if ("deleted".equalsIgnoreCase(request.getStatus())) {
                        System.out.println("🛑 Deleting Agent: " + request.getAgentId());

                        // ✅ Correct MongoDB delete query
                        Query query = new Query();
                        query.addCriteria(Criteria.where("pcoId").is(request.getPcoId())
                                .and("agentId").is(request.getAgentId()));
                        mongoTemplate.remove(query, PcoAgentRecord.class);

                        removeCamelRoute(topicName);
                        removeKafkaTopic(topicName);
                        removeKafkaTopic(responseTopic);

                        System.out.println("❌ Agent " + request.getAgentId() + " deregistered successfully.");
                    } else {
                        System.out.println("✅ Registering Agent: " + request.getAgentId());

                        Query query = new Query(Criteria.where("pcoId").is(request.getPcoId())
                                .and("agentId").is(request.getAgentId()));

                        boolean recordExists = mongoTemplate.exists(query, PcoAgentRecord.class);

                        if (recordExists) {
                            System.out.println("⚠ Agent already registered, skipping MongoDB save and topic creation.");
                            return;
                        }

                        // ✅ Set response topic only if responseEnabled = true
                        boolean responseEnabled = request.isResponseEnabled();
                        String responseTopicElse = responseEnabled ? request.getPcoId() + "-" + request.getAgentId() + "-response" : null;

                        request.setStatus("registered");
                        request.setResponseEnabled(responseEnabled);
                        mongoTemplate.save(request);

                        createKafkaTopic(topicName);
                        if (responseEnabled) {
                            createKafkaTopic(responseTopicElse);
                        }

                        exchange.getContext().addRoutes(new DynamicKafkaRoute(topicName, mongoTemplate, webClient, kafkaTemplate, kafkaAdmin));
                        System.out.println("✅ New route registered for: " + topicName);
                    }


                    /*else {
                        System.out.println("✅ Registering Agent: " + request.getAgentId());

                        // ✅ Check if the record already exists in MongoDB
                        Query query = new Query(Criteria.where("pcoId").is(request.getPcoId())
                                .and("agentId").is(request.getAgentId()));

                        boolean recordExists = mongoTemplate.exists(query, PcoAgentRecord.class);

                        if (recordExists) {
                            System.out.println("⚠ Agent already registered, skipping MongoDB save and topic creation.");
                            return;  // ✅ Exit the process if the record already exists
                        }

                        // ✅ Proceed only if the record does not exist
                        request.setStatus("registered");
                        mongoTemplate.save(request);

                        createKafkaTopic(topicName);
                        createKafkaTopic(responseTopic);

                        // 🔹 ✅ Immediately register the new topic in Camel Context
                        exchange.getContext().addRoutes(new DynamicKafkaRoute(topicName, mongoTemplate, webClient, kafkaTemplate, kafkaAdmin));
                        System.out.println("✅ New route registered for: " + topicName);
                    }*/


                    exchange.getMessage().setBody(request);
                })
                .marshal().json()
                .to("kafka:fenta-pco-agent-response");

        System.out.println("✅ PcoCamelRoutes: Finished configuring Camel routes!");
    }



    private void createKafkaTopic(String topicName) {
        kafkaAdmin.createOrModifyTopics(new NewTopic(topicName, 1, (short) 1));
    }


    private void removeKafkaTopic(String topicName) {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            adminClient.deleteTopics(Collections.singleton(topicName)).all().get();
            System.out.println("✅ Deleted Kafka topic: " + topicName);
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error deleting Kafka topic: " + topicName);
        }
    }


    private void removeCamelRoute(String routeId) {
        try {
            Route route = camelContext.getRoute(routeId);
            if (route != null) {
                camelContext.getRouteController().stopRoute(routeId);
                camelContext.removeRoute(routeId);
                System.out.println("✅ Removed Camel route: " + routeId);
            }
        } catch (Exception e) {
            System.err.println("❌ Error removing Camel route: " + routeId);
        }
    }
}


/*


@Component
public class PcoCamelRoutes extends RouteBuilder {

    private final MongoTemplate mongoTemplate;
    private final WebClient webClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaAdmin kafkaAdmin;

    public PcoCamelRoutes(MongoTemplate mongoTemplate, WebClient.Builder webClientBuilder, KafkaTemplate<String, String> kafkaTemplate, KafkaAdmin kafkaAdmin) {
        this.mongoTemplate = mongoTemplate;
        this.webClient = webClientBuilder.build();
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaAdmin = kafkaAdmin;
    }

    @Override
    public void configure() {

        // ✅ Ensure topics exist
        createKafkaTopic("fenta-check");
        createKafkaTopic("fenta-response");
        createKafkaTopic("fenta-pco-agent");
        createKafkaTopic("fenta-pco-agent-response");

        // ✅ Handshake Confirmation
        from("kafka:fenta-check?groupId=rits-group")
                .unmarshal().json(HandshakeRecord.class)
                .process(exchange -> {
                    HandshakeRecord request = exchange.getIn().getBody(HandshakeRecord.class);
                    request.setStatus("registered");
                    mongoTemplate.save(request);
                    exchange.getMessage().setBody(request);
                })
                .marshal().json()
                .to("kafka:fenta-response");

        // ✅ PCo Agent Registration & Topic Creation
        from("kafka:fenta-pco-agent?groupId=rits-group")
                .unmarshal().json(PcoAgentRecord.class)
                .process(exchange -> {
                    PcoAgentRecord request = exchange.getIn().getBody(PcoAgentRecord.class);
                    request.setStatus("registered");
                    mongoTemplate.save(request);

                    String topicName = request.getPcoId() + "-" + request.getAgentId();
                    String responseTopic = topicName + "-response";

                    // 🔹 Create Kafka topics dynamically
                    createKafkaTopic(topicName);
                    createKafkaTopic(responseTopic);

                    exchange.getMessage().setBody(request);

                    // 🔹 ✅ Immediately register the new topic in Camel Context
                    exchange.getContext().addRoutes(new DynamicKafkaRoute(topicName, mongoTemplate, webClient, kafkaTemplate, kafkaAdmin));
                })
                .marshal().json()
                .to("kafka:fenta-pco-agent-response");
    }
    private void createKafkaTopic(String topicName) {
        kafkaAdmin.createOrModifyTopics(new NewTopic(topicName, 1, (short) 1));
    }
}
*/

