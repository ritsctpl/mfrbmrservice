package com.rits.configuration;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaCamelConfiguration extends RouteBuilder {

    @Value("${kafka.broker.address}")
    private String kafkaBroker;

    @Value("${kafka.default.topic}")
    private String kafkaDefaultTopic;

    @Override
    public void configure() throws Exception {
        from("kafka:" + kafkaDefaultTopic + "?brokers=" + kafkaBroker + "&groupId=dynamicConsumerGroup&autoOffsetReset=earliest")
                .routeId("dynamicMessageProcessorRoute")
                .to("bean:messageProcessingService?method=processMessage(${body}, ${header[kafka.TOPIC]})")
                .log("Processed message for topic: ${header[kafka.TOPIC]}");
    }
}
