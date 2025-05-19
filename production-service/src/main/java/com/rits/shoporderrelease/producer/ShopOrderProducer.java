package com.rits.shoporderrelease.producer;
import com.rits.shoporderrelease.dto.ReleaseRequest;

import java.util.List;
public class ShopOrderProducer {
    private List<ReleaseRequest> message;

    public ShopOrderProducer(List<ReleaseRequest> message) {
        this.message = message;
    }

    public List<ReleaseRequest> getSendResult() {
        return message;
    }
}
