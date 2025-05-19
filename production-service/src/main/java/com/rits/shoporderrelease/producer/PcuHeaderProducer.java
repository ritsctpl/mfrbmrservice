package com.rits.shoporderrelease.producer;

import com.rits.pcuheaderservice.dto.PcuHeaderRequest;

public class PcuHeaderProducer {

    private PcuHeaderRequest message;

    public PcuHeaderProducer(PcuHeaderRequest message) {
        this.message = message;
    }

    public PcuHeaderRequest getSendResult() {
        return message;
    }
}
