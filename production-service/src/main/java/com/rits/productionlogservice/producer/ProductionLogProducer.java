package com.rits.productionlogservice.producer;


import com.rits.productionlogservice.dto.ProductionLogRequest;

public class ProductionLogProducer {
    private ProductionLogRequest message;

    public ProductionLogProducer(ProductionLogRequest message) {
        this.message = message;
    }

    public ProductionLogRequest  getSendResult() {
        return message;
    }

}
