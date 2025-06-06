package com.rits.processorderrelease_old.listner;

import com.rits.processorderrelease_old.dto.BNOHeaderRequest;
import com.rits.processorderrelease_old.dto.PojoSerializer;
import com.rits.processorderrelease_old.producer.BnoHeaderProducer;
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
import java.util.Map;

@Component("bnoHeaderListner")
@RequiredArgsConstructor
@Slf4j
public class BnoHeaderListner {
    @Value("${DOCKER_KAFKA_HOST:localhost}")
    private String dockerKafkaHost;

@EventListener
public void handleProcessOrderEvent(BnoHeaderProducer event) {
    BNOHeaderRequest message = event.getSendResult();
    Map<String, Object> producerProps = new HashMap<>();
    producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, dockerKafkaHost+":9092");
    producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, PojoSerializer.class.getName());
    try {
        KafkaProducer<String, BNOHeaderRequest> producer = new KafkaProducer<>(producerProps);
        ProducerRecord<String, BNOHeaderRequest> record = new ProducerRecord<>(message.getTopic(), message);
        producer.send(record);
        producer.close();
    } catch (Exception e) {
        e.printStackTrace();
    }

}
}
