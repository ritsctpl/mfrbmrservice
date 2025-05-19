package com.rits.bomheaderservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.rits.bomheaderservice.dto.BomHeaderRequest;
import com.rits.bomheaderservice.dto.Extension;
import com.rits.bomheaderservice.model.Bom;
import com.rits.bomheaderservice.model.BomHeader;
import com.rits.bomheaderservice.model.BomHeaderMessageModel;

import java.util.List;

public interface BomHeaderService {

     public BomHeaderMessageModel create(BomHeaderRequest bomHeaderRequest) throws Exception;
     public BomHeaderMessageModel update(BomHeaderRequest bomHeaderRequest) throws Exception;
     public Bom retrieve(BomHeaderRequest bomHeaderRequest) throws Exception;
     public List<BomHeader> updateStatusOfBomToReleased(BomHeaderRequest bomHeaderRequest) throws Exception;

     public String callExtension(Extension extension);

     BomHeaderMessageModel validation(JsonNode payload);

     Boolean deleteBomHeaderByPcu(String site, String pcuBO);

     Boolean unDeleteBomHeaderByPcu(String site, String pcuBO);
}
