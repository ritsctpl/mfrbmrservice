package com.rits.scrapservice.controller;

import com.rits.scrapservice.dto.RetrieveRequest;
import com.rits.scrapservice.dto.ScrapRequest;
import com.rits.scrapservice.dto.ScrapRequestDetails;
import com.rits.scrapservice.model.*;
import com.rits.scrapservice.service.ScrapService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/scrap-service")
public class ScrapController {

    private final ScrapService scrapService;
    @PostMapping("scrap")
    public ScrapMessageModel scrap(@RequestBody List<ScrapRequestDetails> scrapRequestListNoBO)
    {
        try {
            List<ScrapRequest> scrapRequestList = scrapService.convertToScrapRequestList(scrapRequestListNoBO);
            return scrapService.scrap(scrapRequestList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("unScrap")
    public ScrapMessageModel unScrap(@RequestBody List<ScrapRequestDetails> scrapRequestListNoBO)
    {
        try {
            List<ScrapRequest> scrapRequestList = scrapService.convertToScrapRequestList(scrapRequestListNoBO);
            return scrapService.unScrap(scrapRequestList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieve")
    public List<RetrieveResponse> retrieve(@RequestBody RetrieveRequest retrieveRequest)
    {
        try {
            return scrapService.retrieveByPcuShopOrder(retrieveRequest.getSite(), retrieveRequest.getObjectList());
//            return responses.stream()
//                    .map(RetrieveResponseNoBO::new)
//                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("retrieveForUnscrap")
    public  List<RetrieveResponse> retrieveScrapped(@RequestBody RetrieveRequest retrieveRequest)
    {
        try {
            return scrapService.retrieveByPcuShopOrderForUnScrap(retrieveRequest.getSite(), retrieveRequest.getObjectList());
//            return responses.stream()
//                    .map(RetrieveResponseNoBO::new)
//                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("retrieveAll")
    public  List<ScrapNobo> retrieveAll(@RequestBody RetrieveRequest retrieveRequest)
    {
        try {
            List<Scrap> scrap = scrapService.retrieveAllScrap(retrieveRequest.getSite());

            List<ScrapNobo> scrapNobo = scrap.stream()
                    .map(ScrapNobo::new)
                    .collect(Collectors.toList());
            return scrapNobo;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
