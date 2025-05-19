package com.rits.processorderrelease_old.producer;

import com.rits.processorderrelease_old.dto.ReleaseRequest;

import java.util.List;
public class ProcessOrderProducer {
    private List<ReleaseRequest> message;

    public ProcessOrderProducer(List<ReleaseRequest> message) {
        this.message = message;
    }

    public List<ReleaseRequest> getSendResult() {
        return message;
    }
}
