package com.rits.processorderrelease_old.event;

import com.rits.processorderrelease_old.dto.ReleaseRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class ProcessOrderEvent extends ApplicationEvent {
    private String shopOrderRelType;
    private ReleaseRequest releaseRequest;

    public ProcessOrderEvent(String shopOrderRelType) {
        super(shopOrderRelType);
        this.shopOrderRelType=shopOrderRelType;
    }

    public ProcessOrderEvent(Object source, String shopOrderRelType) {
        super(shopOrderRelType);
        this.shopOrderRelType=shopOrderRelType;
    }

    public ProcessOrderEvent(ReleaseRequest releaseRequest) {
        super(releaseRequest);
        this.releaseRequest = releaseRequest;
    }
}
