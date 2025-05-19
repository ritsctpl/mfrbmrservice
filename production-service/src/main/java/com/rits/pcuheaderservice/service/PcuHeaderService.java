package com.rits.pcuheaderservice.service;

import com.rits.pcuheaderservice.dto.Bom;
import com.rits.pcuheaderservice.dto.PcuHeaderRequest;
import com.rits.pcuheaderservice.dto.Response;
import com.rits.pcuheaderservice.model.PcuHeaderMessageModel;
import com.rits.pcuheaderservice.model.PcuHeader;

import java.util.List;

public interface PcuHeaderService {

    public PcuHeaderMessageModel create(PcuHeaderRequest pcuHeaderRequest) throws Exception;

    Bom retrieveBom(PcuHeaderRequest pcuHeaderRequest);

    public List<PcuHeader> update(PcuHeaderRequest pcuHeaderRequest) throws Exception;

    public List<PcuHeader> retrieve(String site) throws Exception;

    public PcuHeader retrieveByPcuBO(PcuHeaderRequest pcuHeaderRequest) throws Exception;

    List<PcuHeader> retrieveByShopOrder(PcuHeaderRequest pcuHeaderRequest) throws Exception;

    List<PcuHeader> retrieveByPcuBOShopOrder(PcuHeaderRequest pcuHeaderRequest) throws Exception;

    public Boolean isExist(PcuHeaderRequest pcuHeadersRequest) throws Exception;

    public Boolean CheckRouterReleased(PcuHeaderRequest pcuHeaderRequest) throws Exception;

    public Boolean CheckBomReleased(PcuHeaderRequest pcuHeaderRequest) throws Exception;

    public PcuHeader updateStatusOfRouterToReleased(PcuHeaderRequest pcuHeaderRequest) throws Exception;

    public PcuHeader updateStatusOfBomToReleased(PcuHeaderRequest pcuHeaderRequest) throws Exception;

    public Boolean checkIfBomAndRouterReleased(PcuHeaderRequest pcuHeaderRequest) throws  Exception;

    public Response delete(PcuHeaderRequest pcuHeaderRequest) throws Exception;

    Response unDelete(PcuHeaderRequest pcuHeaderRequest) throws Exception;

    public List<PcuHeader> retrieveByItem(String site, String itemBO) throws Exception;

    List<PcuHeader> retrieveAll(String site);
}
