package com.rits.oeeservice.listner;

import com.rits.common.dto.OeeFilterRequest;
import com.rits.oeeservice.event.CalculationEvent;
import com.rits.oeeservice.service.OeeServiceImpl;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CalculationEventListener {

    private final OeeServiceHelper oeeServiceHelper;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    public void handleCalculationEvent(CalculationEvent event) {
        OeeFilterRequest request = event.getRequest();
        String step = event.getStep();

        boolean success = false;

        switch (step) {
            case "DOWN_TIME":
                success = oeeServiceHelper.calculateDownTime(request);
                if (success) {
                    eventPublisher.publishEvent(new CalculationEvent(this, request, "AVAILABILITY"));
                }
                break;

            case "AVAILABILITY":
                success = oeeServiceHelper.calculateAvailability(request);
                if (success) {
                    eventPublisher.publishEvent(new CalculationEvent(this, request, "PERFORMANCE"));
                }
                break;

            case "PERFORMANCE":
                success = oeeServiceHelper.calculatePerformance(request);
                if (success) {
                    eventPublisher.publishEvent(new CalculationEvent(this, request, "QUALITY"));
                }
                break;

            case "QUALITY":
                success = oeeServiceHelper.calculateQuality(request);
                if (success) {
                    eventPublisher.publishEvent(new CalculationEvent(this, request, "OEE"));
                }
                break;

            case "OEE":
                oeeServiceHelper.calculateOee(request);
                break;

            default:
                throw new IllegalArgumentException("Invalid calculation step: " + step);
        }
    }
}
