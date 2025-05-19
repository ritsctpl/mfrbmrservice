package com.rits.pco.service.impl;

import com.rits.pco.model.ApiTransaction;
import com.rits.pco.model.PcoAgentRecord;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;

import java.util.Map;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class DynamicKafkaRoute extends RouteBuilder {
    private final String topic;
    private final MongoTemplate mongoTemplate;
    private final WebClient webClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaAdmin kafkaAdmin;

    public DynamicKafkaRoute(String topic, MongoTemplate mongoTemplate, WebClient webClient, KafkaTemplate<String, String> kafkaTemplate, KafkaAdmin kafkaAdmin) {
        this.topic = topic;
        this.mongoTemplate = mongoTemplate;
        this.webClient = webClient;
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaAdmin = kafkaAdmin;
    }

    @Override
    public void configure() {
        System.out.println("✅ DynamicKafkaRoute: Configuring the Camel...");

        from("kafka:" + topic + "?groupId=rits-group")
                .unmarshal().json(ApiTransaction.class)
                .process(exchange -> {
                    ApiTransaction request = exchange.getIn().getBody(ApiTransaction.class);
                    Map<String, Object> requestBodyMap = request.getRequest();

                    // ✅ Save request before API call
                    mongoTemplate.save(request);

                    // ✅ Fetch `responseEnabled` from MongoDB at runtime
                    Query query = new Query(Criteria.where("pcoId").is(request.getPcoId())
                            .and("agentId").is(request.getAgentId()));

                    PcoAgentRecord agentRecord = mongoTemplate.findOne(query, PcoAgentRecord.class);

                    boolean responseEnabled = (agentRecord != null) && agentRecord.isResponseEnabled();
                    System.out.println(" Receieved Message is "+ requestBodyMap);
                    if (responseEnabled) {
                        System.out.println("✅ Response enabled for topic: " + topic);
                    } else {
                        System.out.println("⚠ Response sending is disabled for topic: " + topic);
                    }

                    // Convert method string to HttpMethod
                    //HttpMethod httpMethod = HttpMethod.resolve(request.getMethod().toUpperCase());

                    // ✅ Default method to POST if null
                    String method = request.getMethod() != null ? request.getMethod() : "POST";
                    request.setMethod(method);  // Optional: persist back default method

                    HttpMethod httpMethod = HttpMethod.resolve(method.toUpperCase());

                    // Build WebClient request dynamically
                    WebClient.RequestHeadersSpec<?> requestSpec;

                    if (httpMethod == HttpMethod.GET || httpMethod == HttpMethod.DELETE) {
                        requestSpec = webClient.method(httpMethod).uri(request.getApiUrl());
                    } else {
                        requestSpec = ((RequestBodySpec) webClient.method(httpMethod)
                                .uri(request.getApiUrl()))
                                .bodyValue(requestBodyMap);
                    }

                    // Send request and capture response
                    String response = requestSpec.retrieve()
                            .bodyToMono(String.class)
                            .block();

                    request.setResponse(response);
                    mongoTemplate.save(request);

                    if (responseEnabled) {
                        String responseTopic = topic + "-response";
                        exchange.getMessage().setBody(request);
                        exchange.getContext().createProducerTemplate().sendBody("kafka:" + responseTopic, request);
                    }
                });
    }
}

/*
package com.rits.pco.service.impl;

import com.rits.pco.model.ApiTransaction;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;

import java.util.Map;

public class DynamicKafkaRoute extends RouteBuilder {
    private final String topic;

    private final boolean responseEnabled;  // ✅ Use boolean flag
    private final MongoTemplate mongoTemplate;
    private final WebClient webClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaAdmin kafkaAdmin;

    public DynamicKafkaRoute(String topic, boolean responseEnabled, MongoTemplate mongoTemplate, WebClient webClient, KafkaTemplate<String, String> kafkaTemplate, KafkaAdmin kafkaAdmin) {
        this.topic = topic;
        this.responseEnabled = responseEnabled;  // ✅ Assign boolean flag
        this.mongoTemplate = mongoTemplate;
        this.webClient = webClient;
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaAdmin = kafkaAdmin;
    }

    @Override
    public void configure() {
        System.out.println("✅ DynamicKafkaRoute: Configuring the Camel...");
        from("kafka:" + topic + "?groupId=rits-group")
                .unmarshal().json(ApiTransaction.class)
                .process(exchange -> {
                    ApiTransaction request = exchange.getIn().getBody(ApiTransaction.class);

                    // ✅ No need to convert, request is already a Map
                    Map<String, Object> requestBodyMap = request.getRequest();

                    // ✅ Save request before API call
                    mongoTemplate.save(request);

                    // Convert method string to HttpMethod
                    HttpMethod httpMethod = HttpMethod.resolve(request.getMethod().toUpperCase());

                    // Build WebClient request dynamically
                    WebClient.RequestHeadersSpec<?> requestSpec;

                    if (httpMethod == HttpMethod.GET || httpMethod == HttpMethod.DELETE) {
                        // GET & DELETE requests do not support body
                        requestSpec = webClient.method(httpMethod)
                                .uri(request.getApiUrl());
                    } else {
                        // POST, PUT, PATCH require a body
                        requestSpec = ((RequestBodySpec) webClient.method(httpMethod)
                                .uri(request.getApiUrl()))
                                .bodyValue(requestBodyMap);
                    }

                    // Send request and capture response
                    String response = requestSpec.retrieve()
                            .bodyToMono(String.class)
                            .block();

                    request.setResponse(response);
                    mongoTemplate.save(request);

                    if (responseEnabled) {
                        String responseTopic = topic + "-response";
                        exchange.getMessage().setBody(request);
                        exchange.getContext().createProducerTemplate().sendBody("kafka:" + responseTopic, request);
                    } else {
                        System.out.println("⚠ Response sending is disabled for topic: " + topic);
                    }
                    // Send response to response topic
                   */
/* String responseTopic = topic + "-response";
                    exchange.getMessage().setBody(request);
                    exchange.getContext().createProducerTemplate().sendBody("kafka:" + responseTopic, request);*//*

                });
    }
}

*/
