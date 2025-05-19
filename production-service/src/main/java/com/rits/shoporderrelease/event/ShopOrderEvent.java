package com.rits.shoporderrelease.event;

import com.rits.shoporderrelease.dto.ReleaseRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class ShopOrderEvent extends ApplicationEvent {
    private String shopOrderRelType;
    private ReleaseRequest releaseRequest;

    public ShopOrderEvent(String shopOrderRelType) {
        super(shopOrderRelType);
        this.shopOrderRelType=shopOrderRelType;
    }

    public ShopOrderEvent(Object source, String shopOrderRelType) {
        super(shopOrderRelType);
        this.shopOrderRelType=shopOrderRelType;
    }

    public ShopOrderEvent(ReleaseRequest releaseRequest) {
        super(releaseRequest);
        this.releaseRequest = releaseRequest;
    }
}
