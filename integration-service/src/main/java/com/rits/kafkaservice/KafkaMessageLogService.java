package com.rits.kafkaservice;

import com.rits.kafkaevent.Message;
import com.rits.kafkapojo.ProductionLogPlacedEvent;
import com.rits.kafkapojo.ProductionLogRequest;

public interface KafkaMessageLogService {

    boolean logKafkaProductionLog(String topicName, ProductionLogPlacedEvent productionLogPlacedEvent);
    boolean logKafkaProductionLog(String topicName, ProductionLogRequest productionLogPlacedRequest);

    boolean logKafkaProductionLog(String topicName, Message<?> message);
}
