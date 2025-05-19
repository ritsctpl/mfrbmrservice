package com.rits.performanceservice.event;

import com.rits.performanceservice.dto.PerformanceRequestList;
import com.rits.qualityservice.dto.QualityRequestList;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;
@Getter
@Setter
public class CalculateQualityEvent extends ApplicationEvent {

    private PerformanceRequestList performanceRequestList ;

    public CalculateQualityEvent(Object source) {
        super(source);
    }

    public CalculateQualityEvent(PerformanceRequestList performanceRequestList){
        super(performanceRequestList);
        this.performanceRequestList=performanceRequestList;

    }

}
