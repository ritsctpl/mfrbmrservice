package com.rits.pcudoneservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.rits.pcudoneservice.dto.Extension;
import com.rits.pcudoneservice.dto.PcuDoneRequest;
import com.rits.pcudoneservice.dto.PcuDoneRequestNoBO;
import com.rits.pcudoneservice.model.MessageModel;
import com.rits.pcudoneservice.model.PcuDone;
import com.rits.pcudoneservice.model.PcuDoneNoBO;

public interface PcuDoneService {
    public MessageModel insert(PcuDoneRequest pcuDoneRequest) throws Exception;
    public String callExtension(Extension extension) throws Exception;

    Boolean delete(String site, String pcuBO);

    Boolean unDelete(String site, String pcuBO);

    PcuDone retrieve(String site, String pcuBO);
    PcuDoneRequest convertToPcuDoneRequest(PcuDoneRequestNoBO requestNoBO);
    PcuDoneNoBO convertToPcuDoneNoBO(PcuDone pcuDone);
}
