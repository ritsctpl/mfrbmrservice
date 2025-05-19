package com.rits.shoporderrelease.listner;

import com.rits.pcuheaderservice.dto.PcuHeaderRequest;
import com.rits.shoporderrelease.producer.PcuHeaderProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PcuHeaderListner {
    @Value("${DOCKER_KAFKA_HOST:localhost}")
    private String dockerKafkaHost;

@EventListener
public void handleShopOrderEvent(PcuHeaderProducer event) {
    PcuHeaderRequest message = event.getSendResult();
    Map<String, Object> producerProps = new HashMap<>();
    producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, dockerKafkaHost+":9092");
    producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, com.rits.shoporderrelease.dto.PojoSerializer.class.getName());
    try {
        KafkaProducer<String, PcuHeaderRequest> producer = new KafkaProducer<>(producerProps);
        ProducerRecord<String, PcuHeaderRequest> record = new ProducerRecord<>(message.getTopic(), message);
        producer.send(record);
        producer.close();
    } catch (Exception e) {
        e.printStackTrace();
    }

}
}
