package com.rits.qualityservice.event;

import com.rits.qualityservice.dto.QualityRequestList;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;
@Getter
@Setter
public class CalculateOeeEvent extends ApplicationEvent {

    private QualityRequestList qualityRequestList ;

    public CalculateOeeEvent(Object source) {
        super(source);
    }

    public CalculateOeeEvent(QualityRequestList qualityRequestList){
        super(qualityRequestList);
        this.qualityRequestList=qualityRequestList;

    }

}
