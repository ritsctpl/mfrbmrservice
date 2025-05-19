package com.rits.processorderrelease_old.producer;

import com.rits.processorderrelease_old.dto.BNOHeaderRequest;

public class BnoHeaderProducer {

    private BNOHeaderRequest message;

    public BnoHeaderProducer(BNOHeaderRequest message) {
        this.message = message;
    }

    public BNOHeaderRequest getSendResult() {
        return message;
    }


}
