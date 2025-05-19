package com.rits.kafkaservice;


import com.rits.workinstructionservice.dto.ProductionLogRequest;

public class ProductionLogProducer {
    private ProductionLogRequest message;

    public ProductionLogProducer(ProductionLogRequest message) {
        this.message = message;
    }

    public ProductionLogRequest  getSendResult() {
        return message;
    }

}
