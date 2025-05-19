package com.rits.ncservice.service;

import com.rits.listmaintenceservice.exception.ListMaintenanceException;
import com.rits.ncservice.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
@Service
@RequiredArgsConstructor
public class NCServiceImpl implements NCService{
    private final WebClient.Builder webClientBuilder;

    @Value("${nccode-service.url}/retrieve")
    private String NCCodeUrl;


    @Override
    public List<DispositionRoutings> getDispositionRoutings(String nCCode, String site) throws Exception {
        NCCodeRequest ncCodeRequest = NCCodeRequest.builder().site(site).ncCode(nCCode).build();
        NCCode ncCodeResponse = webClientBuilder.build()
                .post()
                .uri(NCCodeUrl)
                .bodyValue(ncCodeRequest)
                .retrieve()
                .bodyToMono(NCCode.class)
                .block();
        if (ncCodeResponse==null|| ncCodeResponse.getNcCode().isEmpty()) {
            throw new ListMaintenanceException(800);
        }
        return ncCodeResponse.getDispositionRoutingsList();
    }

    @Override
    public List<String> getListOfNcCode(String NcGroup, String site) throws Exception {
        return null;
    }
}
