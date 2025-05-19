package com.rits.downtimeservice.event;

import com.rits.downtimeservice.model.DownTimeMessageModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;
@Getter
@Setter
public class CalculatePerformanceEvent extends ApplicationEvent {

    private DownTimeMessageModel downTimeMessageModel ;

    public CalculatePerformanceEvent(Object source) {
        super(source);
    }

    public CalculatePerformanceEvent(DownTimeMessageModel downTimeMessageModel){
        super(downTimeMessageModel);
        this.downTimeMessageModel=downTimeMessageModel;

    }

}
