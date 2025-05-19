package com.rits.oeeservice.event;

import com.rits.common.dto.OeeFilterRequest;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CalculationEvent extends ApplicationEvent {
    private final OeeFilterRequest request;
    private final String step;

    public CalculationEvent(Object source, OeeFilterRequest request, String step) {
        super(source);
        this.request = request;
        this.step = step;
    }
}
