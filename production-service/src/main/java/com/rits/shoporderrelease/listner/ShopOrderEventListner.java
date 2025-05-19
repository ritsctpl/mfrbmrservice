package com.rits.shoporderrelease.listner;

import com.rits.shoporderrelease.dto.ReleaseRequest;
import com.rits.shoporderrelease.producer.ShopOrderProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("shopOrderEventListner")
@RequiredArgsConstructor
@Slf4j
public class ShopOrderEventListner {
    @Value("${DOCKER_KAFKA_HOST:localhost}")
    private String dockerKafkaHost;
    @EventListener
    public void handleShopOrderEvent(ShopOrderProducer event) {
        List<ReleaseRequest> message = event.getSendResult();
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, dockerKafkaHost+":9092");
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, com.rits.shoporderrelease.dto.PojoSerializer.class.getName());
        try {
            KafkaProducer<String, List<ReleaseRequest>> producer = new KafkaProducer<>(producerProps);
            ProducerRecord<String, List<ReleaseRequest>> record = new ProducerRecord<>(message.get(0).getTopic(), message);
            producer.send(record);
            producer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
