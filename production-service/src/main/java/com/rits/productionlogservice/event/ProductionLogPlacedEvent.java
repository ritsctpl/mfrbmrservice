package com.rits.productionlogservice.event;

import com.rits.productionlogservice.dto.ProductionLogRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.time.Clock;

@Getter
@Setter
public class ProductionLogPlacedEvent extends ApplicationEvent {
    private String productionLogType;
    private ProductionLogRequest productionLogRequest;

    public ProductionLogPlacedEvent(String productionLogType) {
        super(productionLogType);
        this.productionLogType=productionLogType;
    }

    public ProductionLogPlacedEvent(Object source, String productionLogType) {
        super(productionLogType);
        this.productionLogType=productionLogType;
    }

    public ProductionLogPlacedEvent(ProductionLogRequest productionLogRequest) {
        super(productionLogRequest);
        this.productionLogRequest=productionLogRequest;
    }
}
