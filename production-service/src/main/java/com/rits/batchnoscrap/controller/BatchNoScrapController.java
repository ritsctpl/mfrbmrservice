package com.rits.batchnoscrap.controller;


import com.rits.batchnoscrap.dto.BatchNoScrapQtyResponse;
import com.rits.batchnoscrap.dto.BatchNoScrapRequest;
import com.rits.batchnoscrap.exception.BatchNoScrapException;
import com.rits.batchnoscrap.model.BatchNoScrap;
import com.rits.batchnoscrap.model.BatchNoScrapMessageModel;
import com.rits.batchnoscrap.service.BatchNoScrapService;
import com.rits.scrapservice.dto.RetrieveRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/batchnoscrap-service")
public class BatchNoScrapController {

    private final BatchNoScrapService batchNoScrapService;
    @PostMapping("scrap")
    public BatchNoScrapMessageModel scrap(@RequestBody BatchNoScrapRequest batchNoScrapRequest)
    {
        try {
            return batchNoScrapService.scrap(batchNoScrapRequest);
        } catch (BatchNoScrapException batchNoScrapException){
            throw batchNoScrapException;
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("unScrap")
    public BatchNoScrapMessageModel unScrap(@RequestBody BatchNoScrapRequest batchNoScrapRequest)
    {
        try {
            return batchNoScrapService.unScrap(batchNoScrapRequest);
        } catch (BatchNoScrapException batchNoScrapException){
            throw batchNoScrapException;
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @PostMapping("delete")
    public BatchNoScrapMessageModel delete(@RequestBody BatchNoScrapRequest batchNoScrapRequest)
    {
        try {
            return batchNoScrapService.delete(batchNoScrapRequest);
        } catch (BatchNoScrapException batchNoScrapException){
            throw batchNoScrapException;
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieve")
    public BatchNoScrapMessageModel retrieve(@RequestBody BatchNoScrapRequest batchNoScrapRequest)
    {
        try {
            return batchNoScrapService.retrieve(batchNoScrapRequest);
        } catch (BatchNoScrapException batchNoScrapException){
            throw batchNoScrapException;
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @PostMapping("retrieveAll")
    public  List<BatchNoScrap> retrieveAll(@RequestBody RetrieveRequest retrieveRequest)
    {
        try {
            List<BatchNoScrap> scrap = batchNoScrapService.retrieveAllScrap(retrieveRequest.getSite());

            List<BatchNoScrap> scrapNobo = scrap.stream()
                    .map(original -> {
                        BatchNoScrap copy = new BatchNoScrap();
                        BeanUtils.copyProperties(original, copy);
                        return copy;
                    })
                    .collect(Collectors.toList());
            return scrapNobo;
        } catch (BatchNoScrapException batchNoScrapException){
            throw batchNoScrapException;
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getBatchNoScrapByPhaseAndOperation")
    public BatchNoScrapQtyResponse getBatchNoScrapByPhaseAndOperation(@RequestBody BatchNoScrapRequest request){
        try {
            return batchNoScrapService.getBatchNoScrapByPhaseAndOperation(request);
        } catch (BatchNoScrapException batchNoScrapException){
            throw batchNoScrapException;
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
