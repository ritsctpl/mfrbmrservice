package com.rits.scrapservice.service;

import com.rits.scrapservice.dto.ScrapRequest;
import com.rits.scrapservice.dto.ScrapRequestDetails;
import com.rits.scrapservice.model.RetrieveResponse;
import com.rits.scrapservice.model.Scrap;
import com.rits.scrapservice.model.ScrapMessageModel;

import java.util.List;

public interface ScrapService {
    ScrapMessageModel scrap(List<ScrapRequest> scrapRequestList) throws Exception;

    ScrapMessageModel unScrap(List<ScrapRequest> scrapRequestList) throws Exception;

    List<RetrieveResponse> retrieveByPcuShopOrder(String site, List<String> objectList);

    List<RetrieveResponse> retrieveByPcuShopOrderForUnScrap(String site, List<String> objectList);

    List<Scrap> retrieveAllScrap(String site);
    List<ScrapRequest> convertToScrapRequestList(List<ScrapRequestDetails> scrapRequestListNoBO);
}
